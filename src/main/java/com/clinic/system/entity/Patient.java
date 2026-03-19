package com.clinic.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullNameEnglish;
    private String fullNameArabic;

    @Column(unique = true, nullable = false)
    private String email;

    private String mobileNumber;

    private LocalDate dateOfBirth;

    @Column(unique = true)
    private String nationalId;

    @Embedded
    private Address address;

    private boolean isDeleted = false;

    private LocalDateTime createdAt;
}