package com.berk.lab10.service;

import com.berk.lab10.dto.UserResponse;
import com.berk.lab10.model.User;
import com.berk.lab10.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 * 
 * Tests the business logic of user operations without touching the database.
 * Uses Mockito to mock dependencies.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = new User();
        testUser1.setId(1);
        testUser1.setUsername("user1");
        testUser1.setEmail("user1@example.com");
        testUser1.setPassword("hashedPassword1");
        testUser1.setRole("ROLE_USER");

        testUser2 = new User();
        testUser2.setId(2);
        testUser2.setUsername("admin1");
        testUser2.setEmail("admin1@example.com");
        testUser2.setPassword("hashedPassword2");
        testUser2.setRole("ROLE_ADMIN");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser1, testUser2));

        // Act
        List<UserResponse> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        UserResponse first = result.get(0);
        assertEquals(1, first.getId());
        assertEquals("user1", first.getUsername());
        assertEquals("user1@example.com", first.getEmail());
        assertEquals("ROLE_USER", first.getRole());

        UserResponse second = result.get(1);
        assertEquals(2, second.getId());
        assertEquals("admin1", second.getUsername());
        assertEquals("ROLE_ADMIN", second.getRole());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<UserResponse> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(userRepository, times(1)).findAll();
    }
}
