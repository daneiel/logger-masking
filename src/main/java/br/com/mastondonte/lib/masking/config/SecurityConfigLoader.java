package br.com.mastondonte.lib.masking.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

public final class SecurityConfigLoader {
    private static final String KEY_PROPERTY_NAME = "masking.security.key";
    private static final String KEY_ENV_VAR_NAME = "MASKING_SECURITY_KEY";
    private static final String PROPERTIES_FILE_PATH = "/masking.properties";
    private static final int EXPECTED_KEY_LENGTH_BYTES = 32;

    public Optional<SecretKey> loadKey() {
        String base64Key = System.getProperty(KEY_PROPERTY_NAME);
        if (base64Key == null || base64Key.isBlank()) {
            base64Key = System.getenv(KEY_ENV_VAR_NAME);
        }
        if (base64Key == null || base64Key.isBlank()) {
            try (InputStream input = SecurityConfigLoader.class.getResourceAsStream(PROPERTIES_FILE_PATH)) {
                if (input != null) {
                    Properties prop = new Properties();
                    prop.load(input);
                    base64Key = prop.getProperty(KEY_PROPERTY_NAME);
                }
            } catch (IOException e) {
                System.err.println("WARN: Could not read 'masking.properties' file: " + e.getMessage());
            }
        }
        if (base64Key == null || base64Key.isBlank()) {
            return Optional.empty();
        }
        return validateAndDecodeKey(base64Key);
    }

    private Optional<SecretKey> validateAndDecodeKey(String base64Key) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(base64Key);
            if (decodedKey.length != EXPECTED_KEY_LENGTH_BYTES) {
                System.err.println("WARN: Invalid key length. Expected " + EXPECTED_KEY_LENGTH_BYTES + " bytes, found " + decodedKey.length);
                return Optional.empty();
            }
            return Optional.of(new SecretKeySpec(decodedKey, "AES"));
        } catch (IllegalArgumentException e) {
            System.err.println("WARN: Security key is not a valid Base64 string.");
            return Optional.empty();
        }
    }
}
