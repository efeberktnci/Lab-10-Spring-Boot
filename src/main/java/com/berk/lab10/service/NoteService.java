package com.berk.lab10.service;

import com.berk.lab10.dto.NoteRequest;
import com.berk.lab10.dto.NoteResponse;
import com.berk.lab10.model.Note;
import com.berk.lab10.repository.NoteJdbcRepository;
import com.berk.lab10.repository.NoteRepository;
import com.berk.lab10.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteJdbcRepository noteJdbcRepository;
    private final UserRepository userRepository;

    public NoteService(
            NoteRepository noteRepository,
            NoteJdbcRepository noteJdbcRepository,
            UserRepository userRepository
    ) {
        this.noteRepository = noteRepository;
        this.noteJdbcRepository = noteJdbcRepository;
        this.userRepository = userRepository;
    }

    //  Authenticated user id
    private Integer getCurrentUserId(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED))
                .getId();
    }

    //  LIST (raw SQL – prepared statement)
    public List<NoteResponse> getMyNotes(Authentication auth) {
        Integer userId = getCurrentUserId(auth);
        return noteJdbcRepository.findAllByUserId(userId);
    }

    //  CREATE
    public void createNote(NoteRequest req, Authentication auth) {
        Integer userId = getCurrentUserId(auth);

        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(req.getTitle());
        note.setContent(req.getContent());

        noteRepository.save(note);
    }

    public Note findMyNote(Integer id, Authentication auth) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Integer userId = getCurrentUserId(auth);

        return noteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }


    // UPDATE
    public void updateNote(Integer id, NoteRequest req, Authentication auth) {
        Note note = findMyNote(id, auth);
        note.setTitle(req.getTitle());
        note.setContent(req.getContent());
        noteRepository.save(note);
    }

    //  DELETE
    public void deleteNote(Integer id, Authentication auth) {
        Note note = findMyNote(id, auth);
        noteRepository.delete(note);
    }
}
