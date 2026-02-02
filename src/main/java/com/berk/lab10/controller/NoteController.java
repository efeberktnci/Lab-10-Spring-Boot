/*
  NoteController.java

  Bu dosyada ne yapılıyor?
  - Lab 12'nin "secure CRUD feature" kısmı burada.
  - Notes için MVC CRUD routes:
      GET  /notes           -> liste
      GET  /notes/new       -> create form
      POST /notes           -> create submit
      GET  /notes/{id}/edit -> edit form
      POST /notes/{id}      -> update submit
      POST /notes/{id}/delete -> delete submit

  Güvenlik nerede?
  - Kullanıcının sadece kendi notlarına erişebilmesi Service katmanında zorlanır.
    (noteService.findMyNote / updateNote / deleteNote ownership check yapar)

  Validation nerede?
  - NoteRequest DTO @Valid ile validate edilir
  - BindingResult ile hatalıysa formu tekrar gösteririz

  Hocaya anlatım:
  "Controller handles MVC flow and validation.
   Ownership enforcement is in the service layer."
*/

package com.berk.lab10.controller;

import com.berk.lab10.dto.NoteRequest;     // input DTO (validation annotations içerir)
import com.berk.lab10.service.NoteService; // iş mantığı + access control

import jakarta.validation.Valid;           // DTO validation'ı tetikler

import org.springframework.security.core.Authentication; // logged-in user
import org.springframework.stereotype.Controller;        // MVC controller
import org.springframework.ui.Model;                     // view'a veri taşır
import org.springframework.validation.BindingResult;     // validation hataları burada
import org.springframework.web.bind.annotation.*;        // mappings

@Controller
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /*
      GET /notes
      - Kullanıcının kendi notlarını listeler.
      - auth -> hangi kullanıcı? (email)
      - noteService.getMyNotes(auth) -> DB'den sadece o user_id'nin notlarını getirir.
    */
    @GetMapping
    public String list(Model model, Authentication auth) {
        model.addAttribute("notes", noteService.getMyNotes(auth));
        return "notes-list"; // templates/notes-list.html
    }

    /*
      GET /notes/new
      - Boş create form döner.
      - model içine boş NoteRequest koyuyoruz ki thymeleaf th:object çalışsın.
    */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("note", new NoteRequest());
        return "note-form"; // templates/note-form.html
    }

    /*
      POST /notes
      - Create submit endpoint.
      - @Valid: NoteRequest üzerindeki validation annotation'larını çalıştırır.
      - BindingResult: hata varsa yakalarız, formu tekrar gösteririz.
    */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("note") NoteRequest note,
            BindingResult result,
            Authentication auth
    ) {
        // validation hatası varsa aynı formu döndür
        if (result.hasErrors()) {
            return "note-form";
        }

        // service create (user_id'yi auth üzerinden alıp note'a bağlar)
        noteService.createNote(note, auth);

        // başarılıysa listeye dön
        return "redirect:/notes";
    }

    /*
      GET /notes/{id}/edit
      - Edit form endpoint.
      - noteService.findMyNote(id, auth):
          - Eğer not yoksa veya kullanıcıya ait değilse -> 404 (service katmanında)
      - Bulunca DTO'ya title/content kopyalayıp formu doldururuz.
    */
    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Integer id,
            Model model,
            Authentication auth
    ) {
        var n = noteService.findMyNote(id, auth);

        // Form için DTO oluşturup mevcut verilerle dolduruyoruz
        NoteRequest req = new NoteRequest();
        req.setTitle(n.getTitle());
        req.setContent(n.getContent());

        model.addAttribute("note", req);
        model.addAttribute("noteId", id); // note-form.html action URL seçimi için
        return "note-form";
    }

    /*
      POST /notes/{id}
      - Update submit endpoint.
      - DTO validation var.
      - Ownership kontrolü noteService.updateNote içinde.
    */
    @PostMapping("/{id}")
    public String update(
            @PathVariable Integer id,
            @Valid @ModelAttribute("note") NoteRequest note,
            BindingResult result,
            Authentication auth
    ) {
        if (result.hasErrors()) {
            return "note-form";
        }

        noteService.updateNote(id, note, auth);
        return "redirect:/notes";
    }

    /*
      POST /notes/{id}/delete
      - Delete submit endpoint.
      - Ownership kontrolü noteService.deleteNote içinde.
      - CSRF token formda olduğu için güvenli.
    */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, Authentication auth) {
        noteService.deleteNote(id, auth);
        return "redirect:/notes";
    }
}
