package com.softbridge.config;

import com.softbridge.entity.User;
import com.softbridge.enums.Role;
import com.softbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Runs on startup — creates the Admin, Client, and Developer demo users if not present.
 */
@Slf4j
@Component
@org.springframework.context.annotation.Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.firstName}")
    private String adminFirstName;

    @Value("${app.admin.lastName}")
    private String adminLastName;

    @Override
    public void run(String... args) {
        // 1. Seed Admin User
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .firstName(adminFirstName)
                    .lastName(adminLastName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .company("SoftBridge")
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("✅ Admin user created: {}", adminEmail);
        } else {
            log.info("ℹ️  Admin user already exists: {}", adminEmail);
        }

        // 2. Seed Demo Client User
        String clientEmail = "client@softbridge.com";
        if (!userRepository.existsByEmail(clientEmail)) {
            User client = User.builder()
                    .firstName("Demo")
                    .lastName("Client")
                    .email(clientEmail)
                    .password(passwordEncoder.encode("Client@123")) // Matches frontend demo login
                    .company("Client Corp")
                    .role(Role.CLIENT)
                    .active(true)
                    .build();
            userRepository.save(client);
            log.info("✅ Demo Client user created: {}", clientEmail);
        }

        // 3. Seed Demo Developer User
        String devEmail = "dev@softbridge.com";
        if (!userRepository.existsByEmail(devEmail)) {
            User developer = User.builder()
                    .firstName("Demo")
                    .lastName("Developer")
                    .email(devEmail)
                    .password(passwordEncoder.encode("Dev@123")) // Matches frontend demo login
                    .company("SoftBridge Dev")
                    .role(Role.DEVELOPER)
                    .active(true)
                    .build();
            userRepository.save(developer);
            log.info("✅ Demo Developer user created: {}", devEmail);
        }
    }
}