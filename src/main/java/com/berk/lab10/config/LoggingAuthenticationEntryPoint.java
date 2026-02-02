package com.berk.lab10.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
  LoggingAuthenticationEntryPoint

  - Kullanıcı login değilken protected route’a giderse çalışır.
  - Lab checklist:
      * API isteklerinde 401 dönmeli
      * MVC/HTML isteklerinde login'e redirect kabul edilebilir
  - Ayrıca Lab 13 gereği unauthorized access attempt loglar.

  Content negotiation:
  - Accept: application/json gibi ise -> 401
  - Browser HTML -> /login redirect
*/
@Component
public class LoggingAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(LoggingAuthenticationEntryPoint.class);

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();
        String accept = request.getHeader("Accept");

        log.warn("SECURITY: Unauthorized access attempt {} {} accept={}", method, path, accept);

        boolean wantsJson = accept != null && accept.toLowerCase().contains("application/json");

        if (wantsJson) {
            // API expectation: 401
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            // MVC expectation: redirect to login
            response.sendRedirect("/login");
        }
    }
}
