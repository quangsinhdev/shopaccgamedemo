package com.shopaccgame.security.jwt;

import com.shopaccgame.services.user.authentication.LoginUserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


	private final JwtTokenProvider jwtTokenProvider;
	private final LoginUserService loginUserService;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, LoginUserService loginUserService) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.loginUserService = loginUserService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String token = getJwtFromCookie(request);

		if (token != null) {
			try {

				String username = jwtTokenProvider.getUsernameFromToken(token);

				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					if (jwtTokenProvider.validateToken(token, username)) {
						UserDetails userDetails = loginUserService.loadUserByUsername(username);
						UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
								null, userDetails.getAuthorities());
						auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(auth);
					}
				}
			} catch (ExpiredJwtException e) {
				sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
						"Phiên đăng nhập đã hết hạn. Vui lòng làm mới token.");
				return;
			} catch (Exception e) {
				sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token không hợp lệ.");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private String getJwtFromCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("accessToken".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(status);
		response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}");
	}
}