package br.com.mastodonte.lib.masking.config;

import br.com.mastondonte.lib.masking.config.SecurityConfigLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes para o Carregador de Configuração (SecurityConfigLoader)")
class SecurityConfigLoaderTest {

    private SecurityConfigLoader loader;
    private static final String TEST_KEY_PROP = "masking.security.key";
    private static final String VALID_BASE64_KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="; // 32 bytes válidos

    @BeforeEach
    void setUp() {
        loader = new SecurityConfigLoader();
        // Limpa a propriedade de sistema antes de cada teste
        System.clearProperty(TEST_KEY_PROP);
    }

    @AfterEach
    void tearDown() {
        // Garante que a propriedade de sistema seja limpa após cada teste
        System.clearProperty(TEST_KEY_PROP);
    }

    @Test
    @DisplayName("Deve carregar a chave a partir da Propriedade de Sistema com prioridade")
    void shouldLoadKeyFromSystemProperty() {
        // Arrange
        System.setProperty(TEST_KEY_PROP, VALID_BASE64_KEY);

        // Act
        Optional<SecretKey> key = loader.loadKey();

        // Assert
        assertTrue(key.isPresent(), "A chave deveria ser encontrada na propriedade de sistema.");
        assertEquals("AES", key.get().getAlgorithm());
    }

    @Test
    @DisplayName("Deve carregar a chave a partir do arquivo masking.properties se a prop de sistema não existir")
    void shouldLoadKeyFromPropertiesFile() {
        // Arrange: O arquivo está em src/test/resources

        // Act
        Optional<SecretKey> key = loader.loadKey();

        // Assert
        assertTrue(key.isPresent(), "A chave deveria ser encontrada no arquivo de propriedades.");
        assertEquals("k/HqK9+B6n4sT2wU8v9xZ4A/B6n4sT2wU8v9xZ4A/B4=",
                java.util.Base64.getEncoder().encodeToString(key.get().getEncoded()));
    }

    @Test
    @DisplayName("Deve retornar Optional.empty se nenhuma chave for encontrada")
    void shouldReturnEmptyWhenNoKeyFound() {
        // Arrange: Nenhuma propriedade de sistema ou de ambiente está configurada

        // Act
        Optional<SecretKey> key = loader.loadKey();

        // Assert
        assertTrue(key.isEmpty(), "Deveria retornar um Optional vazio se nenhuma chave for encontrada.");
    }

    @Test
    @DisplayName("Deve retornar Optional.empty para uma chave com tamanho incorreto")
    void shouldReturnEmptyForWrongKeyLength() {
        // Arrange
        System.setProperty(TEST_KEY_PROP, "dG9vLXNob3J0LWtleQ=="); // "too-short-key" em base64

        // Act
        Optional<SecretKey> key = loader.loadKey();

        // Assert
        assertTrue(key.isEmpty());
    }
}
