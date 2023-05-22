package org.lightnsalt.hikingdom.service.club.service;

import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.PostConstruct;

import org.lightnsalt.hikingdom.common.dto.CustomSlice;
import org.lightnsalt.hikingdom.common.error.ErrorCode;
import org.lightnsalt.hikingdom.common.error.GlobalException;
import org.lightnsalt.hikingdom.domain.common.enumType.JoinRequestStatusType;
import org.lightnsalt.hikingdom.domain.entity.club.Club;
import org.lightnsalt.hikingdom.domain.entity.club.ClubAsset;
import org.lightnsalt.hikingdom.domain.entity.club.ClubMember;
import org.lightnsalt.hikingdom.domain.entity.club.record.ClubTotalHikingStatistic;
import org.lightnsalt.hikingdom.domain.entity.info.AssetInfo;
import org.lightnsalt.hikingdom.domain.entity.info.BaseAddressInfo;
import org.lightnsalt.hikingdom.domain.entity.member.Member;
import org.lightnsalt.hikingdom.service.club.dto.request.ClubInfoReq;
import org.lightnsalt.hikingdom.service.club.dto.response.ClubDetailRes;
import org.lightnsalt.hikingdom.service.club.dto.response.ClubSearchRes;
import org.lightnsalt.hikingdom.service.club.dto.response.ClubSimpleDetailRes;
import org.lightnsalt.hikingdom.service.club.repository.ClubAssetRepository;
import org.lightnsalt.hikingdom.service.club.repository.ClubJoinRequestRepository;
import org.lightnsalt.hikingdom.service.club.repository.ClubMemberRepository;
import org.lightnsalt.hikingdom.service.club.repository.ClubRepository;
import org.lightnsalt.hikingdom.service.club.repository.ClubSearchRepositoryCustom;
import org.lightnsalt.hikingdom.service.club.repository.record.ClubTotalHikingStatisticRepository;
import org.lightnsalt.hikingdom.service.info.repository.BaseAddressInfoRepository;
import org.lightnsalt.hikingdom.service.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 소모임 기본 CRUD 기능 관련
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClubBasicServiceImpl implements ClubBasicService {
	private static final String LOCATION_CODE_PATTERN = "\\d{10}";

	private final ClubRepository clubRepository;
	private final ClubAssetRepository clubAssetRepository;
	private final ClubMemberRepository clubMemberRepository;
	private final ClubJoinRequestRepository clubJoinRequestRepository;
	private final ClubSearchRepositoryCustom clubSearchRepositoryCustom;
	private final ClubTotalHikingStatisticRepository clubTotalHikingStatisticRepository;
	private final BaseAddressInfoRepository baseAddressInfoRepository;
	private final MemberRepository memberRepository;
	@Value("${values.asset.defaultAsset}")
	private String defaultAssetUrl;
	private AssetInfo defaultAsset;

	@PostConstruct
	private void postConstruct() {
		defaultAsset = new AssetInfo(null, "기본 에셋", defaultAssetUrl, "기본 에셋", 0);
	}

	@Transactional
	@Override
	public CustomSlice<ClubSearchRes> findClubList(String query, String word, Long clubId, Pageable pageable) {
		if (!(query.equals("") && word.equals("") ||
			query.equals("name") ||
			query.equals("location") && word.matches(LOCATION_CODE_PATTERN))) {
			log.error("ClubBasicService:findClubList: Invalid Query {}, Word {}", query, word);
			throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE);
		}

		final Slice<ClubSearchRes> result;
		if (query.equals("location")) {
			result = clubSearchRepositoryCustom.findClubsByLocation(word, clubId, pageable);
		} else {
			result = clubSearchRepositoryCustom.findClubsByName(word, clubId, pageable);
		}

		return new CustomSlice<>(result);
	}

	@Transactional
	@Override
	public Long addClub(String email, ClubInfoReq clubInfoReq) {
		final Member host = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));

		checkDuplicateClubName(clubInfoReq.getName());

		// 소모임 가입 여부 확인. 이미 가입된 경우, 소모임 생성 X
		if (clubMemberRepository.findByMemberId(host.getId()).isPresent())
			throw new GlobalException(ErrorCode.CLUB_ALREADY_JOINED);

		// 소모임 신청 취소
		clubJoinRequestRepository.updatePendingJoinRequestByMember(host, JoinRequestStatusType.RETRACTED,
			LocalDateTime.now());

		final BaseAddressInfo baseAddressInfo = getBaseAddressInfo(clubInfoReq);

		Club club = Club.builder()
			.host(host)
			.name(clubInfoReq.getName())
			.description(clubInfoReq.getDescription())
			.baseAddress(baseAddressInfo)
			.build();

		final Club savedclub = clubRepository.save(club);
		clubTotalHikingStatisticRepository.save(ClubTotalHikingStatistic.builder()
			.club(savedclub)
			.build());

		clubMemberRepository.save(ClubMember.builder()
			.member(host)
			.club(club)
			.build());

		return club.getId();
	}

	@Transactional
	@Override
	public void modifyClub(String email, Long clubId, ClubInfoReq clubInfoReq) {
		final Member host = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));

		final Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ErrorCode.CLUB_NOT_FOUND));

		if (!club.getHost().getId().equals(host.getId())) {
			log.error("ClubBasicService:modifyClub: hostId is not equal, {} {}", host.getId(), club.getHost().getId());
			throw new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED);
		}

		// 소모임 이름 체크
		if (!(club.getName().equals(clubInfoReq.getName()))) {
			checkDuplicateClubName(clubInfoReq.getName());
		}

		// 지역코드 체크
		BaseAddressInfo baseAddressInfo;
		if (!club.getBaseAddress().getDongCode().equals(clubInfoReq.getDongCode())) {
			baseAddressInfo = getBaseAddressInfo(clubInfoReq);
		} else {
			baseAddressInfo = club.getBaseAddress();
		}

		if (!updateClub(clubId, clubInfoReq, baseAddressInfo))
			throw new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	@Transactional
	@Override
	public ClubSimpleDetailRes findClubSimpleDetail(Long clubId) {
		final Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ErrorCode.CLUB_NOT_FOUND));

		return ClubSimpleDetailRes.builder()
			.hostId(club.getHost().getId())
			.clubId(clubId)
			.clubName(club.getName())
			.build();
	}

	@Transactional
	@Override
	public void checkDuplicateClubName(String clubName) {
		if (clubRepository.findByName(clubName).isPresent()) {
			throw new GlobalException(ErrorCode.DUPLICATE_CLUB_NAME);
		}
	}

	@Transactional
	@Override
	public ClubDetailRes findClubDetail(String email, Long clubId) {
		final Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));
		final Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ErrorCode.CLUB_NOT_FOUND));
		final ClubMember clubMember = clubMemberRepository.findByClubIdAndMemberId(club.getId(), member.getId())
			.orElse(null);

		List<ClubAsset> clubAssetList = clubAssetRepository.findAllByClubId(club.getId());

		// 기본 땅 에셋 추가하기
		ClubAsset defaultClubAsset = ClubAsset.builder()
			.club(club)
			.asset(defaultAsset)
			.meetup(null)
			.rowIndex(0)
			.colIndex(0)
			.build();
		clubAssetList.add(0, defaultClubAsset);
		log.info(String.valueOf(defaultClubAsset));

		return new ClubDetailRes(clubMember != null, club, clubAssetList);
	}

	@Transactional
	public boolean updateClub(Long clubId, ClubInfoReq clubInfoReq, BaseAddressInfo baseAddressInfo) {
		return clubRepository.updateClubProfile(clubId, clubInfoReq.getName(), clubInfoReq.getDescription(),
			baseAddressInfo, LocalDateTime.now()) > 0;
	}

	@Transactional
	public BaseAddressInfo getBaseAddressInfo(ClubInfoReq clubInfoReq) {
		if (clubInfoReq.getDongCode() != null) {
			return baseAddressInfoRepository.findById(clubInfoReq.getDongCode())
				.orElseThrow(() -> new GlobalException(ErrorCode.BASE_ADDRESS_NOT_FOUND));
		}
		return baseAddressInfoRepository.findById("0000000000") // 전국
			.orElseThrow(() -> new GlobalException(ErrorCode.BASE_ADDRESS_NOT_FOUND));
	}
}
