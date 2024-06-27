package com.project1.project.service;


import com.project1.project.config.DataConfig;
import com.project1.project.dto.SignUpRequestDto;
import com.project1.project.model.UserEntity;
import com.project1.project.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ClientService {
    @Autowired
    UserRepository userRepository;

    public Optional<String> login(long mobileNo) {
        Optional<UserEntity> optionalClient = userRepository.findByMobileNo(mobileNo);
        return optionalClient.isPresent() ? Optional.of(generateOtp()) : Optional.empty();
    }

    private String generateOtp() {
        return String.format("%04d", new Random().nextInt(10000));
    }

    public Optional<UserEntity> signup(String clientId, SignUpRequestDto signUpRequestDto) {
        if (!DataConfig.isValidDate(signUpRequestDto.getDob())) {
            throw new IllegalArgumentException("Invalid date format. Please use dd-MM-yyyy");
        }

        return userRepository.findByClientId(clientId)
                .map(client -> {
                    client.setMobile_no(signUpRequestDto.getMobileNo());
                    client.setEmail_id(signUpRequestDto.getEmailId());
                    client.setName(signUpRequestDto.getName());
                    client.setGender(signUpRequestDto.getGender());
                    client.setDob(signUpRequestDto.getDob());
                    client.setAddress(signUpRequestDto.getAddress());
                    return userRepository.save(client);
                });
    }
}