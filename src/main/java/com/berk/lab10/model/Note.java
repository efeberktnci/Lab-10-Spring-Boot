/*
  Note.java (JPA Entity)

  Bu dosyada ne yapılıyor?
  - DB'deki "notes" tablosunu temsil eden entity.
  - JPA/Hibernate bu sınıf üzerinden tabloya okuma-yazma yapar.
  - userId alanı sayesinde notu hangi kullanıcıya ait olduğunu tutar (ownership).

  Önemli güvenlik noktası:
  - Notes tablosunda user_id var -> "Users may only access their own data" kuralının temeli.

  createdAt neden insertable=false/updatable=false?
  - createdAt değeri DB tarafından otomatik set edilir (DEFAULT CURRENT_TIMESTAMP gibi).
  - Uygulama bu alanı elle set edemesin diye JPA tarafında kilitlenir.
*/

package com.berk.lab10.model;

import jakarta.persistence.*;

@Entity
@Table(name = "notes")
public class Note {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /*
      user_id:
      - Bu not hangi kullanıcıya ait?
      - Ownership enforcement için kritik: her not bir user'a bağlı.
    */
    @Column(name="user_id", nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private String title;

    /*
      content:
      - length=2000: DB kolon limitine uyumlu
      - Validation'da da max 2000 var (NoteRequest)
    */
    @Column(nullable = false, length = 2000)
    private String content;

    /*
      created_at:
      - DB tarafından set edilir (insertable=false, updatable=false)
      - Uygulama değiştirmesin diye.
      - Tipi String şu an, istersen LocalDateTime da kullanılabilir ama bu hali sunum için yeterli.
    */
    @Column(name="created_at", nullable = false, insertable = false, updatable = false)
    private String createdAt;

    // JPA için boş constructor zorunlu
    public Note() {}

    // Getter/Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
