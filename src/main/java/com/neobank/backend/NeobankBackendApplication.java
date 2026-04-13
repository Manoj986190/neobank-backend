package com.neobank.backend;

// import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.annotation.Bean;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import com.neobank.backend.entity.User;
// import com.neobank.backend.auth.UserRepository;

@SpringBootApplication
public class NeobankBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeobankBackendApplication.class, args);
	}

	// ← Add this bean, run app once, check console, then DELETE
    // @Bean
    // CommandLineRunner seedAdmin(UserRepository userRepository,
    //                             PasswordEncoder passwordEncoder) {
    //     return args -> {
    //         if (!userRepository.existsByEmail("admin@neobank.in")) {
    //             User admin = User.builder()
    //                     .fullName("NeoBank Admin")
    //                     .email("admin@neobank.in")
    //                     .passwordHash(passwordEncoder.encode("Admin@123"))
    //                     .role(User.Role.ADMIN)
    //                     .isActive(true)
    //                     .build();
    //             userRepository.save(admin);
    //             System.out.println("✅ Admin user created");
    //         } else {
    //             System.out.println("ℹ️ Admin already exists");
    //         }
    //     };
    // }

}
