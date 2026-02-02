package com.berk.lab10.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
  LoggingAccessDeniedHandler

  - Kullanıcı login olmuş ama yetkisi yoksa (403) çalışır.
  - Lab 13 gereği forbidden access attempt loglar.
  - Content negotiation:
      * API -> 403
      * Browser -> /access-denied redirect
*/
@Component
public class LoggingAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingAccessDeniedHandler.class);

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();
        String accept = request.getHeader("Accept");

        Authentication auth = (Authentication) request.getUserPrincipal();
        String principal = (auth != null ? auth.getName() : "unknown");

        log.warn("SECURITY: Forbidden access attempt {} {} by={} accept={}", method, path, principal, accept);

        boolean wantsJson = accept != null && accept.toLowerCase().contains("application/json");

        if (wantsJson) {
            //  API expectation: 403
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            // MVC expectation: access denied page
            response.sendRedirect("/access-denied");
        }
    }
}
