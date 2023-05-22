package org.lightnsalt.hikingdom.service.member.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 사용자 토큰 정보 객체
 */
@Data
@Builder
public class MemberTokenRes {

	/**
	 * 사용자 자격 증명을 위한 access token
	 */
	private String accessToken;

	/**
	 * 사용자 자격 증명을 위한 refresh token
	 */
	private String refreshToken;
}
