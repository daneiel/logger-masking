package br.com.mastodonte.lib.masking.crypto;

import br.com.mastondonte.lib.masking.crypto.EncryptionService;
import br.com.mastondonte.lib.masking.exceptions.CryptoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes para o Serviço de Criptografia (EncryptionService)")
class EncryptionServiceTest {

    private EncryptionService encryptionService;
    private SecretKey validKey;
    private SecretKey wrongKey;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // Arrange: É executado antes de cada teste, garantindo um ambiente limpo.
        encryptionService = new EncryptionService();

        // Gera uma chave válida para os testes
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        this.validKey = keyGenerator.generateKey();

        // Gera uma segunda chave, diferente da primeira, para testar cenários de falha
        this.wrongKey = keyGenerator.generateKey();
    }

    @Test
    @DisplayName("Deve criptografar e descriptografar com sucesso, retornando o valor original")
    void shouldEncryptAndDecryptSuccessfully_whenGivenValidKey_thenReturnOriginalValue() {
        // Arrange
        String originalText = "Este é um segredo guardado a 7 chaves!";

        // Act
        String encryptedText = encryptionService.encrypt(originalText, validKey);
        String decryptedText = encryptionService.decrypt(encryptedText, validKey);

        // Assert
        assertNotNull(encryptedText, "O texto criptografado não deveria ser nulo.");
        assertNotEquals(originalText, encryptedText, "O texto criptografado deve ser diferente do original.");
        assertEquals(originalText, decryptedText, "O texto descriptografado deve ser idêntico ao original.");
    }

    @Test
    @DisplayName("Deve lançar CryptoException ao tentar descriptografar com a chave errada")
    void shouldThrowCryptoException_whenDecryptingWithWrongKey() {
        // Arrange
        String originalText = "informação confidencial";
        String encryptedText = encryptionService.encrypt(originalText, validKey);

        // Act & Assert
        // Verifica se a exceção esperada (CryptoException) é lançada ao usar a chave errada.
        assertThrows(CryptoException.class, () -> {
            encryptionService.decrypt(encryptedText, wrongKey);
        }, "Deveria lançar uma CryptoException ao usar a chave de descriptografia incorreta.");
    }

    @Test
    @DisplayName("Deve lançar CryptoException para um ciphertext em formato inválido (não-Base64)")
    void shouldThrowCryptoException_forMalformedCipherText() {
        // Arrange
        String malformedCipherText = "isto-não-é-um-formato-base64-válido!";

        // Act & Assert
        assertThrows(CryptoException.class, () -> {
            encryptionService.decrypt(malformedCipherText, validKey);
        }, "Deveria lançar uma CryptoException para dados malformados.");
    }
}
