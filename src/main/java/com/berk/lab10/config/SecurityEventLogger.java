package com.berk.lab10.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

/*
  SecurityEventLogger

  Lab 13 requirement:
  - Failed login attempt loglanmalı
  - Password/JWT/token/PII loglanmamalı

  Burada sadece principal (email) loglanır.
*/
@Component
public class SecurityEventLogger {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventLogger.class);

    @EventListener
    public void onBadCredentials(AuthenticationFailureBadCredentialsEvent event) {
        Object principal = event.getAuthentication() != null ? event.getAuthentication().getPrincipal() : "unknown";
        log.warn("SECURITY: Failed login attempt for principal={}", principal);
    }
}
