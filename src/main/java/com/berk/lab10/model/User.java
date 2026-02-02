/*
  User.java (JPA Entity)

  Bu dosyada ne yapılıyor?
  - DB'deki "users" tablosunu temsil eden entity.
  - Spring Security login sırasında bu tablodan kullanıcıyı bulur.
  - password alanı HASH olarak saklanır (BCrypt).
  - role alanı ile authorization (USER/ADMIN) yapılır.

  Hocaya anlatım:
  "This is the database-backed user entity used for authentication and role-based authorization."
*/

package com.berk.lab10.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Kullanıcı adı boş olamaz
    @Column(nullable = false)
    private String username;

    // Email zorunlu ve unique
    @Column(nullable = false, unique = true)
    private String email;

    /*
      password:
      - DB'de hash olarak tutulur (plain text değil)
      - Login sırasında PasswordEncoder bunu verify eder
    */
    @Column(nullable = false)
    private String password;

    /*
      role:
      - ROLE_USER / ROLE_ADMIN
      - Varsayılan: ROLE_USER
      - SecurityConfig’de hasRole/hasAnyRole ile kontrol edilir
    */
    @Column(nullable = false)
    private String role = "ROLE_USER";

    // JPA için zorunlu boş constructor
    public User() {}

    // Getter & Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
