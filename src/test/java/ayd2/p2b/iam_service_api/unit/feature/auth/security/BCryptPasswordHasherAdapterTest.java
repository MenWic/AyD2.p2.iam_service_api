package ayd2.p2b.iam_service_api.unit.feature.auth.security;

import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.BCryptPasswordHasherAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BCryptPasswordHasherAdapterTest {

    @Mock private PasswordEncoder passwordEncoder;

    private BCryptPasswordHasherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BCryptPasswordHasherAdapter(passwordEncoder);
    }

    @Test
    void should_delegate_encode_to_password_encoder() {
        when(passwordEncoder.encode("raw")).thenReturn("encoded");

        String result = adapter.encode("raw");

        assertEquals("encoded", result);
        verify(passwordEncoder).encode("raw");
    }

    @Test
    void should_return_true_when_password_matches() {
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

        boolean result = adapter.matches("raw", "encoded");

        assertTrue(result);
        verify(passwordEncoder).matches("raw", "encoded");
    }

    @Test
    void should_return_false_when_password_does_not_match() {
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        boolean result = adapter.matches("wrong", "encoded");

        assertFalse(result);
        verify(passwordEncoder).matches("wrong", "encoded");
    }
}
