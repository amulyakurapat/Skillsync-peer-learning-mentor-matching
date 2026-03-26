package com.skillsync.auth;

import com.skillsync.auth.client.UserClient;
import com.skillsync.auth.dto.RefreshTokenResponse;
import com.skillsync.auth.dto.UserProfileResponse;
import com.skillsync.auth.dto.ValidateResponse;
import com.skillsync.auth.dto.UserDto;
import com.skillsync.auth.entity.Role;
import com.skillsync.auth.entity.RoleName;
import com.skillsync.auth.entity.User;
import com.skillsync.auth.exception.InvalidCredentialsException;
import com.skillsync.auth.exception.RoleNotFoundException;
import com.skillsync.auth.exception.UserAlreadyExistsException;
import com.skillsync.auth.repository.RoleRepository;
import com.skillsync.auth.repository.UserRepository;
import com.skillsync.auth.security.JwtUtil;
import com.skillsync.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserClient userClient;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockRole = new Role();
        mockRole.setName(RoleName.ROLE_LEARNER);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("John");
        mockUser.setEmail("john@gmail.com");
        mockUser.setPassword("encodedPassword");
        mockUser.getRoles().add(mockRole);
    }

    // ===================== REGISTER TESTS =====================

    @Test
    void registerUser_Success() {
        when(userRepository.existsByEmail("john@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_LEARNER)).thenReturn(Optional.of(mockRole));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        doNothing().when(userClient).createUser(any(UserDto.class));

        User result = authService.registerUser("John", "john@gmail.com", "password123", "ROLE_LEARNER");

        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals("john@gmail.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userClient, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void registerUser_DefaultsToLearner_WhenRoleIsNull() {
        when(userRepository.existsByEmail("john@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_LEARNER)).thenReturn(Optional.of(mockRole));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        doNothing().when(userClient).createUser(any(UserDto.class));

        User result = authService.registerUser("John", "john@gmail.com", "password123", null);

        assertNotNull(result);
        verify(roleRepository).findByName(RoleName.ROLE_LEARNER);
    }

    @Test
    void registerUser_ThrowsException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("john@gmail.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
            authService.registerUser("John", "john@gmail.com", "password123", "ROLE_LEARNER")
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_ThrowsException_WhenAdminRoleRequested() {
        when(userRepository.existsByEmail("john@gmail.com")).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
            authService.registerUser("John", "john@gmail.com", "password123", "ROLE_ADMIN")
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_ThrowsException_WhenRoleNotFound() {
        when(userRepository.existsByEmail("john@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.ROLE_LEARNER)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () ->
            authService.registerUser("John", "john@gmail.com", "password123", "ROLE_LEARNER")
        );
    }

    // ===================== LOGIN TESTS =====================

    @Test
    void loginUser_Success() {
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        User result = authService.loginUser("john@gmail.com", "password123");

        assertNotNull(result);
        assertEquals("john@gmail.com", result.getEmail());
    }

    @Test
    void loginUser_ThrowsException_WhenEmailNotFound() {
        when(userRepository.findByEmail("wrong@gmail.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () ->
            authService.loginUser("wrong@gmail.com", "password123")
        );
    }

    @Test
    void loginUser_ThrowsException_WhenPasswordWrong() {
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () ->
            authService.loginUser("john@gmail.com", "wrongpassword")
        );
    }

    // ===================== VALIDATE TOKEN TESTS =====================

    @Test
    void validateToken_Success() {
        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        when(jwtUtil.extractEmail("validToken")).thenReturn("john@gmail.com");
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(mockUser));

        ValidateResponse result = authService.validateToken("validToken");

        assertTrue(result.isValid());
        assertEquals("john@gmail.com", result.getEmail());
    }

    @Test
    void validateToken_ReturnsFalse_WhenTokenInvalid() {
        when(jwtUtil.validateToken("badToken")).thenReturn(false);

        ValidateResponse result = authService.validateToken("badToken");

        assertFalse(result.isValid());
        assertNull(result.getEmail());
    }

    // ===================== GET ME TESTS =====================

    @Test
    void getMe_Success() {
        when(jwtUtil.extractEmail("validToken")).thenReturn("john@gmail.com");
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(mockUser));

        UserProfileResponse result = authService.getMe("validToken");

        assertNotNull(result);
        assertEquals("john@gmail.com", result.getEmail());
        assertEquals("John", result.getName());
    }

    @Test
    void getMe_ThrowsException_WhenUserNotFound() {
        when(jwtUtil.extractEmail("validToken")).thenReturn("unknown@gmail.com");
        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () ->
            authService.getMe("validToken")
        );
    }

    // ===================== REFRESH TOKEN TESTS =====================

    @Test
    void refreshToken_Success() {
        when(jwtUtil.validateToken("refreshToken")).thenReturn(true);
        when(jwtUtil.extractEmail("refreshToken")).thenReturn("john@gmail.com");
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken("john@gmail.com")).thenReturn("newAccessToken");
        when(jwtUtil.generateRefreshToken("john@gmail.com")).thenReturn("newRefreshToken");

        RefreshTokenResponse result = authService.refreshToken("refreshToken");

        assertNotNull(result);
        assertEquals("newAccessToken", result.getAccessToken());
        assertEquals("newRefreshToken", result.getRefreshToken());
    }

    @Test
    void refreshToken_ThrowsException_WhenTokenInvalid() {
        when(jwtUtil.validateToken("badToken")).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
            authService.refreshToken("badToken")
        );
    }

    // ===================== LOGOUT TESTS =====================

    @Test
    void logout_Success() {
        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        when(jwtUtil.extractEmail("validToken")).thenReturn("john@gmail.com");

        String result = authService.logout("validToken");

        assertEquals("Logged out successfully!", result);
    }

    @Test
    void logout_ThrowsException_WhenTokenInvalid() {
        when(jwtUtil.validateToken("badToken")).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
            authService.logout("badToken")
        );
    }
}
