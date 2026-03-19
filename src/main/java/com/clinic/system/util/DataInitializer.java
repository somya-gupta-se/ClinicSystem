package com.clinic.system.util;

import com.clinic.system.entity.Doctor;
import com.clinic.system.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DoctorRepository doctorRepository;



    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Override
    public void run(String... args) throws Exception {
        if (doctorRepository.count() == 0) {
            log.info("Initializing sample doctors...");
            List<Doctor> doctors = Arrays.asList(
                    Doctor.builder()
                            .nameEnglish("Dr. Ahmed Hassan")
                            .nameArabic("د. أحمد حسن")
                            .specialty("Cardiology")
                            .yearsOfExperience(15)
                            .consultationDuration(30)
                            .build(),
                    Doctor.builder()
                            .nameEnglish("Dr. Fatima Al-Rashid")
                            .nameArabic("د. فاطمة الراشد")
                            .specialty("Pediatrics")
                            .yearsOfExperience(12)
                            .consultationDuration(25)
                            .build(),
                    Doctor.builder()
                            .nameEnglish("Dr. Mohammed Al-Dosari")
                            .nameArabic("د. محمد الدوسري")
                            .specialty("Orthopedics")
                            .yearsOfExperience(18)
                            .consultationDuration(45)
                            .build(),
                    Doctor.builder()
                            .nameEnglish("Dr. Layla Al-Qahtani")
                            .nameArabic("د. ليلى القحطاني")
                            .specialty("Dermatology")
                            .yearsOfExperience(10)
                            .consultationDuration(20)
                            .build(),
                    Doctor.builder()
                            .nameEnglish("Dr. Samir Al-Otaibi")
                            .nameArabic("د. سمير العتيبي")
                            .specialty("Psychiatry")
                            .yearsOfExperience(8)
                            .consultationDuration(15)
                            .build()
            );

            doctorRepository.saveAll(doctors);
            log.info("Sample doctors initialized successfully");
        }
    }
}

