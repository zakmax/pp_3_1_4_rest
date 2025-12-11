package ru.kata.spring.boot_security.demo.configs;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class SuccessUserHandler implements AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String contentType = request.getContentType();
        String header = request.getHeader("Accept");

        // Если это REST запрос (JSON)
        if (contentType != null && contentType.contains("application/json") ||
                header != null && header.contains("application/json")) {

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("admin") || role.equals("ADMIN"));

            String jsonResponse = String.format("{\"success\": true, \"isAdmin\": %s}", isAdmin);
            response.getWriter().write(jsonResponse);

        } else {
            // Если это обычный запрос браузера
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("admin") || role.equals("ADMIN"));

            if (isAdmin) {
                response.sendRedirect("/admin");
            } else {
                response.sendRedirect("/user");
            }
        }
    }
}