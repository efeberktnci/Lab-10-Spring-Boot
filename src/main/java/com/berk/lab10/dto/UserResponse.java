/**
 * UserResponse.java (DTO)
 *
 * Bu dosyada ne yapılıyor?
 * - Admin panelde kullanıcıları listelerken kullanılan "response DTO".
 * - Güvenlik için: password gibi hassas alanlar BURADA YOK.
 *
 * Neden önemli?
 * - Entity (User) direkt view'a verilirse yanlışlıkla password gibi alanlar sızabilir.
 * - DTO ile sadece izin verilen (whitelisted) alanlar döndürülür.
 */
package com.berk.lab10.dto;

public class UserResponse {

    private Integer id;
    private String username;
    private String email;
    private String role;

    // Constructor: service mapping sırasında hızlı DTO üretmek için
    public UserResponse(Integer id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // Getter'lar
    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
