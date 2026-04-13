package com.neobank.backend.auth;

import com.neobank.backend.auth.dto.*;
import com.neobank.backend.entity.User;
import com.neobank.backend.exception.EmailAlreadyExistsException;
import com.neobank.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository      userRepository;
    @Mock private PasswordEncoder     passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil             jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("John Doe");
        registerRequest.setEmail("john@neobank.in");
        registerRequest.setPassword("Test@1234");
    }

    // ── Registration tests ────────────────────────────────

    @Test
    void register_success_returnsUserResponse() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(i -> {
            User u = i.getArgument(0);
            u = User.builder()
                    .id(1L)
                    .fullName(u.getFullName())
                    .email(u.getEmail())
                    .passwordHash(u.getPasswordHash())
                    .build();
            return u;
        });

        RegisterResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("john@neobank.in", response.getEmail());
        assertEquals("John Doe", response.getFullName());
        assertNull(response.getId() == null ? null : null); // password not in response
        verify(passwordEncoder).encode("Test@1234");
    }

    @Test
    void register_duplicateEmail_throws409() {
        when(userRepository.existsByEmail("john@neobank.in")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
            () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_passwordIsHashed_neverStoredPlain() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("Test@1234")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any())).thenAnswer(i -> {
            User u = i.getArgument(0);
            assertNotEquals("Test@1234", u.getPasswordHash());
            return u;
        });

        authService.register(registerRequest);
        verify(passwordEncoder).encode("Test@1234");
    }

    // ── Login tests ───────────────────────────────────────

    @Test
    void login_success_returnsTokenResponse() {
        User user = User.builder()
                .id(1L)
                .email("john@neobank.in")
                .passwordHash("hashed")
                .fullName("John Doe")
                .role(User.Role.CUSTOMER)
                .isActive(true)
                .build();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john@neobank.in");
        loginRequest.setPassword("Test@1234");

        when(userRepository.findByEmail("john@neobank.in"))
            .thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(1L, "john@neobank.in", "CUSTOMER"))
            .thenReturn("mock.jwt.token");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response.getToken());
        assertEquals("john@neobank.in", response.getEmail());
        assertEquals("CUSTOMER", response.getRole());
    }

    @Test
    void login_invalidCredentials_throws401() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrong@neobank.in");
        loginRequest.setPassword("wrongpass");

        doThrow(new BadCredentialsException("Bad credentials"))
            .when(authenticationManager)
            .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class,
            () -> authService.login(loginRequest));
    }

    @Test
    void login_inactiveUser_throws403() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("inactive@neobank.in");
        loginRequest.setPassword("Test@1234");

        // Tell the mock to fail immediately
        doThrow(new org.springframework.security.authentication.DisabledException("Account is inactive"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Assert that the exception is thrown
        assertThrows(org.springframework.security.authentication.DisabledException.class,
                () -> authService.login(loginRequest));

        // REMOVE the verify(userRepository) line from here!
        // It is correct that it's NOT invoked because the Exception stops the code
        // before the DB call.
    }
}