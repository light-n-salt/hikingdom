package org.lightnsalt.hikingdom.service.club.service.meetup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lightnsalt.hikingdom.common.dto.CustomSlice;
import org.lightnsalt.hikingdom.common.error.ErrorCode;
import org.lightnsalt.hikingdom.common.error.GlobalException;
import org.lightnsalt.hikingdom.common.util.S3FileUtil;
import org.lightnsalt.hikingdom.service.club.dto.response.meetup.MeetupAlbumRes;
import org.lightnsalt.hikingdom.domain.entity.club.Club;
import org.lightnsalt.hikingdom.domain.entity.club.meetup.Meetup;
import org.lightnsalt.hikingdom.domain.entity.club.meetup.MeetupAlbum;
import org.lightnsalt.hikingdom.service.club.repository.ClubRepository;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupAlbumRepository;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupAlbumRepositoryCustom;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupMemberRepository;
import org.lightnsalt.hikingdom.service.club.repository.meetup.MeetupRepository;
import org.lightnsalt.hikingdom.domain.entity.member.Member;
import org.lightnsalt.hikingdom.service.member.repository.MemberRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MeetupAlbumServiceImpl implements MeetupAlbumService {
	private final MeetupRepository meetupRepository;
	private final ClubRepository clubRepository;
	private final MemberRepository memberRepository;
	private final MeetupMemberRepository meetupMemberRepository;
	private final S3FileUtil s3FileUtil;
	private final MeetupAlbumRepository meetupAlbumRepository;

	private final MeetupAlbumRepositoryCustom meetupRepositoryCustom;

	@Override
	@Transactional
	public List<String> saveMeetupAlbum(String email, Long clubId, Long meetupId, List<MultipartFile> photos) {
		// meetup에 가입되어있는 회원인지 확인
		final Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_FOUND));

		// 모임 데이터 가져오기
		final Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ErrorCode.CLUB_NOT_FOUND));

		// 일정 데이터 가져오기
		final Meetup meetup = meetupRepository.findById(meetupId)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEETUP_NOT_FOUND));

		final boolean isExit = meetupMemberRepository.existsByMeetupIdAndMemberId(meetupId, member.getId());
		if (!isExit) {
			throw new GlobalException(ErrorCode.MEETUP_MEMBER_UNAUTHORIZED);
		}

		// DB에 사진 url 저장
		List<String> result = new ArrayList<>();
		List<MeetupAlbum> albums = new ArrayList<>();
		photos.forEach(photo -> {
			try {
				String url = s3FileUtil.upload(photo, "album/" + clubId + "/" + meetupId);
				result.add(url);
				albums.add(MeetupAlbum.builder()
					.club(club)
					.meetup(meetup)
					.member(member)
					.imgUrl(url)
					.build());
			} catch (IOException e) {
				throw new GlobalException(ErrorCode.FAIL_TO_SAVE_PHOTO);
			}
		});
		meetupAlbumRepository.saveAll(albums);

		return result;
	}

	@Override
	@Transactional
	public CustomSlice<MeetupAlbumRes> findMeetupAlbumList(String email, Long clubId, Long meetupId, Long photoId,
		Pageable pageable) {
		// 회원 정보 가져오기
		final Long memberId = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED)).getId();
		// 사진 정보 가져오기
		Slice<MeetupAlbum> list = meetupRepositoryCustom.findPhotos(photoId, meetupId, pageable);

		// 형 변환
		return new CustomSlice<>(
			list.map(meetupAlbum -> new MeetupAlbumRes(meetupAlbum, memberId)));
	}

	@Override
	@Transactional
	public void removeMeetupAlbum(String email, Long clubId, Long meetupId, Long photoId) {
		// 사진을 올린 사용자인지 확인
		final Long memberId = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED)).getId();

		// 사진이 존재하는지 확인
		final MeetupAlbum album = meetupAlbumRepository.findById(photoId)
			.orElseThrow(() -> new GlobalException(ErrorCode.PHOTO_NOT_FOUND));

		// 사진을 올리지 않은 사람이 삭제 요청을 하면 인증자격 유효 X
		if (!album.getMember().getId().equals(memberId)) {
			throw new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED);
		}

		meetupAlbumRepository.deleteById(photoId);
	}
}
