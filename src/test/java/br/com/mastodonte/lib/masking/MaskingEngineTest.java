package br.com.mastodonte.lib.masking;

import br.com.mastondonte.lib.masking.masking.Mask;
import br.com.mastondonte.lib.masking.masking.MaskingEngine;
import br.com.mastondonte.lib.masking.masking.MaskingStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskingEngineTest {

    private static class User {
        @Mask(strategy = MaskingStrategy.FULL)
        private final String password = "very-secret-password-123";

        @Mask(strategy = MaskingStrategy.KEEP_LAST_4)
        private final String creditCard = "1234-5678-9012-3456";

        private final String publicInfo = "This is public";
    }

    @Test
    @DisplayName("Should mask all fields correctly according to their annotations")
    void testMaskingOnAnnotatedObject() {
        User user = new User();
        String maskedResult = MaskingEngine.mask(user);

        assertTrue(maskedResult.contains("password=************************"));
        assertTrue(maskedResult.contains("creditCard=***************3456"));
        assertTrue(maskedResult.contains("publicInfo=This is public"));
        assertFalse(maskedResult.contains("very-secret-password-123"));
    }
}
