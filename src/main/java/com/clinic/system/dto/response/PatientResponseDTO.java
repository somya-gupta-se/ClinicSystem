package com.clinic.system.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponseDTO {
    private Long id;
    private String fullNameEnglish;
    private String fullNameArabic;
    private String email;
    private String mobileNumber;
    private LocalDate dateOfBirth;
    private String nationalId;
    private AddressDTO address;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDTO {
        private String street;
        private String city;
        private String region;
    }
}

