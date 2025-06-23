package com.taskmanager.taskmanager;

import com.taskmanager.taskmanager.enums.Role;
import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Configuration
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(UserDataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User user = User.builder()
                    .nomUtilisateur("Admin")
                    .email("admin@task.com")
                    .password(passwordEncoder.encode("password"))
                    .role(Collections.singleton(Role.PROJECT_MANAGER))
                    .build();

            userRepository.save(user);
            log.info("✅ Utilisateur initial créé : admin@task.com / password1234");
        } else {
            log.info("ℹ️ Utilisateurs déjà existants, pas d’init nécessaire.");
        }
    }
}