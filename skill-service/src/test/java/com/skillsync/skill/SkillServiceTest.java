package com.skillsync.skill;

import com.skillsync.skill.dto.SkillRequest;
import com.skillsync.skill.dto.SkillResponse;
import com.skillsync.skill.entity.Skill;
import com.skillsync.skill.repository.SkillRepository;
import com.skillsync.skill.service.SkillService;
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
public class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    private Skill mockSkill;
    private SkillRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockSkill = new Skill();
        mockSkill.setId(1L);
        mockSkill.setName("Java");
        mockSkill.setCategory("Programming");
        mockSkill.setDescription("Java programming language");

        mockRequest = new SkillRequest();
        mockRequest.setName("Java");
        mockRequest.setCategory("Programming");
        mockRequest.setDescription("Java programming language");
    }

    // ===================== GET ALL SKILLS TESTS =====================

    @Test
    void getAllSkills_Success() {
        when(skillRepository.findAll()).thenReturn(List.of(mockSkill));

        List<SkillResponse> result = skillService.getAllSkills();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java", result.get(0).getName());
        verify(skillRepository, times(1)).findAll();
    }

    @Test
    void getAllSkills_ReturnsEmptyList_WhenNoSkills() {
        when(skillRepository.findAll()).thenReturn(List.of());

        List<SkillResponse> result = skillService.getAllSkills();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ===================== GET SKILL BY ID TESTS =====================

    @Test
    void getSkillById_Success() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(mockSkill));

        SkillResponse result = skillService.getSkillById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Java", result.getName());
    }

    @Test
    void getSkillById_ThrowsException_WhenNotFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            skillService.getSkillById(99L)
        );
    }

    // ===================== CREATE SKILL TESTS =====================

    @Test
    void createSkill_Success() {
        when(skillRepository.existsByName("Java")).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenReturn(mockSkill);

        SkillResponse result = skillService.createSkill(mockRequest);

        assertNotNull(result);
        assertEquals("Java", result.getName());
        assertEquals("Programming", result.getCategory());
        verify(skillRepository, times(1)).save(any(Skill.class));
    }

    @Test
    void createSkill_ThrowsException_WhenSkillAlreadyExists() {
        when(skillRepository.existsByName("Java")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
            skillService.createSkill(mockRequest)
        );
        verify(skillRepository, never()).save(any());
    }

    // ===================== UPDATE SKILL TESTS =====================

    @Test
    void updateSkill_Success() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(mockSkill));
        when(skillRepository.save(any(Skill.class))).thenReturn(mockSkill);

        SkillResponse result = skillService.updateSkill(1L, mockRequest);

        assertNotNull(result);
        assertEquals("Java", result.getName());
        verify(skillRepository, times(1)).save(any(Skill.class));
    }

    @Test
    void updateSkill_ThrowsException_WhenSkillNotFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            skillService.updateSkill(99L, mockRequest)
        );
        verify(skillRepository, never()).save(any());
    }

    // ===================== DELETE SKILL TESTS =====================

    @Test
    void deleteSkill_Success() {
        when(skillRepository.existsById(1L)).thenReturn(true);
        doNothing().when(skillRepository).deleteById(1L);

        assertDoesNotThrow(() -> skillService.deleteSkill(1L));
        verify(skillRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteSkill_ThrowsException_WhenSkillNotFound() {
        when(skillRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
            skillService.deleteSkill(99L)
        );
        verify(skillRepository, never()).deleteById(any());
    }
}
