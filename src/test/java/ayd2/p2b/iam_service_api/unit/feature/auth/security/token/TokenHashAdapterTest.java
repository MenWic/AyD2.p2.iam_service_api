package ayd2.p2b.iam_service_api.unit.feature.auth.security.token;

import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.token.TokenHashAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenHashAdapterTest {

    private final TokenHashAdapter tokenHashAdapter = new TokenHashAdapter();

    @Test
    void should_return_same_hash_when_input_is_same() {
        String token = "refresh-token-value";

        String first = tokenHashAdapter.sha256(token);
        String second = tokenHashAdapter.sha256(token);

        assertEquals(first, second);
    }

    @Test
    void should_return_different_hashes_when_inputs_are_different() {
        String first = tokenHashAdapter.sha256("token-one");
        String second = tokenHashAdapter.sha256("token-two");

        assertNotEquals(first, second);
    }

    @Test
    void should_return_64_character_hex_hash_when_using_sha256() {
        String hash = tokenHashAdapter.sha256("sample-token");

        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    void should_not_expose_original_token_when_hashing() {
        String token = "refresh-token-value";
        String hash = tokenHashAdapter.sha256(token);

        assertNotEquals(token, hash);
        assertFalse(hash.contains(token));
    }
}

