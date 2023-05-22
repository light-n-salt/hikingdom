package org.lightnsalt.hikingdom.service.member.controller;

import javax.validation.Valid;

import org.lightnsalt.hikingdom.common.dto.BaseResponseBody;
import org.lightnsalt.hikingdom.common.dto.CustomResponseBody;
import org.lightnsalt.hikingdom.common.dto.ErrorResponseBody;
import org.lightnsalt.hikingdom.common.error.ErrorCode;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberEmailReq;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberLoginReq;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberRefreshTokenReq;
import org.lightnsalt.hikingdom.service.member.dto.response.MemberTokenRes;
import org.lightnsalt.hikingdom.service.member.service.MemberAuthService;
import org.lightnsalt.hikingdom.service.member.service.MemberEmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 회원 인증을 받기 위해 필요한 API
 * JWT 토큰 인증이 없어도 접근 가능
 */
@RestController
@RequestMapping("/api/v1/members/auth")
@RequiredArgsConstructor
public class MemberAuthController {
	private final MemberAuthService memberAuthService;
	private final MemberEmailService memberEmailService;

	/**
	 * 로그인을 처리한다.
	 *
	 * @param memberLoginReq 사용자 자격 증명(이메일, 비밀번호 등)을 포함한 로그인 요청 객체
	 * @param bindingResult 로그인 요청 객체에 대한 유효성 검사 결과
	 * @return 로그인을 요청한 사용자에 대한 access 및 refresh 토큰을 포함한 응답
	 */
	@PostMapping("/login")
	public ResponseEntity<CustomResponseBody> login(@RequestBody @Valid MemberLoginReq memberLoginReq,
		BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return new ResponseEntity<>(ErrorResponseBody.of(ErrorCode.INVALID_INPUT_VALUE,
				bindingResult.getAllErrors().get(0).getDefaultMessage()),
				HttpStatus.BAD_REQUEST);
		}

		MemberTokenRes memberTokenRes = memberAuthService.login(memberLoginReq);

		return new ResponseEntity<>(BaseResponseBody.of("로그인에 성공했습니다", memberTokenRes), HttpStatus.OK);
	}

	/**
	 * Access token 재발급 요청을 처리한다.
	 *
	 * @param memberRefreshTokenReq refresh token을 포함한 토큰 재발급 요청 객체
	 * @return 새로운 access token을 포함한 응답
	 */
	@PostMapping("/refresh-token")
	public ResponseEntity<CustomResponseBody> tokenRefresh(@RequestBody MemberRefreshTokenReq memberRefreshTokenReq) {
		MemberTokenRes memberTokenRes = memberAuthService.refreshToken(memberRefreshTokenReq);

		return new ResponseEntity<>(BaseResponseBody.of("토큰 재발급에 성공했습니다", memberTokenRes), HttpStatus.OK);
	}

	/**
	 * 비밀번호 재발급 요청을 처리한다.
	 *
	 * @param memberEmailReq 이메일 요청 객체
	 * @param bindingResult 이메일 요청 객체에 대한 유효성 검사 결과
	 * @return 비밀번호 재발급 요청 처리 상태에 대한 응답
	 */
	@PutMapping("/password-find")
	public ResponseEntity<CustomResponseBody> passwordFind(@RequestBody @Valid MemberEmailReq memberEmailReq,
		BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return new ResponseEntity<>(ErrorResponseBody.of(ErrorCode.INVALID_INPUT_VALUE,
				bindingResult.getAllErrors().get(0).getDefaultMessage()),
				HttpStatus.BAD_REQUEST);
		}

		memberEmailService.sendFindPasswordEmail(memberEmailReq);

		return new ResponseEntity<>(BaseResponseBody.of("임시 비밀번호 발급에 성공했습니다"), HttpStatus.OK);
	}

	/**
	 * 서비스 상태 확인 요청을 처리한다.
	 *
	 * @return 서비스 상태에 대한 응답
	 */
	@GetMapping("/health-check")
	public ResponseEntity<CustomResponseBody> healthCheck() {
		return new ResponseEntity<>(BaseResponseBody.of("서비스가 정상 작동 상태입니다"), HttpStatus.OK);
	}
}
