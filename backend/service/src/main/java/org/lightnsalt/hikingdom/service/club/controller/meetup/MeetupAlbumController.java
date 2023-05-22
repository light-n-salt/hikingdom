package org.lightnsalt.hikingdom.service.club.controller.meetup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lightnsalt.hikingdom.common.dto.BaseResponseBody;
import org.lightnsalt.hikingdom.common.dto.CustomResponseBody;
import org.lightnsalt.hikingdom.common.dto.CustomSlice;
import org.lightnsalt.hikingdom.service.club.dto.response.meetup.MeetupAlbumRes;
import org.lightnsalt.hikingdom.service.club.service.meetup.MeetupAlbumService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/clubs/{clubId}/meetups/{meetupId}/photos")
@RequiredArgsConstructor
public class MeetupAlbumController {

	private final MeetupAlbumService meetupAlbumService;

	@PostMapping("")
	public ResponseEntity<CustomResponseBody> meetupAlbumAdd(Authentication authentication, @PathVariable Long clubId,
		@PathVariable Long meetupId, @RequestBody List<MultipartFile> photos) {

		List<String> list = meetupAlbumService.saveMeetupAlbum(authentication.getName(), clubId, meetupId, photos);

		Map<String, List<String>> result = new HashMap<>();
		result.put("imgUrl", list);
		return new ResponseEntity<>(BaseResponseBody.of("일정 사진이 등록되었습니다", result), HttpStatus.CREATED);
	}

	@GetMapping("")
	public ResponseEntity<CustomResponseBody> meetupAlbumList(Authentication authentication, @PathVariable Long clubId,
		@PathVariable Long meetupId,
		@RequestParam(defaultValue = "") Long photoId, Pageable pageable) {

		CustomSlice<MeetupAlbumRes> result = meetupAlbumService.findMeetupAlbumList(authentication.getName(), clubId,
			meetupId, photoId, pageable);
		return new ResponseEntity<>(BaseResponseBody.of("일정 사진 조회에 성공했습니다", result), HttpStatus.OK);
	}

	@DeleteMapping("/{photoId}")
	public ResponseEntity<CustomResponseBody> meetupAlbumRemove(Authentication authentication,
		@PathVariable Long clubId, @PathVariable Long meetupId, @PathVariable Long photoId) {

		meetupAlbumService.removeMeetupAlbum(authentication.getName(), clubId, meetupId, photoId);
		return new ResponseEntity<>(BaseResponseBody.of("일정 사진이 삭제되었습니다"), HttpStatus.OK);
	}

}
