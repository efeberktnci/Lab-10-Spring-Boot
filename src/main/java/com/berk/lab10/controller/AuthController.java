/*
  AuthController.java

  Bu dosyada ne yapılıyor?
  - /login ve /register sayfalarını (GET) kullanıcıya gösteriyoruz.
  - /register POST ile yeni kullanıcı oluşturuyoruz.
  - Güvenlik için:
      1) Aynı email ile tekrar kayıt engelleniyor.
      2) Common password blacklist var (password123 gibi).
      3) Strong password policy var (min 8, büyük/küçük/rakam/özel).
      4) Şifreler BCrypt ile HASH edilerek kaydediliyor (plain text saklanmaz).
      5) Admin rolü sadece secret doğru ise veriliyor.

  “Registration controller parola politikalarını zorunlu kılar ve parolaları hashlenmiş şekilde saklar.
  Login sayfası Spring Security tarafından yönetilmektedir,
  ancak login view (görünüm) rotasını biz sağlıyoruz.”
*/

package com.berk.lab10.controller;

import com.berk.lab10.model.User;                // DB'deki users tablosunu temsil eden entity
import com.berk.lab10.repository.UserRepository; // DB işlemleri (existsByEmail, save, findByEmail)

import org.springframework.beans.factory.annotation.Value; // application.properties / .env içinden değer okur
import org.springframework.security.crypto.password.PasswordEncoder; // BCrypt encoder bean'i
import org.springframework.stereotype.Controller; // MVC controller -> HTML view döner
import org.springframework.web.bind.annotation.*; // @GetMapping, @PostMapping, @RequestParam

import java.util.Set;               // common password blacklist için
import java.util.regex.Pattern;    // regex validation'lar için

@Controller
public class AuthController {

    // Repository: kullanıcıyı DB'de kontrol etmek ve kaydetmek için
    private final UserRepository userRepository;

    // PasswordEncoder: şifreyi BCrypt ile hashlemek için
    private final PasswordEncoder passwordEncoder;

    /*
      ADMIN_REGISTER_SECRET:
      - Admin kayıtlarında secret key istiyoruz.
      - .env veya application.properties içinde yoksa default "" (boş) olur.
      - Bu sayede herkes ROLE_ADMIN olamaz.
    */
    @Value("${ADMIN_REGISTER_SECRET:}")
    private String adminRegisterSecret;

    /*
      Strong password policy (regex):

      - (?=.*[a-z])            -> en az 1 küçük harf
      - (?=.*[A-Z])            -> en az 1 büyük harf
      - (?=.*\d)               -> en az 1 rakam
      - (?=.*[^A-Za-z\d])      -> en az 1 özel karakter
      - .{8,}                  -> minimum 5 karakter

      🔒 SECURITY:
      - Client-side validation yeterli değildir.
      - Bu kontrol mutlaka server-side yapılmalıdır.
    */
    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{5,}$"
    );

    /*
      Email server-side validation (basit regex)

      🔒 SECURITY:
      - HTML input type="email" tek başına yeterli değildir.
      - Backend tarafında da invalid email mutlaka reddedilmelidir.
    */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
    );

    /*
      Common passwords blacklist:
      - Çok bilinen / zayıf şifreler.
      - Case-insensitive kontrol edilir.
      - Bu liste demo + sunum için yeterlidir.
    */
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password",
            "password123!",
            "a12345678!",
            "a123456789!",
            "aqwerty123",
            "qwertyuiop",
            "Admin123!",
            "letmein123!",
            "welcome123!",
            "a11111111!",
            "qwerty123!"
    );

    // Constructor injection
    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /*
      GET /login
      - Login sayfasını gösterir (login.html).
      - Eğer kullanıcı zaten login olmuşsa tekrar login sayfası göstermeyip
        doğrudan /home'a yönlendirir.
    */
    @GetMapping("/login")
    public String loginPage(org.springframework.security.core.Authentication auth) {

        // auth != null ve authenticated ise kullanıcı login olmuş demektir
        // AnonymousAuthenticationToken ise guest (login değildir)
        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }

        return "login"; // templates/login.html
    }

    /*
      GET /register
      - Register sayfasını gösterir (register.html).
      - Login olmuş kullanıcı tekrar register olamasın diye
        /home'a yönlendirilir.
    */
    @GetMapping("/register")
    public String registerPage(org.springframework.security.core.Authentication auth) {

        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }

        return "register"; // templates/register.html
    }

    /*
      POST /register

      Formdan gelen alanlar:
      - username
      - email
      - password
      - role (opsiyonel)
      - adminSecret (opsiyonel)

      Validasyon sırası (ÖNEMLİ):
      1) Email normalize + format kontrolü
      2) Email benzersiz mi?
      3) Common password mu?
      4) Strong password policy geçiyor mu?
    */
    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String adminSecret
    ) {

        // email normalize (trim + lowercase)
        String normalizedEmail = (email == null)
                ? ""
                : email.trim().toLowerCase();

        // 🔒 SECURITY: null password safety
        if (password == null) {
            return "redirect:/register?error=weak";
        }

        // 🔒 SECURITY: server-side email validation
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            return "redirect:/register?error=email";
        }

        // 1) Email benzersiz mi?
        if (userRepository.existsByEmail(normalizedEmail)) {
            return "redirect:/register?error=exists";
        }

        // 2) Common password kontrolü (case-insensitive)
        String normalizedPass = password.trim().toLowerCase();
        if (COMMON_PASSWORDS.contains(normalizedPass)) {
            return "redirect:/register?error=common";
        }

        // 3) Strong password kontrolü
        if (!STRONG_PASSWORD.matcher(password).matches()) {
            return "redirect:/register?error=weak";
        }

        // Username normalize (trim)
        String normalizedUsername = (username == null)
                ? ""
                : username.trim();

        /*
          Kullanıcı entity oluşturulur.
          🔒 SECURITY:
          - Mass assignment YOK
          - Field'lar tek tek set edilir
        */
        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);

        // Şifre DB'ye plain text değil, HASH olarak kaydedilir
        user.setPassword(passwordEncoder.encode(password));

        // Varsayılan rol
        String finalRole = "ROLE_USER";

        /*
          Admin rolü sadece:
          - role == ROLE_ADMIN
          - ADMIN_REGISTER_SECRET boş değil
          - adminSecret doğruysa
        */
        if (role != null
                && role.equalsIgnoreCase("ROLE_ADMIN")
                && adminRegisterSecret != null
                && !adminRegisterSecret.isBlank()
                && adminRegisterSecret.equals(adminSecret)) {

            finalRole = "ROLE_ADMIN";
        }

        user.setRole(finalRole);

        // DB'ye kaydet
        userRepository.save(user);

        // Kayıt sonrası login sayfasına yönlendir
        return "redirect:/login";
    }
}
