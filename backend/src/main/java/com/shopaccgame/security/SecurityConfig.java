package com.shopaccgame.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.shopaccgame.security.jwt.JwtAuthenticationEntryPoint;
import com.shopaccgame.security.jwt.JwtAuthenticationFilter;
import com.shopaccgame.security.oauth2.OAuth2LoginAuthenticationFailureHandler;
import com.shopaccgame.security.oauth2.OAuth2LoginAuthenticationSuccessHandler;
import com.shopaccgame.services.user.authentication.LoginUserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final OAuth2LoginAuthenticationFailureHandler oAuth2LoginAuthenticationFailureHandler;
	private final OAuth2LoginAuthenticationSuccessHandler oAuth2LoginAuthenticationSuccessHandler;
	private final LoginUserService loginUserService;
	private final CsrfFilter csrfFilter;

	public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			OAuth2LoginAuthenticationFailureHandler oAuth2LoginAuthenticationFailureHandler,
			OAuth2LoginAuthenticationSuccessHandler oAuth2LoginAuthenticationSuccessHandler,
			LoginUserService loginUserService, CsrfFilter csrfFilter) {
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.oAuth2LoginAuthenticationFailureHandler = oAuth2LoginAuthenticationFailureHandler;
		this.oAuth2LoginAuthenticationSuccessHandler = oAuth2LoginAuthenticationSuccessHandler;
		this.loginUserService = loginUserService;
		this.csrfFilter = csrfFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource())).formLogin(form -> form.disable())
				.csrf(csrf -> csrf.disable())
				.oauth2Login(oauth2 -> oauth2.loginPage("/pages/client/login.html").defaultSuccessUrl("/", true)
						.failureHandler(oAuth2LoginAuthenticationFailureHandler)
						.successHandler(oAuth2LoginAuthenticationSuccessHandler))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/index.html", "/pages/client/login.html", "/pages/client/recovery.html",
								"/pages/client/updatepassword.html", "/assets/**", "/css/**", "/js/**")
						.permitAll().requestMatchers("/oauth2/authorization/google", "/login?oauth2error=true")
						.permitAll().requestMatchers("/api/gameaccounts/**").permitAll()
						.requestMatchers("/api/users/verify-recovery-token", "/api/users/login", "/api/users/logout",
								"/api/users/refresh-token", "/api/users/register", "/api/users/password-recovery")
						.permitAll()
						.requestMatchers("/error").permitAll()
						.requestMatchers("/swagger-ui/**","/v3/api-docs/**","/swagger-ui.html").permitAll()
						.requestMatchers("/api/users/password", "/api/users/deposit-payment-info",
								"/api/users/transactions/**", "/api/users/vouchers/**", "/api/users/giftcodes/**")
						.hasAnyRole("USER", "AGENCY", "ADMIN").requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/agency/**").hasRole("AGENCY").anyRequest().authenticated())
				.exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterAfter(csrfFilter, JwtAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder.userDetailsService(loginUserService).passwordEncoder(passwordEncoder());
		return authenticationManagerBuilder.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(Arrays.asList("https://localhost:3000"));
		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
		config.setAllowedHeaders(Arrays.asList("*"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}