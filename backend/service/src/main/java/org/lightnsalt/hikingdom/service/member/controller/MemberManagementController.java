package org.lightnsalt.hikingdom.service.member.controller;

import java.util.List;

import javax.validation.Valid;

import org.lightnsalt.hikingdom.common.dto.BaseResponseBody;
import org.lightnsalt.hikingdom.common.dto.CustomResponseBody;
import org.lightnsalt.hikingdom.common.dto.ErrorResponseBody;
import org.lightnsalt.hikingdom.common.error.ErrorCode;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberChangePasswordReq;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberLogoutReq;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberNicknameReq;
import org.lightnsalt.hikingdom.service.member.dto.response.MemberDetailRes;
import org.lightnsalt.hikingdom.service.member.dto.response.MemberProfileRes;
import org.lightnsalt.hikingdom.service.member.dto.response.MemberRequestClubRes;
import org.lightnsalt.hikingdom.service.member.service.MemberManagementService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 관리와 관련된 API
 * JWT 토큰 인증 필요
 */
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberManagementController {
	private final MemberManagementService memberManagementService;

	/**
	 * 로그인한 사용자의 상세 정보 조회 요청을 처리한다.
	 *
	 * @param authentication 현재 인증된 사용자의 인증 객체
	 * @return 로그인한 사용자의 상세 정보를 포함한 응답
	 */
	@GetMapping
	public ResponseEntity<CustomResponseBody> memberInfoDetail(Authentication authentication) {
		MemberDetailRes memberDetailRes = memberManagementService.findMemberInfo(authentication.getName());
		return new ResponseEntity<>(BaseResponseBody.of("회원 정보 조회에 성공했습니다", memberDetailRes), HttpStatus.OK);
	}

	/**
	 * 사용자 탈퇴 요청을 처리한다.
	 *
	 * @param authentication 현재 인증된 사용자의 인증 객체
	 * @return 사용자 탈퇴 처리 상태에 대한 응답
	 */
	@DeleteMapping("/withdraw")
	public ResponseEntity<CustomResponseBody> memberRemove(Authentication authentication) {
		memberManagementService.removeMember(authentication.getName());

		return new ResponseEntity<>(BaseResponseBody.of("회원 탈퇴에 성공했습니다"), HttpStatus.OK);
	}

	/**
	 * 로그아웃 요청을 처리한다.
	 *
	 * @param bearerToken 사용자 인증을 위한 Bearer token
	 * @param memberLogoutReq 로그아웃 요청 객체
	 * @return 로그아웃 처리 상태에 대한 응답
	 */
	@PostMapping("/logout")
	public ResponseEntity<CustomResponseBody> logout(@RequestHeader("Authorization") String bearerToken,
		@RequestBody(required = false) MemberLogoutReq memberLogoutReq) {
		memberManagementService.logout(bearerToken, memberLogoutReq);

		return new ResponseEntity<>(BaseResponseBody.of("로그아웃에 성공했습니다"), HttpStatus.OK);
	}

	/**
	 * 비밀번호 변경 요청을 처리한다.
	 *
	 * @param authentication 현재 인증된 사용자의 인증 객체
	 * @param memberChangePasswordReq 비밀번호 변경 요청 객체
	 * @param bindingResult 비밀번호 변경 요청 객체에 대한 유효성 검사 결과
	 * @return 비밀번호 변경 요청 처리 상태에 대한 응답
	 */
	@PutMapping("/password-change")
	public ResponseEntity<CustomResponseBody> passwordChange(Authentication authentication,
		@RequestBody @Valid MemberChangePasswordReq memberChangePasswordReq, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return new ResponseEntity<>(ErrorResponseBody.of(ErrorCode.INVALID_INPUT_VALUE,
				bindingResult.getAllErrors().get(0).getDefaultMessage()),
				HttpStatus.BAD_REQUEST);
		}

		memberManagementService.changePassword(authentication.getName(), memberChangePasswordReq);

		return new ResponseEntity<>(BaseResponseBody.of("비밀번호 변경에 성공했습니다"), HttpStatus.OK);
	}

	/**
	 * 닉네임 변경 요청을 처리한다.
	 *
	 * @param authentication 현재 인증된 사용자의 인증 객체
	 * @param memberNicknameReq 닉네임 변경 요청 객체
	 * @param bindingResult 닉네임 변경 요청 객체에 대한 유효성 검사 결과
	 * @return 닉네임 변경 요청 처리 상태에 대한 응답
	 */
	@PutMapping("/nickname-change")
	public ResponseEntity<CustomResponseBody> nicknameChange(Authentication authentication,
		@RequestBody @Valid MemberNicknameReq memberNicknameReq, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return new ResponseEntity<>(ErrorResponseBody.of(ErrorCode.INVALID_INPUT_VALUE,
				bindingResult.getAllErrors().get(0).getDefaultMessage()),
				HttpStatus.BAD_REQUEST);
		}

		memberManagementService.changeNickname(authentication.getName(), memberNicknameReq);

		return new ResponseEntity<>(BaseResponseBody.of("닉네임 변경에 성공했습니다"), HttpStatus.OK);
	}

	/**
	 * 프로필 이미지 변경 요청을 처리한다.
	 *
	 * @param authentication 현재 인증된 사용자의 인증 객체
	 * @param image 변경할 프로필 이미지
	 * @return 변경된 프로필 이미지의 URL을 포함한 응답
	 */
	@PutMapping("/profile-image-change")
	public ResponseEntity<CustomResponseBody> profileImageChange(Authentication authentication,
		@RequestBody MultipartFile image) {
		String profileUrl = memberManagementService.changeProfileImage(authentication.getName(), image);

		return new ResponseEntity<>(BaseResponseBody.of("프로필 사진 변경에 성공했습니다", profileUrl), HttpStatus.OK);
	}

	/**
	 * 사용자 프로필 상세 조회를 처리한다.
	 *
	 * @param authentication 현재 인증된 사용자의 인증 객체
	 * @param nickname 조회할 사용자의 닉네임
	 * @param pageable 페이지네이션 정보를 포함하는 Pageable 객체 (필수 X)
	 * @return 사용자 프로필 정보를 포함한 응답
	 */
	@GetMapping("{nickname}")
	public ResponseEntity<CustomResponseBody> profileDetail(Authentication authentication,
		@PathVariable String nickname, @PageableDefault(value = 3) Pageable pageable) {
		MemberProfileRes result = memberManagementService.findProfile(authentication.getName(), nickname, pageable);
		return new ResponseEntity<>(BaseResponseBody.of("회원 프로필 조회에 성공했습니다", result), HttpStatus.OK);
	}

	/**
	 * 현재 사용자가 가입 대기 중인 소모임 목록을 조회한다.
	 *
	 * @param authentication 현재 인증된 사용자의 인증 객체
	 * @return 가입 대기 중인 소모임 목록을 포함한 응답
	 */
	@GetMapping("/clubs/my-requests")
	public ResponseEntity<CustomResponseBody> requestClubList(Authentication authentication) {
		List<MemberRequestClubRes> result = memberManagementService.findRequestClub(authentication.getName());
		return new ResponseEntity<>(BaseResponseBody.of("가입 대기중인 모임 조회에 성공했습니다", result), HttpStatus.OK);
	}
}
