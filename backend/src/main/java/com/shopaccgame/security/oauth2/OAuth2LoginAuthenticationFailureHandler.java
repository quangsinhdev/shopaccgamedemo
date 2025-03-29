package com.shopaccgame.security.oauth2;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class OAuth2LoginAuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		String errorMessage = "Đăng nhập thất bại. Vui lòng thử lại";

		if (exception instanceof BadCredentialsException) {
			errorMessage = "Thông tin đăng nhập không hợp lệ.";
		}

		String redirectUrl = ("/login?oauth2error=true");

		response.sendRedirect(redirectUrl);
	}
}
