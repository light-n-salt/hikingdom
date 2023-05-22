package org.lightnsalt.hikingdom.service.club.service.meetup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.lightnsalt.hikingdom.common.error.ErrorCode;
import org.lightnsalt.hikingdom.common.error.GlobalException;
import org.lightnsalt.hikingdom.service.club.dto.response.meetup.MeetupMemberDetailListRes;
import org.lightnsalt.hikingdom.domain.entity.club.meetup.Meetup;
import org.lightnsalt.hikingdom.domain.entity.club.meetup.MeetupMember;
import org.lightnsalt.hikingdom.service.club.repository.ClubMemberRepository;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupMemberRepository;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupRepository;
import org.lightnsalt.hikingdom.domain.entity.member.Member;
import org.lightnsalt.hikingdom.service.member.repository.MemberRepository;
import org.lightnsalt.hikingdom.service.club.dto.response.meetup.MeetupMemberListRes;
import org.lightnsalt.hikingdom.service.club.dto.response.MemberShortRes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MeetupMemberServiceImpl implements MeetupMemberService {
	private final ClubMemberRepository clubMemberRepository;
	private final MemberRepository memberRepository;
	private final MeetupRepository meetupRepository;
	private final MeetupMemberRepository meetupMemberRepository;

	@Override
	@Transactional
	public void addJoinMeetup(String email, Long clubId, Long meetupId) {
		final Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));

		if (!clubMemberRepository.existsByClubIdAndMemberId(clubId, member.getId()))
			throw new GlobalException(ErrorCode.CLUB_MEMBER_UNAUTHORIZED);

		if (meetupMemberRepository.existsByMeetupIdAndMemberId(meetupId, member.getId()))
			throw new GlobalException(ErrorCode.MEETUP_ALREADY_JOINED);

		final Meetup meetup = meetupRepository.findById(meetupId)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEETUP_NOT_FOUND));

		if (meetup.getStartAt().isBefore(LocalDateTime.now()))
			throw new GlobalException(ErrorCode.MEETUP_ALREADY_DONE);

		meetupMemberRepository.save(MeetupMember.builder().meetup(meetup).member(member).build());
	}

	@Override
	@Transactional
	public void removeJoinMeetup(String email, Long clubId, Long meetupId) {
		final Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));
		final Meetup meetup = meetupRepository.findById(meetupId)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEETUP_NOT_FOUND));

		// 사용자가 생성한 일정인 경우, 참여취소 불가
		if (meetup.getHost().equals(member))
			throw new GlobalException(ErrorCode.MEETUP_IS_HOST);

		final MeetupMember meetupMember = meetupMemberRepository.findByMeetupIdAndMemberId(meetupId,
			member.getId()).orElseThrow(() -> new GlobalException(ErrorCode.MEETUP_NOT_JOINED));

		if (meetup.getStartAt().isBefore(LocalDateTime.now()))
			throw new GlobalException(ErrorCode.MEETUP_ALREADY_DONE);

		meetupMemberRepository.delete(meetupMember);
	}

	@Override
	@Transactional
	public List<MeetupMemberDetailListRes> findMeetupMemberDetail(Long clubId, Long meetupId) {
		// 형 변환
		return getMeetupMember(clubId, meetupId).stream()
			.map(meetupMember -> new MeetupMemberDetailListRes(meetupMember.getMember()))
			.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public MeetupMemberListRes findMeetupMember(String email, Long clubId, Long meetupId) {
		// 회원 ID 가져오기
		final Long memberId = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED)).getId();
		// 일정에 참여 중인지 확인
		final boolean isJoin = meetupMemberRepository.existsByMeetupIdAndMemberId(meetupId, memberId);

		// 일정 참여 멤버 수 조회하기
		final int totalMember = meetupMemberRepository.countByMeetupId(meetupId);

		// 멤버 정보 가져오기
		List<MemberShortRes> memberInfo = getMeetupMember(clubId, meetupId).stream()
			.map(MemberShortRes::new)
			.collect(Collectors.toList());
		return new MeetupMemberListRes(totalMember, isJoin, memberInfo);
	}

	private List<MeetupMember> getMeetupMember(Long clubId, Long meetupId) {
		// 존재하는 일정인지 확인
		final Meetup meetup = meetupRepository.findById(meetupId)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEETUP_NOT_FOUND));

		// 소모임에 포함된 일정인지 확인
		if (!clubId.equals(meetup.getClub().getId())) {
			throw new GlobalException(ErrorCode.CLUB_MEETUP_NOT_FOUND);
		}
		// 일정 멤버 조회
		return meetupMemberRepository.findByMeetupId(meetupId);
	}
}
