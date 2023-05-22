package org.lightnsalt.hikingdom.service.member.service;

import java.util.List;

import org.lightnsalt.hikingdom.service.member.dto.request.MemberChangePasswordReq;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberLogoutReq;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberNicknameReq;
import org.lightnsalt.hikingdom.service.member.dto.response.MemberDetailRes;
import org.lightnsalt.hikingdom.service.member.dto.response.MemberProfileRes;
import org.lightnsalt.hikingdom.service.member.dto.response.MemberRequestClubRes;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface MemberManagementService {
	MemberDetailRes findMemberInfo(String email);

	void removeMember(String email);

	void logout(String bearerToken, MemberLogoutReq memberLogoutReq);

	void changePassword(String email, MemberChangePasswordReq memberChangePasswordReq);

	void changeNickname(String email, MemberNicknameReq memberNicknameReq);

	String changeProfileImage(String email, MultipartFile photo);

	MemberProfileRes findProfile(String email, String nickname, Pageable pageable);

	List<MemberRequestClubRes> findRequestClub(String email);
}
