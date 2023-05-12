package org.lightnsalt.hikingdom.domain.club.common.enumtype;

import lombok.Getter;

/**
 * 소모임 가입 신청 상태
 */
@Getter
public enum JoinRequestStatusType {
	PENDING, // 가입 신청
	ACCEPTED, // 가입 신청 수락
	REJECTED // 가입 신청 거절
}