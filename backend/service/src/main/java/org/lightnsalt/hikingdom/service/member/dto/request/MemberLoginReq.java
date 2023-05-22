package org.lightnsalt.hikingdom.service.member.dto.request;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import lombok.Data;

/**
 * 사용자 로그인 요청 객체
 */
@Data
public class MemberLoginReq {
	
	/**
	 * 사용자 이메일
	 */
	@NotEmpty(message = "이메일은 필수 입력값입니다.")
	@Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "이메일 형식에 맞지 않습니다.")
	private String email;

	/**
	 * 사용자 비밀번호
	 */
	@NotEmpty(message = "비밀번호는 필수 입력값입니다.")
	private String password;

	/**
	 * 사용자 기기의 Firebase Cloud Messaging(FCM) 토큰
	 */
	private String fcmToken;
}
