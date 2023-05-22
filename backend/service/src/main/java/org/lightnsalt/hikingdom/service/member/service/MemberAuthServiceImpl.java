package org.lightnsalt.hikingdom.service.member.service;

import org.lightnsalt.hikingdom.common.error.ErrorCode;
import org.lightnsalt.hikingdom.common.error.GlobalException;
import org.lightnsalt.hikingdom.common.util.JwtTokenUtil;
import org.lightnsalt.hikingdom.common.util.RedisUtil;
import org.lightnsalt.hikingdom.domain.entity.notification.MemberFcmToken;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberLoginReq;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberRefreshTokenReq;
import org.lightnsalt.hikingdom.service.member.dto.response.MemberTokenRes;
import org.lightnsalt.hikingdom.domain.entity.member.Member;
import org.lightnsalt.hikingdom.service.member.repository.MemberFcmTokenRepository;
import org.lightnsalt.hikingdom.service.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberAuthServiceImpl implements MemberAuthService {
	private final PasswordEncoder passwordEncoder;

	private final RedisUtil redisUtil;
	private final JwtTokenUtil jwtTokenUtil;

	private final MemberRepository memberRepository;
	private final MemberFcmTokenRepository memberFcmTokenRepository;

	@Transactional
	@Override
	public MemberTokenRes login(MemberLoginReq memberLoginReq) {
		String email = memberLoginReq.getEmail();

		final Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_LOGIN_FAIL));

		if (!passwordEncoder.matches(memberLoginReq.getPassword(), member.getPassword())) {
			throw new GlobalException(ErrorCode.MEMBER_LOGIN_FAIL);
		}

		// save FCM token
		String fcmToken = memberLoginReq.getFcmToken();
		if (fcmToken != null && fcmToken.length() > 0 &&
			!memberFcmTokenRepository.existsByMemberIdAndBody(member.getId(), fcmToken)) {
			MemberFcmToken memberFcmToken = MemberFcmToken.builder().member(member).body(fcmToken).build();

			memberFcmTokenRepository.save(memberFcmToken);
		}

		String accessToken = jwtTokenUtil.createAccessToken(email, member.getRole());
		String refreshToken = jwtTokenUtil.createRefreshToken(email, member.getRole());

		redisUtil.deleteValue("RT" + email);
		redisUtil.setValueWithExpiration("RT" + email, refreshToken.substring(7), jwtTokenUtil.refreshExpiration);

		log.debug("Successful Login: " + email);

		return MemberTokenRes.builder().accessToken(accessToken).refreshToken(refreshToken).build();
	}

	@Override
	public MemberTokenRes refreshToken(MemberRefreshTokenReq memberRefreshTokenReq) {
		String oldRefreshToken = jwtTokenUtil.resolveToken(memberRefreshTokenReq.getRefreshToken());
		String email = jwtTokenUtil.getEmailFromToken(oldRefreshToken);

		if (!oldRefreshToken.equals(redisUtil.getValue("RT" + email))) {
			throw new GlobalException(ErrorCode.INVALID_TOKEN);
		}

		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_UNAUTHORIZED));

		String accessToken = jwtTokenUtil.createAccessToken(email, member.getRole());
		
		// refresh token은 재발급하지 않음. Android/Frontend 동기화 위해
		return MemberTokenRes.builder().accessToken(accessToken).refreshToken("Bearer " + oldRefreshToken).build();
	}
}
