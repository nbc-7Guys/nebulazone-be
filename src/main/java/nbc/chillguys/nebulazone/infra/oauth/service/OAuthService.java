package nbc.chillguys.nebulazone.infra.oauth.service;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.user.dto.UserSignUpCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.oauth.dto.CustomOAuth2User;
import nbc.chillguys.nebulazone.infra.oauth.dto.KakaoOAuth2UserInfo;
import nbc.chillguys.nebulazone.infra.oauth.dto.NaverOAuth2UserInfo;
import nbc.chillguys.nebulazone.infra.oauth.dto.OAuth2UserInfo;
import nbc.chillguys.nebulazone.infra.oauth.exception.OAuthErrorCode;
import nbc.chillguys.nebulazone.infra.oauth.exception.OAuthException;
import nbc.chillguys.nebulazone.infra.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class OAuthService extends DefaultOAuth2UserService {
	private final UserDomainService userDomainService;
	private final JwtUtil jwtUtil;
	private final ObjectMapper objectMapper;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		OAuth2User oAuth2User = super.loadUser(userRequest);
		Map<String, Object> attributes = oAuth2User.getAttributes();

		OAuth2UserInfo oAuth2UserInfo = switch (registrationId) {
			// localhost:8080/oauth2/authorization/kakao
			case "kakao" -> objectMapper.convertValue(attributes, KakaoOAuth2UserInfo.class);

			// localhost:8080/oauth2/authorization/naver
			case "naver" -> objectMapper.convertValue(attributes, NaverOAuth2UserInfo.class);

			default -> throw new OAuthException(OAuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
		};

		User user = getOrCreateUser(oAuth2UserInfo);
		AuthUser authUser = AuthUser.from(user);

		String accessToken = jwtUtil.generateAccessToken(authUser);
		String refreshToken = jwtUtil.generateRefreshToken(authUser);

		return CustomOAuth2User.builder()
			.userId(user.getId())
			.roles(user.getRoles())
			.attributes(attributes)
			.nameAttributeKey(userRequest.getClientRegistration()
				.getProviderDetails()
				.getUserInfoEndpoint()
				.getUserNameAttributeName())
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	private User getOrCreateUser(OAuth2UserInfo oAuth2UserInfo) {
		User user;

		try {
			userDomainService.validEmail(oAuth2UserInfo.getEmail());
		} catch (UserException e) {
			return userDomainService.findActiveUserByEmailAndOAuthType(oAuth2UserInfo.getEmail(),
				oAuth2UserInfo.getOAuthType());
		}

		userDomainService.validNickname(oAuth2UserInfo.getNickname());

		user = userDomainService.createUser(UserSignUpCommand.from(oAuth2UserInfo));

		return user;
	}

}
