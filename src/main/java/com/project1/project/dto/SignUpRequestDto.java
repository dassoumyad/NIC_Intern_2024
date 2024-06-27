package com.project1.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequestDto {
    private long mobileNo;
    private String emailId;
    private String name;
    private String gender;
    private String dob;
    private String address;
}