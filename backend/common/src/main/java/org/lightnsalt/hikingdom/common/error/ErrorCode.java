package org.lightnsalt.hikingdom.common.error;

import lombok.Getter;

/**
 * List of possible error codes
 */
@Getter
public enum ErrorCode {

	// common error codes
	INVALID_INPUT_VALUE(400, "C001", "입력값이 유효하지 않습니다"),
	METHOD_NOT_ALLOWED(405, "C002", "지원하지 않는 HTTP 요청 메서드입니다"),
	INTERNAL_SERVER_ERROR(500, "C003", "서버에서 예상치 못한 오류가 발생했습니다"),
	MEMBER_UNAUTHORIZED(401, "C004", "인증 자격 증명이 유효하지 않습니다"),
	INVALID_TOKEN(401, "C005", "토큰이 유효하지 않습니다"),
	EXPIRED_TOKEN(401, "C006", "토큰이 만료되었습니다"),
	MISSING_REQUEST_BODY(400, "C007", "응답 요청 내용이 없습니다"),
	MISSING_REQUEST_PARAMETER(400, "C008", "응답 요청 파라미터가 없습니다"),

	// business error codes //

	// member related error codes
	DUPLICATE_EMAIL(400, "M001", "이미 가입된 이메일입니다"),
	DUPLICATE_NICKNAME(400, "M002", "이미 존재하는 닉네임입니다"),
	MEMBER_NOT_FOUND(404, "M003", "존재하지 않는 사용자입니다"),
	MEMBER_LOGIN_FAIL(401, "M004", "이메일 또는 비밀번호가 올바르지 않습니다"),

	// info related error codes
	DUPLICATED_MOUNTAIN_REGISTER(403, "IM001", "이미 등록된 산 이름입니다"),
	MOUNTAIN_NOT_FOUND(404, "IM002", "존재하지 않는 데이터입니다"),
	BASE_ADDRESS_NOT_FOUND(404, "L001", "존재하지 않는 지역 정보입니다"),
	WRONG_DATA_SCOPE(400, "IM003", "잘못된 위경도 범위입니다"),

	// club related error codes
	CLUB_ALREADY_JOINED(400, "K001", "이미 소모임에 가입되어 있습니다"),
	DUPLICATE_CLUB_NAME(400, "K002", "이미 존재하는 소모임 이름입니다"),
	CLUB_NOT_FOUND(404, "K003", "존재하지 않는 소모임입니다"),
	CLUB_MEETUP_NOT_FOUND(404, "KM001", "해당 소모임에 포함된 일정이 아닙니다"),

	// club member related error codes
	CLUB_MEMBER_UNAUTHORIZED(401, "CM001", "소모임에 가입된 회원이 아닙니다"),
	CLUB_JOIN_REQUEST_PENDING(400, "CM002", "이미 가입 요청한 소모임입니다"),
	CLUB_JOIN_REQUEST_NOT_FOUND(404, "CM003", "유효한 소모임 가입 요청이 없습니다"),
	CLUB_MEMBER_HOST_MEETUP_EXISTS(400, "CM004", "아직 진행 중인 일정이 있습니다"),
	CLUB_MEMBER_IS_HOST(400, "CM005", "소모임 모임장은 탈퇴할 수 없습니다"),

	// meetup related error codes
	MEETUP_NOT_FOUND(404, "MU001", "존재하지 않는 일정입니다"),
	MEETUP_ALREADY_JOINED(400, "MU002", "이미 참여한 일정입니다"),
	MEETUP_MEMBER_UNAUTHORIZED(401, "MU003", "일정에 가입된 회원이 아닙니다"),
	MEETUP_HOST_UNAUTHORIZED(401, "MU004", "일정 운영진이 아닙니다"),
	MEETUP_ALREADY_DONE(400, "MU005", "이미 지나간 일정입니다"),
	MEETUP_NOT_JOINED(400, "MU006", "참여한 일정이 아닙니다"),
	MEETUP_START_AT_INVALID(400, "MU007", "이미 지나간 날짜에 일정을 생성할 수 없습니다"),

	// meetup photo related error codes
	FAIL_TO_SAVE_PHOTO(500, "MP001", "사진 저장에 실패했습니다"),
	PHOTO_NOT_FOUND(404, "MP002", "삭제되었거나 존재하지 않는 사진입니다"),

	// meetup review related error codes
	MEETUP_REVIEW_NOT_FOUND(404, "MR001", "존재하지 않는 일정 후기입니다"),

	// hiking record related error codes
	HIKING_RECORD_NOT_FOUND(404, "H001", "존재하지 않는 등산 기록입니다"),
	HIKING_LOCATION_SHARE_INVALID(404, "H002", "올바르지 않은 등산 위치 공유 요청입니다"),

	// chat related error codes
	CHAT_NOT_FOUND(404, "CH001", "존재하지 않는 채팅 기록입니다"),

	// notification related error codes
	NOTIFICATION_NOT_FOUND(404, "N001", "알림이 존재하지 않습니다"),

	// member hiking statistic related error codes
	MEMBER_HIKING_STATISTIC_NOT_FOUND(404, "HS001", "등산 통계가 존재하지 않습니다"),

	// member level info related error codes
	MEMBER_LEVEL_INFO_NOT_FOUND(404, "L001", "레벨 정보가 존재하지 않습니다"),

	// club member related error codes
	MEMBER_HAS_NO_CLUBS(404, "CM001", "사용자가 속한 소모임이 없습니다"),

	// asset related error codes
	ASSET_NOT_FOUND(404, "A001", "Asset이 존재하지 않습니다"),

	// report related error codes
	ALREADY_REPORTED(400, "R001", "이미 신고되었습니다");

	private final int status;
	private final String code;
	private final String message;

	ErrorCode(final int status, final String code, final String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}
