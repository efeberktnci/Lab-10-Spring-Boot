/*
  NoteRequest.java (DTO)

  Bu dosyada ne yapılıyor?
  - Notes Create/Update formundan gelen veriyi taşır (title, content).
  - Kullanıcıdan gelen input'u doğrulamak (validation) için annotation'lar içerir.
  - Controller'da @Valid ile kullanıldığında Spring otomatik validation yapar.
  - Validation hatası varsa BindingResult içinde errors oluşur ve form tekrar gösterilir.

*/

package com.berk.lab10.dto;

import jakarta.validation.constraints.NotBlank; // boş/whitespace input'u engeller
import jakarta.validation.constraints.Size;     // max uzunluk sınırı koyar

public class NoteRequest {

    /*
      title alanı:
      - @NotBlank: null, "" veya "   " (sadece boşluk) olamaz
      - @Size(max=100): 100 karakteri geçemez
      Neden?:
      - UI ve DB tarafında aşırı uzun/gereksiz input'u engellemek
      - Basit validation ile güvenlik + veri kalitesi
    */
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title max 100 chars")
    private String title;

    /*
      content alanı:
      - @NotBlank: boş olamaz
      - @Size(max=2000): 2000 karakter sınırı
      Neden?:
      - DB column length ile uyumlu
      - Aşırı büyük payload (DoS gibi) riskini azaltır
    */
    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content max 2000 chars")
    private String content;

    // Getter: Controller/Thymeleaf/Service title'ı okuyabilsin
    public String getTitle() { return title; }

    // Setter: Formdan gelen değeri Spring bu method ile set eder
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
