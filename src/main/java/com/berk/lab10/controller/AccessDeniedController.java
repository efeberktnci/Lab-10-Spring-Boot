package com.berk.lab10.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/*
  AccessDeniedController
  - /access-denied sayfasını döndürür.
  - LoggingAccessDeniedHandler buraya redirect eder (browser istekleri için).
*/
@Controller
public class AccessDeniedController {

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
