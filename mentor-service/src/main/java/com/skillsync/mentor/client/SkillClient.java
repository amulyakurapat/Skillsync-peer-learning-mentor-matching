package com.skillsync.mentor.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.skillsync.mentor.dto.SkillDTO;
import java.util.List;

@FeignClient(name = "skill-service")
public interface SkillClient {

    @GetMapping("/skills/{id}")
    SkillDTO getSkillById(@PathVariable Long id);

    @GetMapping("/skills/search")
    SkillDTO getSkillByName(@RequestParam String name);

    @GetMapping("/skills")
    List<SkillDTO> getAllSkills();  // ✅ NEW
}