/*
 Bu sınıf uygulamanın güvenlik ayarlarını içerir.

 - Hangi URL’lerin login gerektirdiğini belirler
 - USER / ADMIN rol yetkilerini tanımlar
 - Login ve logout işlemlerini Spring Security ile yönetir
 - CSRF korumasını aktif eder
 - Şifrelerin BCrypt ile hash’lenmesini sağlar
 - Yetkisiz erişimde:
      * Browser (HTML) -> redirect /login veya /access-denied
      * API (Accept: application/json) -> direkt 401 / 403 döner
 - @PreAuthorize gibi method-level güvenliği aktif eder
 - HTTP security headers (CSP, nosniff, frame options, referrer policy) ekler
 - Unauthorized/forbidden access attempt loglar

 Kısaca: Uygulamanın kapısı, kilidi ve anahtarı buradadır.
*/

package com.berk.lab10.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt strength parameter (cost factor)
        // 10 default, 12 daha güçlü ve yaygın bir seçim
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            LoggingAuthenticationEntryPoint loggingAuthenticationEntryPoint,
            LoggingAccessDeniedHandler loggingAccessDeniedHandler
    ) throws Exception {

        http
                //  CSRF koruması açık.
                // CookieCsrfTokenRepository.withHttpOnlyFalse() KALDIRILDI.
                // Thymeleaf form'larda hidden _csrf token var; bu MVC için yeterli.
                .csrf(csrf -> { })

                //  HTTP and security headers (Lab 13 / checklist)
                .headers(headers -> headers
                        // X-Content-Type-Options: nosniff
                        .contentTypeOptions(c -> { })

                        // X-Frame-Options: SAMEORIGIN (istersen DENY yapabilirsin)
                        .frameOptions(f -> f.sameOrigin())

                        // Referrer-Policy
                        .referrerPolicy(r -> r.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER
                        ))

                        // Content-Security-Policy (CSP)
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data:; " +
                                        "object-src 'none'; " +
                                        "base-uri 'self'; " +
                                        "frame-ancestors 'self'"
                        ))
                )

                // URL bazlı yetkilendirme kuralları
                .authorizeHttpRequests(auth -> auth

                        // Login olmadan erişilebilen sayfalar
                        .requestMatchers("/login", "/register", "/access-denied", "/error", "/css/**", "/js/**")
                        .permitAll()

                        // Sadece ADMIN rolüne sahip kullanıcılar erişebilir
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // USER veya ADMIN erişebilir
                        .requestMatchers("/user/**")
                        .hasAnyRole("USER", "ADMIN")

                        // Diğer tüm endpoint’ler login gerektirir
                        .anyRequest()
                        .authenticated()
                )

                // Exception handling:
                // - Browser HTML istekleri için redirect
                // - API/JSON istekleri için 401/403
                // + her iki durumda da SECURITY log bas
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(loggingAuthenticationEntryPoint)
                        .accessDeniedHandler(loggingAccessDeniedHandler)
                )

                // Session tabanlı form login
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // Logout işlemi
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .permitAll()
                );

        return http.build();
    }
}
