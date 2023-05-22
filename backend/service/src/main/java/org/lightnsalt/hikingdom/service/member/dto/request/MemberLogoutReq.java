package org.lightnsalt.hikingdom.service.member.dto.request;

import lombok.Data;

/**
 * 사용자 로그아웃 요청 객체
 */
@Data
public class MemberLogoutReq {

	/**
	 * 사용자 기기의 Firebase Cloud Messaging(FCM) 토큰
	 */
	private String fcmToken;
}
