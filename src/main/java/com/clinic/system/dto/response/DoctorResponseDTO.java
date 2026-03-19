package com.clinic.system.dto.response;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponseDTO {
    private Long id;
    private String nameEnglish;
    private String nameArabic;
    private String specialty;
    private int yearsOfExperience;
    private int consultationDuration;
}

