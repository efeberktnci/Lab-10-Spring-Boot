/*
  AdminController.java

  Bu dosyada ne yapılıyor?
  - Admin'e özel sayfaları yönetiyoruz.
  - @PreAuthorize ile method seviyesinde rol kontrolü yapıyoruz.
  - /admin/ping -> admin erişimi test sayfası
  - /admin/users -> tüm kullanıcı listesini gösteren sayfa

  Hocaya anlatım:
  "Admin routes are protected with ROLE_ADMIN using method-level security
   via @PreAuthorize."
*/

package com.berk.lab10.controller;

import com.berk.lab10.service.UserService; // Kullanıcı listesini almak için servis

import org.springframework.security.access.prepost.PreAuthorize; // method bazlı güvenlik
import org.springframework.stereotype.Controller;                // MVC controller (HTML)
import org.springframework.ui.Model;                             // view'a data taşımak için
import org.springframework.web.bind.annotation.GetMapping;       // GET endpoint

@Controller
public class AdminController {

    private final UserService userService;

    // Constructor injection
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /*
      GET /admin/ping
      - Sadece ADMIN erişebilsin diye @PreAuthorize var.
      - Basit bir HTML sayfa döner (admin-ping.html)
    */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/ping")
    public String adminPing() {
        return "admin-ping";
    }

    /*
      GET /admin/users
      - Sadece admin görebilir.
      - userService.getAllUsers() ile DTO listesi alınır.
      - model içine koyup admin-users.html'e göndeririz.
    */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public String adminUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-users";
    }
}
