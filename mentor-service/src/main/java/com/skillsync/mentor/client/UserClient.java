package com.skillsync.mentor.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.skillsync.mentor.dto.UserDTO;
import org.springframework.web.bind.annotation.RequestParam;
@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users/{id}")
    UserDTO getUserById(@PathVariable Long id);
    @GetMapping("/users/search")
    UserDTO getUserByName(@RequestParam String name);
}