package com.shopaccgame.security.oauth2;

import com.shopaccgame.models.user.User;
import com.shopaccgame.repositories.user.UserRepository;
import com.shopaccgame.security.jwt.JwtTokenProvider;
import com.shopaccgame.models.user.CustomUserDetails;
import com.shopaccgame.services.user.authentication.LoginUserService;
import com.shopaccgame.enums.user.UserRole;
import com.shopaccgame.enums.user.UserStatus;
import com.shopaccgame.exceptions.common.ForbiddenException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
public class OAuth2LoginAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginAuthenticationSuccessHandler.class);

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginUserService loginUserService;

    @Value("${frontend.success.redirect:http://localhost:3000/home}")
    private String successRedirectBaseUrl;

    public OAuth2LoginAuthenticationSuccessHandler(UserRepository userRepository, 
                                                  JwtTokenProvider jwtTokenProvider,
                                                  LoginUserService loginUserService) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginUserService = loginUserService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();
        OAuth2AuthenticationToken oauth2AuthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauth2AuthToken.getAuthorizedClientRegistrationId();
        String providerId = getProviderId(provider, attributes);
        String email = attributes.get("email") != null ? (String) attributes.get("email")
                : generateDefaultEmail(providerId);
        String fullname = attributes.get("name") != null ? (String) attributes.get("name") : "Unknown";

        User user = userRepository.findByProviderAndProviderId(provider, providerId).orElse(null);
        boolean isNewUser = user == null;

        if (isNewUser) {
            user = createNewUser(provider, providerId, email, fullname);
        } else {
            updateUserInfo(user, fullname);
        }

        if (user.getUserStatus() == UserStatus.LOCKED) {
            throw new ForbiddenException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin để được hỗ trợ.");
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) loginUserService.loadUserByUsername(user.getUsername());

        UsernamePasswordAuthenticationToken newAuthToken = new UsernamePasswordAuthenticationToken(
            customUserDetails,
            null,
            customUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuthToken);

        String token = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String successRedirect = successRedirectBaseUrl + "?token=" + token;

        response.sendRedirect(successRedirect);
    }

    private User createNewUser(String provider, String providerId, String email, String fullname) {
        User newUser = new User();
        String username = "oauth_" + provider + "_" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        newUser.setEmail(email);
        newUser.setProvider(provider);
        newUser.setProviderId(providerId);
        newUser.setUsername(username);
        newUser.setFullname(fullname);
        newUser.setRole(UserRole.USER);
        newUser.setUserStatus(UserStatus.ACTIVE);
        newUser.setBalance(0);
        newUser.setTotaldeposit(0);
        return userRepository.save(newUser);
    }

    private void updateUserInfo(User user, String fullname) {
        user.setFullname(fullname);
        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }
        userRepository.save(user);
    }

    private String getProviderId(String provider, Map<String, Object> attributes) {
        switch (provider) {
            case "google":
                return (String) attributes.get("sub");
            case "facebook":
                return (String) attributes.get("id");
            default:
                return (String) attributes.get("id");
        }
    }

    private String generateDefaultEmail(String providerId) {
        return "oauth_" + providerId + "@shopaccgame.com";
    }
}