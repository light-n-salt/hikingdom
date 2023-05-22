package org.lightnsalt.hikingdom.service.member.dto.request;

import lombok.Data;

/**
 * 토큰 재발급 요청 객체
 */
@Data
public class MemberRefreshTokenReq {

	/**
	 * "Bearer " prefix를 포함한 사용자 refresh token
	 */
	private String refreshToken; // includes "Bearer " prefix
}
