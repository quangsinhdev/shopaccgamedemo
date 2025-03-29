package com.shopaccgame.security.jwt;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		String message;
		if (authException.getCause() instanceof ExpiredJwtException) {
			message = "Access token đã hết hạn. Vui lòng dùng refresh token để làm mới.";
		} else {
			message = "Xác thực không thành công: " + authException.getMessage();
		}

		response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}");
	}
}