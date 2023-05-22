package org.lightnsalt.hikingdom.service.report.service;

import java.time.LocalDateTime;

import org.lightnsalt.hikingdom.common.error.ErrorCode;
import org.lightnsalt.hikingdom.common.error.GlobalException;
import org.lightnsalt.hikingdom.domain.entity.club.meetup.MeetupAlbum;
import org.lightnsalt.hikingdom.domain.entity.club.meetup.MeetupReview;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupAlbumRepository;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupReviewRepository;
import org.lightnsalt.hikingdom.domain.entity.member.Member;
import org.lightnsalt.hikingdom.service.member.repository.MemberRepository;
import org.lightnsalt.hikingdom.service.report.dto.MemberReportReq;
import org.lightnsalt.hikingdom.domain.entity.report.MemberReport;
import org.lightnsalt.hikingdom.service.report.repository.MemberReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberReportServiceImpl implements MemberReportService {
	private final MemberReportRepository memberReportRepository;
	private final MeetupAlbumRepository meetupAlbumRepository;
	private final MemberRepository memberRepository;
	private final MeetupReviewRepository meetupReviewRepository;

	@Override
	@Transactional
	public Long saveMemberReport(String email, MemberReportReq req) {
		// 신고하는 사용자 조회
		final Member reporter = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));

		// 신고하려는 사용자 조회
		final Member reported;

		switch (req.getType()) {
			case "ALBUM":
				final MeetupAlbum meetupAlbum = meetupAlbumRepository.findById(Long.parseLong(req.getId()))
					.orElseThrow(() -> new GlobalException(ErrorCode.PHOTO_NOT_FOUND));

				reported = memberRepository.findById(meetupAlbum.getMember().getId())
					.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
				break;
			case "REVIEW":
				final MeetupReview meetupReview = meetupReviewRepository.findById(Long.parseLong(req.getId()))
					.orElseThrow(() -> new GlobalException(ErrorCode.PHOTO_NOT_FOUND));

				reported = memberRepository.findById(meetupReview.getMember().getId())
					.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
				break;
			case "MEMBER":
				reported = memberRepository.findByNickname(req.getId())
					.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
				break;
			default:
				throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE);
		}

		// 신고자가 신고할 사람과 같은지 확인
		if (reporter.equals(reported))
			throw new GlobalException(ErrorCode.SAME_REPORTER_AND_REPORTED);

		// 이미 신고된 내역인지 확인
		if (memberReportRepository.existsByReportTypeAndReporterIdAndReportedId(req.getType(), reporter.getId(),
			reported.getId()))
			throw new GlobalException(ErrorCode.ALREADY_REPORTED);
		
		
		// 신고내용 저장
		final MemberReport memberReport = MemberReport.builder()
			.reporter(reporter)
			.reported(reported)
			.reportedAt(LocalDateTime.now())
			.reportType(req.getType())
			.reportedContent(req.getId())
			.build();

		return memberReportRepository.save(memberReport).getId();

	}
}
