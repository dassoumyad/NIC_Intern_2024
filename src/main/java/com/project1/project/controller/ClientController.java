package com.project1.project.controller;


import com.project1.project.service.ClientService;
import com.project1.project.dto.SignUpRequestDto;
import com.project1.project.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping("/signup")
    public ResponseEntity<UserEntity> signup(@RequestParam String clientId, @RequestBody SignUpRequestDto signUpRequestDto) {
        return clientService.signup(clientId, signUpRequestDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam long mobileNo) {
        Optional<String> otp = clientService.login(mobileNo);
        return otp.map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().body("Invalid mobile number"));
    }
}