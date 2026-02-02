/*
  HomeController.java

  Bu dosyada ne yapılıyor?
  - Login olduktan sonra /home sayfasını gösteriyoruz.
  - Kullanıcının email'ini ekrana basıyoruz.
  - Kullanıcı admin mi diye kontrol edip view'da admin linklerini koşullu gösteriyoruz.

  Hocaya anlatım:
  “Authentication üzerinden doğrulanmış kullanıcıyı okuyoruz ve e-posta ile isAdmin bilgisini Thymeleaf view’a aktarıyoruz.”
*/

package com.berk.lab10.controller;

import org.springframework.security.core.Authentication;     // login olmuş kullanıcı bilgisi
import org.springframework.security.core.GrantedAuthority;   // rol kontrolü
import org.springframework.stereotype.Controller;            // MVC controller
import org.springframework.ui.Model;                         // view'a data
import org.springframework.web.bind.annotation.GetMapping;   // GET mapping

@Controller
public class HomeController {

    /*
      GET /home
      - Authentication parametresi Spring tarafından otomatik verilir.
      - auth.getName() -> bu projede email (usernameParameter=email)
      - auth.getAuthorities() -> roller
    */
    @GetMapping("/home")
    public String home(Authentication auth, Model model) {

        // Admin mi? -> role listesinde ROLE_ADMIN var mı?
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        // Email'i view'a gönder
        model.addAttribute("email", auth != null ? auth.getName() : "");

        // Admin flag'i view'a gönder (Thymeleaf th:if ile kullanılır)
        model.addAttribute("isAdmin", isAdmin);

        // templates/home.html
        return "home";
    }
}
