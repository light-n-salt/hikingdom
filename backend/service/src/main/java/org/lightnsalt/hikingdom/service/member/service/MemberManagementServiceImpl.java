package org.lightnsalt.hikingdom.service.member.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.lightnsalt.hikingdom.common.error.ErrorCode;
import org.lightnsalt.hikingdom.common.error.GlobalException;
import org.lightnsalt.hikingdom.common.util.JwtTokenUtil;
import org.lightnsalt.hikingdom.common.util.RedisUtil;
import org.lightnsalt.hikingdom.common.util.S3FileUtil;
import org.lightnsalt.hikingdom.domain.common.enumType.JoinRequestStatusType;
import org.lightnsalt.hikingdom.domain.entity.club.ClubJoinRequest;
import org.lightnsalt.hikingdom.domain.entity.club.ClubMember;
import org.lightnsalt.hikingdom.domain.entity.club.meetup.MeetupMember;
import org.lightnsalt.hikingdom.domain.entity.hiking.MemberHiking;
import org.lightnsalt.hikingdom.domain.entity.member.Member;
import org.lightnsalt.hikingdom.domain.entity.member.MemberHikingStatistic;
import org.lightnsalt.hikingdom.service.club.repository.ClubJoinRequestRepository;
import org.lightnsalt.hikingdom.service.club.repository.ClubMemberRepository;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupMemberRepository;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupRepository;
import org.lightnsalt.hikingdom.service.club.repository.record.ClubRankingRepository;
import org.lightnsalt.hikingdom.service.hiking.dto.response.HikingRecordRes;
import org.lightnsalt.hikingdom.service.hiking.repository.MemberHikingRepository;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberChangePasswordReq;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberLogoutReq;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberNicknameReq;
import org.lightnsalt.hikingdom.service.member.dto.response.MemberDetailRes;
import org.lightnsalt.hikingdom.service.member.dto.response.MemberInfoRes;
import org.lightnsalt.hikingdom.service.member.dto.response.MemberProfileRes;
import org.lightnsalt.hikingdom.service.member.dto.response.MemberRequestClubRes;
import org.lightnsalt.hikingdom.service.member.repository.MemberFcmTokenRepository;
import org.lightnsalt.hikingdom.service.member.repository.MemberHikingStatisticRepository;
import org.lightnsalt.hikingdom.service.member.repository.MemberRepository;
import org.lightnsalt.hikingdom.service.notification.repository.NotificationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberManagementServiceImpl implements MemberManagementService {
	private final ClubRankingRepository clubRankingRepository;
	private final MemberHikingRepository memberHikingRepository;
	private final MemberHikingStatisticRepository memberHikingStatisticRepository;
	private final PasswordEncoder passwordEncoder;

	private final S3FileUtil s3FileUtil;
	private final RedisUtil redisUtil;
	private final JwtTokenUtil jwtTokenUtil;

	private final ClubJoinRequestRepository clubJoinRequestRepository;
	private final ClubMemberRepository clubMemberRepository;
	private final MeetupRepository meetupRepository;
	private final MeetupMemberRepository meetupMemberRepository;
	private final MemberRepository memberRepository;
	private final MemberFcmTokenRepository memberFcmTokenRepository;
	private final NotificationRepository notificationRepository;

	private final RestTemplate restTemplate;

	@Override
	public MemberDetailRes findMemberInfo(String email) {
		final Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));
		final ClubMember clubMember = clubMemberRepository.findByMemberId(member.getId())
			.orElse(null);

		return MemberDetailRes.builder()
			.memberId(member.getId())
			.email(member.getEmail())
			.nickname(member.getNickname())
			.profileUrl(member.getProfileUrl())
			.clubId(clubMember != null ? clubMember.getClub().getId() : null)
			.level(member.getLevel().getId())
			.build();
	}

	@Transactional
	@Override
	public void removeMember(String email) {
		final Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));
		final ClubMember clubMember = clubMemberRepository.findByMemberId(member.getId())
			.orElse(null);

		LocalDateTime now = LocalDateTime.now();

		if (clubMember != null) {
			// 소모임 모임장 또는 진행 중인 일정이 있을 시 탈퇴 불가능
			if (clubMember.getClub().getHost().getId().equals(member.getId())) {
				throw new GlobalException(ErrorCode.CLUB_MEMBER_IS_HOST);
			}

			if (!meetupRepository.findByClubIdAndHostIdAndStartAtAfter(clubMember.getClub().getId(), member.getId(),
				LocalDateTime.now()).isEmpty()) {
				throw new GlobalException(ErrorCode.CLUB_MEMBER_HOST_MEETUP_EXISTS);
			}

			// 참여하고 있는 일정이 있을 시, 자동 참여 취소
			List<MeetupMember> participatingEvents = meetupMemberRepository.findByMemberIdAndStartAtAfter(
				member.getId(), now);
			meetupMemberRepository.deleteAll(participatingEvents);

			// 소모임 탈퇴
			clubMemberRepository.deleteById(clubMember.getId());

			// 소모임 회원 목록 변경된 것 채팅 서비스로 전달
			List<MemberInfoRes> members = clubMemberRepository.findByClubIdReturnMemberInfoRes(
				clubMember.getClub().getId());
			sendMemberUpdateAlert(clubMember.getClub().getId(), members);
		}

		// 소모임 가입 신청 취소
		clubJoinRequestRepository.updatePendingJoinRequestByMember(member, JoinRequestStatusType.RETRACTED,
			now);

		// fcm token 삭제
		memberFcmTokenRepository.deleteAllByMemberId(member.getId());

		memberRepository.updateMemberWithdraw(member.getId(), true, now);
	}

	@Transactional
	@Override
	public void logout(String bearerToken, MemberLogoutReq memberLogoutReq) {
		String accessToken = jwtTokenUtil.resolveToken(bearerToken);
		String email = jwtTokenUtil.getEmailFromToken(accessToken);

		final Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));

		// FCM Token 존재할 시 삭제
		if (memberLogoutReq != null && memberLogoutReq.getFcmToken() != null) {
			memberFcmTokenRepository.deleteByMemberIdAndBody(member.getId(), memberLogoutReq.getFcmToken());
		}

		// redis에서 refresh token 삭제
		if (redisUtil.getValue("RT" + email) != null) {
			redisUtil.deleteValue("RT" + email);
		}

		// 기존 access token 블랙리스트
		Long expiration = jwtTokenUtil.getExpirationFromToken(accessToken);
		redisUtil.setValueWithExpiration(accessToken, "logout", expiration);
	}

	@Transactional
	@Override
	public void changePassword(String email, MemberChangePasswordReq memberChangePasswordReq) {
		if (!memberChangePasswordReq.getCheckPassword().equals(memberChangePasswordReq.getNewPassword())) {
			throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE);
		}

		final Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));

		boolean isValidPassword = passwordEncoder.matches(memberChangePasswordReq.getPassword(), member.getPassword());

		if (!isValidPassword) {
			throw new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED);
		}

		memberRepository.setPasswordById(passwordEncoder.encode(memberChangePasswordReq.getNewPassword()),
			member.getId());
	}

	@Transactional
	@Override
	public void changeNickname(String email, MemberNicknameReq memberNicknameReq) {
		final Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));

		String newNickname = memberNicknameReq.getNickname();

		if (member.getNickname().equals(newNickname)) {
			return;
		}

		if (memberRepository.existsByNickname(newNickname)) {
			throw new GlobalException(ErrorCode.DUPLICATE_NICKNAME);
		}

		memberRepository.setNicknameById(newNickname, member.getId());

		// 소모임 회원 목록 변경된 것 채팅 서비스로 전달
		final ClubMember clubMember = clubMemberRepository.findByMemberId(member.getId())
			.orElse(null);

		if (clubMember != null) {
			List<MemberInfoRes> members = clubMemberRepository.findByClubIdReturnMemberInfoRes(
				clubMember.getClub().getId());
			sendMemberUpdateAlert(clubMember.getClub().getId(), members);
		}
	}

	@Transactional
	@Override
	public String changeProfileImage(String email, MultipartFile photo) {
		final Long memberId = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED))
			.getId();

		try {
			String url = s3FileUtil.upload(photo, "members/" + memberId + "/profiles");
			memberRepository.setProfileUrlById(url, memberId);

			// 소모임 회원 목록 변경된 것 채팅 서비스로 전달
			final ClubMember clubMember = clubMemberRepository.findByMemberId(memberId)
				.orElse(null);

			if (clubMember != null) {
				List<MemberInfoRes> members = clubMemberRepository.findByClubIdReturnMemberInfoRes(
					clubMember.getClub().getId());
				sendMemberUpdateAlert(clubMember.getClub().getId(), members);
			}

			return url;
		} catch (IOException e) {
			throw new GlobalException(ErrorCode.FAIL_TO_SAVE_PHOTO);
		}
	}

	@Transactional
	@Override
	public MemberProfileRes findProfile(String email, String nickname, Pageable pageable) {
		// 회원정보 가져오기
		final Member member = memberRepository.findByNickname(nickname)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
		// 회원 등산통계 가져오기
		final MemberHikingStatistic memberHikingStatistic = memberHikingStatisticRepository.findById(member.getId())
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));

		// 회원 등산기록 가져오기
		final List<MemberHiking> memberHikingList = memberHikingRepository.findAllByMemberIdOrderByEndAtDescStartAtDesc(
			member.getId(), pageable);
		List<HikingRecordRes> hikingRecordResList = memberHikingList.stream()
			.map(HikingRecordRes::new)
			.collect(Collectors.toList());

		// 본인일 경우, 안 읽은 알림 개수 리턴
		Integer unreadNotificationCount = null;
		if (email.equals(member.getEmail())) {
			unreadNotificationCount = notificationRepository.countAllByMemberIdAndIsRead(member.getId(), false);
		}

		// dto에 담아 리턴
		return new MemberProfileRes(member, memberHikingStatistic, hikingRecordResList, unreadNotificationCount);
	}

	@Transactional
	@Override
	public List<MemberRequestClubRes> findRequestClub(String email) {
		// 회원 정보 가져오기
		final Long memberId = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED))
			.getId();

		// 가입 신청한 소모임 정보 가져오기
		final List<ClubJoinRequest> clubMemberList = clubJoinRequestRepository.findByMemberIdAndStatus(
			memberId, JoinRequestStatusType.PENDING);

		// dto에 담아 리턴
		return clubMemberList.stream().map(clubJoinRequest -> {
			Long ranking = 0L;
			if (clubJoinRequest.getClub() != null) {
				var clubRanking = clubRankingRepository.findByClubIdAndSetDate(clubJoinRequest.getClub().getId(),
					LocalDate.now());
				if (clubRanking != null) {
					ranking = clubRanking.getRanking();
				}
			}

			assert clubJoinRequest.getClub() != null;
			return new MemberRequestClubRes(clubJoinRequest.getClub(), ranking);
		}).collect(Collectors.toList());
	}

	private void sendMemberUpdateAlert(Long clubId, List<MemberInfoRes> members) {
		restTemplate.postForEntity("https://hikingdom.kr/chat/clubs/" + clubId + "/member-update",
			members, MemberInfoRes.class);
	}
}