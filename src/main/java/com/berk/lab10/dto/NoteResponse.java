/*
  NoteResponse.java (DTO)

  Bu dosyada ne yapılıyor?
  - Notes listesinde veya controller -> view tarafında gösterilecek "çıktı" modelidir.
  - Request DTO'dan farklıdır: sadece göstermek istediğimiz alanları içerir.
  - id ve createdAt gibi alanlar genelde response tarafında gösterilir.

*/

package com.berk.lab10.dto;

public class NoteResponse {

    // DB'deki notun id'si (primary key)
    private Integer id;

    // Not başlığı
    private String title;

    // Not içeriği
    private String content;

    // Oluşturulma zamanı (DB tarafından set edilen alan)
    private String createdAt;

    /*
      Constructor:
      - JDBC query veya Service mapping sırasında kolay oluşturmak için.
      - Örn: new NoteResponse(id, title, content, createdAt)
    */
    public NoteResponse(Integer id, String title, String content, String createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getter'lar: view / controller bu alanları okuyabilsin
    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCreatedAt() { return createdAt; }
}
