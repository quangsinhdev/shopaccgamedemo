package com.shopaccgame.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class CsrfFilter extends OncePerRequestFilter {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Value("${security.csrf.excluded-paths}")
	private String[] excludedPaths;

	private List<String> CSRF_EXCLUDED_PATHS;

	private static final List<String> SAFE_METHODS = Arrays.asList("GET", "HEAD", "OPTIONS");

	@PostConstruct
	public void init() {
		CSRF_EXCLUDED_PATHS = Arrays.asList(excludedPaths);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String requestPath = request.getRequestURI();
		String method = request.getMethod();

		if (CSRF_EXCLUDED_PATHS.stream().anyMatch(path -> requestPath.startsWith(path))) {
			filterChain.doFilter(request, response);
			return;
		}

		if (SAFE_METHODS.contains(method)) {
			filterChain.doFilter(request, response);
			return;
		}

		String username = null;
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (principal instanceof UserDetails) {
				username = ((UserDetails) principal).getUsername();
			}
		}

		if (username == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Người dùng chưa đăng nhập!");
			return;
		}

		String csrfTokenFromHeader = request.getHeader("X-XSRF-TOKEN");
		if (csrfTokenFromHeader == null || csrfTokenFromHeader.isEmpty()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Thiếu CSRF token trong header!");
			return;
		}

		String redisKey = "csrf:" + username;
		String csrfTokenFromRedis = (String) redisTemplate.opsForValue().get(redisKey);

		if (csrfTokenFromRedis == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không tồn tại hoặc đã hết hạn!");
			return;
		}

		if (!csrfTokenFromHeader.equals(csrfTokenFromRedis)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ!");
			return;
		}

		filterChain.doFilter(request, response);
	}
}