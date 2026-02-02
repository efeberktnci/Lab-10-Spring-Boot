package com.berk.lab10.config;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/*
  GlobalExceptionHandler

  Amaç:
  - Kullanıcıya stack trace göstermek yok.
  - Güvenli, minimal hata mesajı.
  - Accept: application/json ise JSON dön.
  - Browser isteklerinde error page döndür.

  Not: Burada password/token/PII loglamıyoruz.
*/
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private boolean wantsJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return accept != null && accept.toLowerCase().contains("application/json");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Object handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {

        HttpStatus status = (HttpStatus) ex.getStatusCode();

        // Log (safe)
        log.warn("APP: {} {} -> {}", req.getMethod(), req.getRequestURI(), status.value());

        if (wantsJson(req)) {
            return ResponseEntity.status(status).body(Map.of(
                    "error", status.getReasonPhrase()
            ));
        }

        ModelAndView mv = new ModelAndView("error");
        mv.addObject("status", status.value());
        mv.addObject("message", status.getReasonPhrase());
        return mv;
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneric(Exception ex, HttpServletRequest req) {

        // Log (stack trace server logs’da olabilir; ama kullanıcıya göstermiyoruz)
        log.error("APP: Unhandled exception on {} {}", req.getMethod(), req.getRequestURI(), ex);

        if (wantsJson(req)) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Internal Server Error"
            ));
        }

        ModelAndView mv = new ModelAndView("error");
        mv.addObject("status", 500);
        mv.addObject("message", "Internal Server Error");
        return mv;
    }
}
