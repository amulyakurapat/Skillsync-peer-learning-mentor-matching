package com.skillsync.userservice;

import com.skillsync.userservice.dto.UserDto;
import com.skillsync.userservice.dto.UserRequest;
import com.skillsync.userservice.dto.UserResponse;
import com.skillsync.userservice.entity.UserProfile;
import com.skillsync.userservice.exception.UserAlreadyExistsException;
import com.skillsync.userservice.exception.UserNotFoundException;
import com.skillsync.userservice.repository.UserRepository;
import com.skillsync.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserProfile mockUser;
    private UserRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockUser = new UserProfile();
        mockUser.setId(1L);
        mockUser.setName("John");
        mockUser.setEmail("john@gmail.com");
        mockUser.setBio("Java Developer");
        mockUser.setSkills("Java, Spring Boot");
        mockUser.setAvailability("Weekend");

        mockRequest = new UserRequest();
        mockRequest.setName("John");
        mockRequest.setEmail("john@gmail.com");
        mockRequest.setBio("Java Developer");
        mockRequest.setSkills("Java, Spring Boot");
        mockRequest.setAvailability("Weekend");
    }

    // ===================== CREATE USER TESTS =====================

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail("john@gmail.com")).thenReturn(false);
        when(userRepository.save(any(UserProfile.class))).thenReturn(mockUser);

        UserResponse result = userService.createUser(mockRequest);

        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals("john@gmail.com", result.getEmail());
        verify(userRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void createUser_ThrowsException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("john@gmail.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
            userService.createUser(mockRequest)
        );
        verify(userRepository, never()).save(any());
    }

    // ===================== GET USER BY ID TESTS =====================

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());
    }

    @Test
    void getUserById_ThrowsException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
            userService.getUserById(99L)
        );
    }

    // ===================== GET USER BY EMAIL TESTS =====================

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(mockUser));

        UserResponse result = userService.getUserByEmail("john@gmail.com");

        assertNotNull(result);
        assertEquals("john@gmail.com", result.getEmail());
    }

    @Test
    void getUserByEmail_ThrowsException_WhenEmailNotFound() {
        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
            userService.getUserByEmail("unknown@gmail.com")
        );
    }

    // ===================== GET USER BY NAME TESTS =====================

    @Test
    void getUserByName_Success() {
        when(userRepository.findByName("John")).thenReturn(Optional.of(mockUser));

        UserResponse result = userService.getUserByName("John");

        assertNotNull(result);
        assertEquals("John", result.getName());
    }

    @Test
    void getUserByName_ThrowsException_WhenNameNotFound() {
        when(userRepository.findByName("Unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            userService.getUserByName("Unknown")
        );
    }

    // ===================== CREATE USER FROM AUTH TESTS =====================

    @Test
    void createUserFromAuth_Success() {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("John");
        dto.setEmail("john@gmail.com");

        when(userRepository.save(any(UserProfile.class))).thenReturn(mockUser);

        UserResponse result = userService.createUserFromAuth(dto);

        assertNotNull(result);
        assertEquals("John", result.getName());
        verify(userRepository, times(1)).save(any(UserProfile.class));
    }

    // ===================== GET ALL USERS TESTS =====================

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser));

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getName());
    }

    @Test
    void getAllUsers_ReturnsEmptyList_WhenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ===================== UPDATE USER TESTS =====================

    @Test
    void updateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(UserProfile.class))).thenReturn(mockUser);

        UserResponse result = userService.updateUser(1L, mockRequest);

        assertNotNull(result);
        assertEquals("John", result.getName());
        verify(userRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void updateUser_ThrowsException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
            userService.updateUser(99L, mockRequest)
        );
        verify(userRepository, never()).save(any());
    }

    // ===================== DELETE USER TESTS =====================

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_ThrowsException_WhenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () ->
            userService.deleteUser(99L)
        );
        verify(userRepository, never()).deleteById(any());
    }
}
