package com.shopaccgame.utils;

import com.shopaccgame.exceptions.user.authentication.TokenAuthenticationException;
import com.shopaccgame.models.user.CustomUserDetails;
import com.shopaccgame.models.user.User;

import org.springframework.security.core.Authentication;

public class AuthenticationUtil {

    public static User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new TokenAuthenticationException("Không thể xác thực người dùng.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        } else if (principal instanceof User) {
            return (User) principal;
        } else {
            throw new TokenAuthenticationException("Không thể xác định thông tin người dùng từ token.");
        }
    }
}