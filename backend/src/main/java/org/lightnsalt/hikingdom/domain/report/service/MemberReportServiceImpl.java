package org.lightnsalt.hikingdom.domain.report.service;

import java.time.LocalDateTime;

import org.lightnsalt.hikingdom.common.error.ErrorCode;
import org.lightnsalt.hikingdom.common.error.GlobalException;
import org.lightnsalt.hikingdom.domain.club.entity.meetup.MeetupAlbum;
import org.lightnsalt.hikingdom.domain.club.entity.meetup.MeetupReview;
import org.lightnsalt.hikingdom.domain.club.repository.MeetupAlbumRepository;
import org.lightnsalt.hikingdom.domain.club.repository.MeetupReviewRepository;
import org.lightnsalt.hikingdom.domain.member.entity.Member;
import org.lightnsalt.hikingdom.domain.member.repository.MemberRepository;
import org.lightnsalt.hikingdom.domain.report.dto.MemberReportReq;
import org.lightnsalt.hikingdom.domain.report.entity.MemberReport;
import org.lightnsalt.hikingdom.domain.report.repository.MemberReportRepository;
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
		final Member reporter = memberRepository.findByEmailAndIsWithdraw(email, false)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));

		// 신고하려는 사용자 조회
		final Member reported;

		switch (req.getType()) {
			case "ALBUM":
				final MeetupAlbum meetupAlbum = meetupAlbumRepository.findById(req.getContentId())
					.orElseThrow(() -> new GlobalException(ErrorCode.PHOTO_NOT_FOUND));

				reported = memberRepository.findById(meetupAlbum.getMember().getId())
					.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
				break;
			case "REVIEW":
				final MeetupReview meetupReview = meetupReviewRepository.findById(req.getContentId())
					.orElseThrow(() -> new GlobalException(ErrorCode.PHOTO_NOT_FOUND));

				reported = memberRepository.findById(meetupReview.getMember().getId())
					.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));
				break;
			default:
				throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE);
		}

		// 신고내용 저장
		final MemberReport memberReport = MemberReport.builder()
			.reporter(reporter)
			.reported(reported)
			.reportedAt(LocalDateTime.now())
			.reportType(req.getType())
			.reportedContent(req.getContentId())
			.build();

		return memberReportRepository.save(memberReport).getId();

	}
}
