/*
  UserController.java

  Bu dosyada ne yapılıyor?
  - USER rolüyle korunan basit bir test endpoint'i var.
  - /user/ping -> "USER OK" döner.
  - Ama bu endpoint:
      - login yoksa -> 401
      - route security'de /user/** -> USER/ADMIN yetkisiyle çalışır

*/

package com.berk.lab10.controller;

import org.springframework.web.bind.annotation.*; // REST anotasyonları

@RestController // Bu controller HTML değil, direkt text/JSON döner
@RequestMapping("/user") // bütün endpoint'ler /user ile başlar
public class UserController {

    /*
      GET /user/ping
      - Yetki test endpoint'i
      - Başarılıysa "USER OK"
    */
    @GetMapping("/ping")
    public String ping() {
        return "USER OK";
    }
}
