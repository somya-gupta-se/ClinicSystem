package com.clinic.system.util;

import com.clinic.system.entity.User;
import com.clinic.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Initializing sample users...");
            List<User> users = Arrays.asList(
                    User.builder()
                            .username("admin")
                            .email("admin@clinic.com")
                            .password(passwordEncoder.encode("admin123"))
                            .fullName("System Administrator")
                            .createdAt(LocalDateTime.now())
                            .build(),
                    User.builder()
                            .username("doctor1")
                            .email("doctor@clinic.com")
                            .password(passwordEncoder.encode("doctor123"))
                            .fullName("Doctor User")
                            .createdAt(LocalDateTime.now())
                            .build(),
                    User.builder()
                            .username("reception")
                            .email("reception@clinic.com")
                            .password(passwordEncoder.encode("reception123"))
                            .fullName("Reception Staff")
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            userRepository.saveAll(users);
            log.info("Sample users initialized successfully");
        }
    }
}

