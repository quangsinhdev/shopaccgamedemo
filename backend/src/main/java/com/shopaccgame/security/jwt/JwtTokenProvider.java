package com.shopaccgame.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.shopaccgame.enums.user.UserRole;
import com.shopaccgame.exceptions.user.authentication.TokenAuthenticationException;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String secretKey;

	@Value("${jwt.expiration}")
	private long accessTokenExpiration;

	@Value("${refresh-token.expiration}")
	private long refreshTokenExpiration;

	private SecretKey key;

	public long getAccessTokenExpiration() {
		return accessTokenExpiration;
	}

	public long getRefreshTokenExpiration() {
		return refreshTokenExpiration;
	}

	public SecretKey getKey() {
		return key;
	}

	public void setKey(SecretKey key) {
		this.key = key;
	}

	@PostConstruct
	public void init() {
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
	}

	public String generateAccessToken(String username, UserRole role) {
		if (username == null || role == null) {
			throw new IllegalArgumentException("Username and role cannot be null");
		}

		String roleString = role.name();
		String token = Jwts.builder().subject(username).claim("role", roleString).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + accessTokenExpiration)).signWith(key).compact();

		return token;
	}

	public String generateRefreshToken(String username, UserRole role) {
		if (username == null || role == null) {
			throw new IllegalArgumentException("Username and role cannot be null");
		}

		String roleString = role.name();
		String token = Jwts.builder().subject(username).claim("role", roleString).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration)).signWith(key).compact();

		return token;
	}

	public String getUsernameFromToken(String token) {
		if (token == null) {
			throw new IllegalArgumentException("Token cannot be null");
		}
		try {
			String username = getClaimsFromToken(token).getSubject();
			return username;
		} catch (ExpiredJwtException e) {
			throw e;
		} catch (JwtException e) {
			throw new TokenAuthenticationException("Token không hợp lệ: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

	public UserRole getRoleFromToken(String token) {
		if (token == null) {
			throw new TokenAuthenticationException("Token cannot be null", HttpStatus.UNAUTHORIZED);
		}
		try {
			Claims claims = getClaimsFromToken(token);
			String roleString = claims.get("role", String.class);
			if (roleString == null) {
				throw new TokenAuthenticationException("Role not found in token", HttpStatus.UNAUTHORIZED);
			}
			UserRole role = UserRole.valueOf(roleString);
			return role;
		} catch (ExpiredJwtException e) {
			throw e;
		} catch (JwtException e) {
			throw new TokenAuthenticationException("Token không hợp lệ: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

	private Claims getClaimsFromToken(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}

	public boolean isTokenExpired(String token) {
		if (token == null) {
			throw new TokenAuthenticationException("Token cannot be null", HttpStatus.UNAUTHORIZED);
		}
		try {
			boolean expired = getClaimsFromToken(token).getExpiration().before(new Date());
			return expired;
		} catch (ExpiredJwtException e) {
			return true;
		} catch (JwtException e) {
			throw new TokenAuthenticationException("Token không hợp lệ: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}

	public boolean validateToken(String token, String username) {
		if (token == null || username == null) {
			throw new TokenAuthenticationException("Token và username không được để trống", HttpStatus.UNAUTHORIZED);
		}
		try {
			Claims claims = getClaimsFromToken(token);
			String tokenUsername = claims.getSubject();
			String roleString = claims.get("role", String.class);
			boolean expired = claims.getExpiration().before(new Date());

			if (tokenUsername == null) {
				throw new TokenAuthenticationException("Không tìm thấy username trong token", HttpStatus.UNAUTHORIZED);
			}
			if (roleString == null) {
				throw new TokenAuthenticationException("Không tìm thấy role trong token", HttpStatus.UNAUTHORIZED);
			}

			boolean isValid = tokenUsername.equals(username) && !expired;
			return isValid;
		} catch (ExpiredJwtException e) {
			throw e;
		} catch (JwtException e) {
			throw new TokenAuthenticationException("Token không hợp lệ: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}
}