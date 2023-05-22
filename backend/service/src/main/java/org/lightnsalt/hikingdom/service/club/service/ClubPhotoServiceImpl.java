package org.lightnsalt.hikingdom.service.club.service;

import org.lightnsalt.hikingdom.common.dto.CustomSlice;
import org.lightnsalt.hikingdom.common.error.ErrorCode;
import org.lightnsalt.hikingdom.common.error.GlobalException;
import org.lightnsalt.hikingdom.domain.entity.club.meetup.MeetupAlbum;
import org.lightnsalt.hikingdom.service.club.dto.response.meetup.MeetupAlbumRes;
import org.lightnsalt.hikingdom.service.club.repository.ClubMemberRepository;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupAlbumRepository;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupAlbumRepositoryCustom;
import org.lightnsalt.hikingdom.service.member.repository.MemberRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClubPhotoServiceImpl implements ClubPhotoService {
	private final MemberRepository memberRepository;
	private final ClubMemberRepository clubMemberRepository;
	private final MeetupAlbumRepository meetupAlbumRepository;
	private final MeetupAlbumRepositoryCustom meetupRepositoryCustom;

	@Transactional
	@Override
	public CustomSlice<MeetupAlbumRes> findClubPhotoList(String email, Long clubId, Long photoId, Pageable pageable) {
		final Long memberId = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED)).getId();

		// 소모임 가입 여부 확인
		if (clubMemberRepository.findByClubIdAndMemberId(clubId, memberId).isEmpty())
			throw new GlobalException(ErrorCode.CLUB_MEMBER_UNAUTHORIZED);

		Slice<MeetupAlbum> list = meetupRepositoryCustom.findPhotosByClubId(photoId, clubId, pageable);
		return new CustomSlice<>(
			list.map(meetupAlbum -> new MeetupAlbumRes(meetupAlbum, memberId)));
	}

	@Transactional
	@Override
	public void removeClubPhoto(String email, Long clubId, Long photoId) {
		final Long memberId = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED)).getId();

		// 소모임 가입 여부 확인
		if (clubMemberRepository.findByClubIdAndMemberId(clubId, memberId).isEmpty())
			throw new GlobalException(ErrorCode.CLUB_MEMBER_UNAUTHORIZED);

		final MeetupAlbum meetupAlbum = meetupAlbumRepository.findById(photoId)
			.orElseThrow(() -> new GlobalException(ErrorCode.PHOTO_NOT_FOUND));

		if (!meetupAlbum.getMember().getId().equals(memberId))
			throw new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED);

		meetupAlbumRepository.deleteById(photoId);
	}
}
