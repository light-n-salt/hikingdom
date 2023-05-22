package org.lightnsalt.hikingdom.service.hiking.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.lightnsalt.hikingdom.common.error.ErrorCode;
import org.lightnsalt.hikingdom.common.error.GlobalException;
import org.lightnsalt.hikingdom.domain.entity.club.Club;
import org.lightnsalt.hikingdom.domain.entity.club.ClubAsset;
import org.lightnsalt.hikingdom.domain.entity.club.ClubMember;
import org.lightnsalt.hikingdom.domain.entity.club.meetup.Meetup;
import org.lightnsalt.hikingdom.domain.entity.hiking.MemberHiking;
import org.lightnsalt.hikingdom.domain.entity.hiking.MemberHikingGps;
import org.lightnsalt.hikingdom.domain.entity.info.AssetInfo;
import org.lightnsalt.hikingdom.domain.entity.info.MountainInfo;
import org.lightnsalt.hikingdom.domain.entity.member.Member;
import org.lightnsalt.hikingdom.domain.entity.member.MemberHikingStatistic;
import org.lightnsalt.hikingdom.domain.entity.member.MemberLevelInfo;
import org.lightnsalt.hikingdom.service.club.repository.ClubAssetRepository;
import org.lightnsalt.hikingdom.service.club.repository.ClubMemberRepository;
import org.lightnsalt.hikingdom.service.club.repository.ClubRepository;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupMemberRepository;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupRepository;
import org.lightnsalt.hikingdom.service.hiking.dto.request.GpsRoute;
import org.lightnsalt.hikingdom.service.hiking.dto.request.HikingRecordReq;
import org.lightnsalt.hikingdom.service.hiking.dto.response.TodayMeetupRes;
import org.lightnsalt.hikingdom.service.hiking.repository.HikingRepositoryCustom;
import org.lightnsalt.hikingdom.service.hiking.repository.MemberHikingGpsRepository;
import org.lightnsalt.hikingdom.service.hiking.repository.MemberHikingRepository;
import org.lightnsalt.hikingdom.service.info.repository.AssetInfoRepository;
import org.lightnsalt.hikingdom.service.info.repository.MountainInfoRepository;
import org.lightnsalt.hikingdom.service.member.repository.MemberHikingStatisticRepository;
import org.lightnsalt.hikingdom.service.member.repository.MemberLevelInfoRepository;
import org.lightnsalt.hikingdom.service.member.repository.MemberRepository;
import org.lightnsalt.hikingdom.service.notification.dto.event.CreateClubAssetNotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class HikingServiceImpl implements HikingService {

	private final ClubRepository clubRepository;
	private final MeetupRepository meetupRepository;
	private final MeetupMemberRepository meetupMemberRepository;
	private final MemberRepository memberRepository;
	private final MemberHikingRepository memberHikingRepository;
	private final MemberHikingGpsRepository memberHikingGpsRepository;
	private final HikingRepositoryCustom hikingRepositoryCustom;
	private final MountainInfoRepository mountainInfoRepository;
	private final MemberHikingStatisticRepository memberHikingStatisticRepository;
	private final MemberLevelInfoRepository memberLevelInfoRepository;
	private final ClubMemberRepository clubMemberRepository;
	private final ClubAssetRepository clubAssetRepository;
	private final AssetInfoRepository assetInfoRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	@Transactional
	public List<TodayMeetupRes> findTodayMeetup(String email) {
		// 회원 확인
		final Long memberId = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND))
			.getId();

		final List<Meetup> meetups = hikingRepositoryCustom.findTodayMeetup(memberId);

		return meetups.stream().map(meetup -> {
			TodayMeetupRes dto = new TodayMeetupRes(meetup);
			// 일정 참여 멤버 가져오기
			final int totalMember = meetupMemberRepository.countByMeetupId(meetup.getId());
			dto.setTotalMember(totalMember);
			return dto;
		}).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public Long saveHikingRecord(String email, HikingRecordReq hikingRecordReq) {
		log.info("request is : {}", hikingRecordReq);
		final Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));

		final MountainInfo mountainInfo = mountainInfoRepository.findById(hikingRecordReq.getMountainId())
			.orElseThrow(() -> new GlobalException(ErrorCode.MOUNTAIN_NOT_FOUND));

		LocalDateTime startAt = LocalDateTime.parse(hikingRecordReq.getStartAt(),
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		LocalDateTime endAt = startAt.plusSeconds(hikingRecordReq.getTotalDuration());

		Meetup meetup;
		MemberHiking memberHiking;

		// 개인 등산인지 소모임 일정 등산인지 확인
		if (hikingRecordReq.getIsMeetup()) {  // 소모임 일정 등산
			meetup = meetupRepository.findById(hikingRecordReq.getMeetupId())
				.orElseThrow(() -> new GlobalException(ErrorCode.MEETUP_NOT_FOUND));

			// 개인 등산이 아닌 소모임 일정 등산일 경우, 소모임 통계 및 소모임 asset 업데이트
			// 속한 소모임이 있는지 체크
			ClubMember clubMember = clubMemberRepository.findByMemberId(member.getId())
				.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_HAS_NO_CLUBS));

			// 사용자가 속해있는 소모임과 request로 들어온 소모임이 일치하는지 체크
			Club club = meetup.getClub(); // request로 들어온 소모임
			if (!clubMember.getClub().getId().equals(club.getId()))
				throw new GlobalException(ErrorCode.CLUB_MEMBER_UNAUTHORIZED);

			memberHiking = MemberHiking.builder()
				.member(member)
				.mountain(mountainInfo)
				.meetup(meetup)
				.isMeetup(hikingRecordReq.getIsMeetup())
				.startAt(startAt)
				.endAt(endAt)
				.totalDuration((long)hikingRecordReq.getTotalDuration())
				.totalDistance((long)hikingRecordReq.getTotalDistance())
				.totalAlt((long)hikingRecordReq.getMaxAlt())
				.isSummit(hikingRecordReq.getIsSummit())
				.build();

			if (hikingRecordReq.getIsSummit()) {  // 완등 시
				// ClubTotalHikingStatistic 업데이트

				// ClubAsset 업데이트
				ClubAsset clubAsset = clubAssetRepository.findByMeetupId(hikingRecordReq.getMeetupId());
				if (clubAsset == null) { // clubAsset이 없으면 이 데이터는 이 일정에서 처음으로 완등한 데이터임. clubAsset 생성
					AssetInfo assetInfo = assetInfoRepository.findByMountainId(mountainInfo.getId());

					if (assetInfo == null)
						throw new GlobalException(ErrorCode.ASSET_NOT_FOUND);

					// 전에 완등한 산이었는지 확인
					boolean isNewMountain = !clubAssetRepository.existsByClubIdAndAssetId(club.getId(),
						assetInfo.getId());

					clubAssetRepository.save(ClubAsset.builder()
						.club(club)
						.asset(assetInfo)
						.meetup(meetup)
						.build());

					clubRepository.updateClubMountainCountAndAssetCountAndScore(club.getId(),
						isNewMountain ? club.getTotalMountainCount() + 1 :
							club.getTotalMountainCount(), club.getTotalAssetCount() + 1,
						club.getScore() + assetInfo.getScore(), LocalDateTime.now());

					// 비동기 알림
					eventPublisher.publishEvent(new CreateClubAssetNotificationEvent(
						clubMemberRepository.findByClubId(club.getId()),
						assetInfo,
						club.getId()
					));
				} else {
					// 이미 완등한 사용자가 한 명 이상이여서 에셋이 이미 발급된 경우 PASS
					log.info("이미 완등한 사용자가 한 명 이상이여서 에셋이 이미 발급된 경우");
				}
			}
		} else {  // 개인 등산일 경우
			memberHiking = MemberHiking.builder()
				.member(member)
				.mountain(mountainInfo)
				.isMeetup(hikingRecordReq.getIsMeetup())
				.startAt(startAt)
				.endAt(endAt)
				.totalDuration((long)hikingRecordReq.getTotalDuration())
				.totalDistance((long)hikingRecordReq.getTotalDistance())
				.totalAlt((long)hikingRecordReq.getMaxAlt())
				.isSummit(hikingRecordReq.getIsSummit())
				.build();
		}

		//        hikingRecordReq.getGpsRoute()
		JsonObject gpsRoute = new JsonObject();

		//        JsonArray jsonArray = new JsonArray();
		//        jsonArray.add(hikingRecordReq.getGpsRoute());
		JsonArray jsonArray = new JsonArray();
		for (GpsRoute gps : hikingRecordReq.getGpsRoute()) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("lat", gps.getLat());
			jsonObject.addProperty("lng", gps.getLng());
			jsonObject.addProperty("alt", gps.getAlt());
			jsonArray.add(jsonObject);
		}
		//        Map<String, Object> map = new HashMap<>();
		//        map.put("gpsRoute", jsonArray);
		gpsRoute.add("gpsRoute", jsonArray);
		MemberHikingGps memberHikingGps = MemberHikingGps.builder()
			.hiking(memberHiking)
			.gpsRoute(getMapFromJsonObject(gpsRoute))
			.build();

		final MemberHiking savedMemberHiking = memberHikingRepository.save(memberHiking);
		final MemberHikingGps savedMemberHikingGps = memberHikingGpsRepository.save(memberHikingGps);

		// MemberHikingStatistic 업데이트
		int isSummit = memberHiking.getIsSummit() ? 1 : 0;  // 완등 시 toatlMountainCount + 1

		MemberHikingStatistic memberHikingStatistic = memberHikingStatisticRepository.findById(member.getId())
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_HIKING_STATISTIC_NOT_FOUND));
		//        log.info("memberHikingStatistic : {}", memberHikingStatistic);
		memberHikingStatistic.updateMemberHikingStatistic(member, memberHikingStatistic.getTotalHikingCount() + 1,
			memberHikingStatistic.getTotalMountainCount() + isSummit,
			memberHikingStatistic.getTotalDuration() + savedMemberHiking.getTotalDuration(),
			memberHikingStatistic.getTotalDistance() + savedMemberHiking.getTotalDistance(),
			memberHikingStatistic.getTotalAlt() + savedMemberHiking.getTotalAlt());
		//        log.info("memberHikingStatistic : {}", memberHikingStatistic);

		// Level 업데이트
		int originLevel = member.getLevel().getId();
		int newLevel = originLevel;

		Long totalMountainCount = memberHikingStatistic.getTotalMountainCount();
		List<MemberLevelInfo> memberLevelInfos = memberLevelInfoRepository.findAll();

		for (int i = 0; i < memberLevelInfos.size(); i++) {
			MemberLevelInfo currentLevelInfo = memberLevelInfos.get(i);
			int currentLevelCondition = memberLevelInfos.get(i).getLevelCondition();

			if (totalMountainCount >= currentLevelCondition) {
				if (i == memberLevelInfos.size() - 1 || totalMountainCount < memberLevelInfos.get(i + 1)
					.getLevelCondition()) {
					newLevel = currentLevelInfo.getId();
					break;
				}
			}
		}

		if (newLevel != originLevel) {
			MemberLevelInfo memberLevelInfo = memberLevelInfoRepository.findById(newLevel)
				.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_LEVEL_INFO_NOT_FOUND));
			member.updateMemberLevel(memberLevelInfo);
		}
		return savedMemberHiking.getId();
	}

	public static Map<String, Object> getMapFromJsonObject(JsonObject jsonObj) {
		Map<String, Object> map = null;

		try {
			map = new ObjectMapper().readValue(jsonObj.toString(), Map.class);
		} catch (JsonParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
}
