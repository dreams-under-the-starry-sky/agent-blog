package com.blog.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminSessionController {
    @GetMapping("/session")
    public ResponseEntity<Void> session() {
        return ResponseEntity.ok().build();
    }
}
