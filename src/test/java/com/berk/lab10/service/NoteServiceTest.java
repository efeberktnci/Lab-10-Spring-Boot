package com.berk.lab10.service;

import com.berk.lab10.dto.NoteRequest;
import com.berk.lab10.dto.NoteResponse;
import com.berk.lab10.model.Note;
import com.berk.lab10.model.User;
import com.berk.lab10.repository.NoteJdbcRepository;
import com.berk.lab10.repository.NoteRepository;
import com.berk.lab10.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NoteService
 *
 * Tests:
 * - CRUD operations
 * - User data isolation (users can only access their own notes)
 * - Access control enforcement
 */
@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteJdbcRepository noteJdbcRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NoteService noteService;

    private User testUser;
    private User otherUser;
    private Note testNote;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setRole("ROLE_USER");

        otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("hashedPassword2");
        otherUser.setRole("ROLE_USER");

        testNote = new Note();
        testNote.setId(1);
        testNote.setTitle("Test Note");
        testNote.setContent("Test Content");
        testNote.setUserId(1);
    }

    @Test
    void createNote_ShouldSaveNoteWithCurrentUserId() {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        NoteRequest request = new NoteRequest();
        request.setTitle("New Note");
        request.setContent("New Content");

        // Act
        noteService.createNote(request, authentication);

        // Assert
        verify(noteRepository, times(1)).save(any(Note.class));
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void getMyNotes_ShouldReturnOnlyCurrentUserNotes() {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // NoteResponse requires 4 params: Integer, String, String, String
        List<NoteResponse> expectedNotes = Arrays.asList(
                new NoteResponse(1, "Note 1", "Content 1", "2026-02-02"),
                new NoteResponse(2, "Note 2", "Content 2", "2026-02-02")
        );
        when(noteJdbcRepository.findAllByUserId(1)).thenReturn(expectedNotes);

        // Act
        List<NoteResponse> result = noteService.getMyNotes(authentication);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(noteJdbcRepository, times(1)).findAllByUserId(1);
    }

    @Test
    void findMyNote_WithValidOwner_ShouldReturnNote() {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(noteRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(testNote));

        // Act
        Note result = noteService.findMyNote(1, authentication);

        // Assert
        assertNotNull(result);
        assertEquals("Test Note", result.getTitle());
        assertEquals(1, result.getUserId());
        verify(noteRepository, times(1)).findByIdAndUserId(1, 1);
    }

    @Test
    void findMyNote_WithInvalidId_ShouldThrowException() {
        // IMPORTANT:
        // Do NOT stub authentication/userRepository here, because service likely throws
        // before accessing them. Otherwise Mockito throws UnnecessaryStubbingException.

        // Act & Assert - Invalid ID (null)
        assertThrows(ResponseStatusException.class, () -> {
            noteService.findMyNote(null, authentication);
        });

        // Act & Assert - Invalid ID (0)
        assertThrows(ResponseStatusException.class, () -> {
            noteService.findMyNote(0, authentication);
        });

        // Act & Assert - Invalid ID (negative)
        assertThrows(ResponseStatusException.class, () -> {
            noteService.findMyNote(-1, authentication);
        });
    }

    @Test
    void findMyNote_WhenNoteDoesNotExist_ShouldThrowException() {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(noteRepository.findByIdAndUserId(999, 1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            noteService.findMyNote(999, authentication);
        });
    }

    @Test
    void findMyNote_WhenUserTriesToAccessOtherUsersNote_ShouldThrowException() {
        // Arrange - User 2 trying to access User 1's note
        when(authentication.getName()).thenReturn("other@example.com");
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(noteRepository.findByIdAndUserId(1, 2)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            noteService.findMyNote(1, authentication);
        });

        verify(noteRepository, times(1)).findByIdAndUserId(1, 2);
    }

    @Test
    void updateNote_WithValidOwner_ShouldUpdateNote() {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(noteRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        NoteRequest request = new NoteRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");

        // Act
        noteService.updateNote(1, request, authentication);

        // Assert
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void deleteNote_WithValidOwner_ShouldDeleteNote() {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(noteRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(testNote));

        // Act
        noteService.deleteNote(1, authentication);

        // Assert
        verify(noteRepository, times(1)).delete(testNote);
    }

    @Test
    void deleteNote_WhenUserTriesToDeleteOtherUsersNote_ShouldThrowException() {
        // Arrange - User 2 trying to delete User 1's note
        when(authentication.getName()).thenReturn("other@example.com");
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(noteRepository.findByIdAndUserId(1, 2)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            noteService.deleteNote(1, authentication);
        });

        verify(noteRepository, never()).delete(any(Note.class));
    }
}
