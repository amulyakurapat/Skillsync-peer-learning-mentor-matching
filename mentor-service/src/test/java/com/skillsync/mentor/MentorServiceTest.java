package com.skillsync.mentor;

import com.skillsync.mentor.client.UserClient;
import com.skillsync.mentor.dto.MentorDTO;
import com.skillsync.mentor.dto.UserDTO;
import com.skillsync.mentor.entity.Mentor;
import com.skillsync.mentor.repository.MentorRepository;
import com.skillsync.mentor.service.MentorService;
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
public class MentorServiceTest {

    @Mock
    private MentorRepository mentorRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private MentorService mentorService;

    private Mentor mockMentor;
    private UserDTO mockUser;

    @BeforeEach
    void setUp() {
        mockMentor = new Mentor();
        mockMentor.setId(1L);
        mockMentor.setUserId(22L);
        mockMentor.setBio("Java Developer");
        mockMentor.setSkills("Java, Spring Boot");
        mockMentor.setAvailability("Weekend");

        mockUser = new UserDTO();
        mockUser.setId(22L);
        mockUser.setName("John");
        mockUser.setEmail("john@gmail.com");
    }

    // ===================== GET USER DETAILS TESTS =====================

    @Test
    void getUserDetails_Success() {
        when(userClient.getUserById(22L)).thenReturn(mockUser);

        UserDTO result = mentorService.getUserDetails(22L);

        assertNotNull(result);
        assertEquals("John", result.getName());
        verify(userClient, times(1)).getUserById(22L);
    }

    // ===================== APPLY MENTOR TESTS =====================

    @Test
    void applyMentor_Success() {
        when(userClient.getUserById(22L)).thenReturn(mockUser);
        when(mentorRepository.save(any(Mentor.class))).thenReturn(mockMentor);

        Mentor result = mentorService.applyMentor(mockMentor);

        assertNotNull(result);
        assertEquals(22L, result.getUserId());
        assertNull(mockMentor.getId()); // id must be null before save
        verify(mentorRepository, times(1)).save(any(Mentor.class));
    }

    @Test
    void applyMentor_ThrowsException_WhenUserNotFound() {
        when(userClient.getUserById(22L)).thenReturn(null);

        assertThrows(RuntimeException.class, () ->
            mentorService.applyMentor(mockMentor)
        );
        verify(mentorRepository, never()).save(any());
    }

    // ===================== GET ALL MENTORS TESTS =====================

    @Test
    void getAllMentors_Success() {
        when(mentorRepository.findAll()).thenReturn(List.of(mockMentor));
        when(userClient.getUserById(22L)).thenReturn(mockUser);

        List<MentorDTO> result = mentorService.getAllMentors();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getName());
    }

    @Test
    void getAllMentors_ReturnsUnknown_WhenUserServiceDown() {
        when(mentorRepository.findAll()).thenReturn(List.of(mockMentor));
        when(userClient.getUserById(22L)).thenThrow(new RuntimeException("User Service down"));

        List<MentorDTO> result = mentorService.getAllMentors();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Unknown", result.get(0).getName());
    }

    @Test
    void getAllMentors_ReturnsEmptyList_WhenNoMentors() {
        when(mentorRepository.findAll()).thenReturn(List.of());

        List<MentorDTO> result = mentorService.getAllMentors();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ===================== GET MENTOR BY ID TESTS =====================

    @Test
    void getMentorById_Success() {
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mockMentor));
        when(userClient.getUserById(22L)).thenReturn(mockUser);

        MentorDTO result = mentorService.getMentorById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());
    }

    @Test
    void getMentorById_ReturnsNull_WhenNotFound() {
        when(mentorRepository.findById(99L)).thenReturn(Optional.empty());

        MentorDTO result = mentorService.getMentorById(99L);

        assertNull(result);
    }

    // ===================== UPDATE AVAILABILITY TESTS =====================

    @Test
    void updateAvailability_Success() {
        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mockMentor));
        when(mentorRepository.save(any(Mentor.class))).thenReturn(mockMentor);

        Mentor result = mentorService.updateAvailability(1L, "Monday");

        assertNotNull(result);
        assertEquals("Monday", result.getAvailability());
        verify(mentorRepository, times(1)).save(any(Mentor.class));
    }

    @Test
    void updateAvailability_ReturnsNull_WhenMentorNotFound() {
        when(mentorRepository.findById(99L)).thenReturn(Optional.empty());

        Mentor result = mentorService.updateAvailability(99L, "Monday");

        assertNull(result);
        verify(mentorRepository, never()).save(any());
    }

    // ===================== GET MENTOR BY NAME TESTS =====================

    @Test
    void getMentorByName_Success() {
        when(userClient.getUserByName("John")).thenReturn(mockUser);
        when(mentorRepository.findByUserId(22L)).thenReturn(Optional.of(mockMentor));

        Mentor result = mentorService.getMentorByName("John");

        assertNotNull(result);
        assertEquals(22L, result.getUserId());
    }

    @Test
    void getMentorByName_ThrowsException_WhenUserNotFound() {
        when(userClient.getUserByName("Unknown")).thenReturn(null);

        assertThrows(RuntimeException.class, () ->
            mentorService.getMentorByName("Unknown")
        );
        verify(mentorRepository, never()).findByUserId(any());
    }

    @Test
    void getMentorByName_ThrowsException_WhenMentorNotFound() {
        when(userClient.getUserByName("John")).thenReturn(mockUser);
        when(mentorRepository.findByUserId(22L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            mentorService.getMentorByName("John")
        );
    }
}
