package org.lightnsalt.hikingdom.service.member;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lightnsalt.hikingdom.service.member.dto.request.MemberLoginReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class MemberAuthControllerTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@Autowired
	private MappingJackson2HttpMessageConverter jsonConverter;

	@BeforeEach
	public void setUp(RestDocumentationContextProvider restDocumentation) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
			.apply(documentationConfiguration(restDocumentation))
			.build();
	}

	@DisplayName("로그인: Access/Refresh 토큰 발급")
	@Test
	public void testLoginSuccess() throws Exception {
		// given
		final String url = "/api/v1/members/auth/login";

		// when
		MemberLoginReq memberLoginReq = new MemberLoginReq();
		memberLoginReq.setEmail("test@test.com");
		memberLoginReq.setPassword("ssafy!1234");
		memberLoginReq.setFcmToken(
			"dnIm4GPxQ5-IIu-x4ZUilv:APA91bEazOFoChb5wO2NeP0ljiFiOFnxdm5YcMFkPP9HIV53KaFZfgpaxwck4z7TW3rHC2AsKM73wZLH6gJLpL_UB_okRuc60QGm1sDIQWy8KZ67CsPzuG7hYLL8hliANI_CDW4BgjkZ");

		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonConverter.getObjectMapper().writeValueAsString(memberLoginReq))
		);

		// then
		resultActions.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("members/login",
				requestFields(
					fieldWithPath("email").description("이메일").type(JsonFieldType.STRING),
					fieldWithPath("password").description("비밀번호").type(JsonFieldType.STRING),
					fieldWithPath("fcmToken").description("FCM Token (필수 X)").type(JsonFieldType.STRING)
				),
				responseFields(
					fieldWithPath("message").description("응답 메시지").type(JsonFieldType.STRING),
					fieldWithPath("result.accessToken").description("액세스 토큰").type(JsonFieldType.STRING),
					fieldWithPath("result.refreshToken").description("리프레시 토큰").type(JsonFieldType.STRING)
				)));
	}
}
