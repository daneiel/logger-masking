package br.com.mastondonte.lib.masking.crypto;

import br.com.mastondonte.lib.masking.exceptions.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public final class EncryptionService {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BIT = 128;
    private final SecureRandom secureRandom = new SecureRandom();

    public String encrypt(String plaintext, SecretKey key) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Failed to encrypt data", e);
        }
    }

    public String decrypt(String base64CipherText, SecretKey key) {
        try {
            byte[] cipherTextWithIv = Base64.getDecoder().decode(base64CipherText);

            // ... (resto da sua lógica de descriptografia)
            ByteBuffer byteBuffer = ByteBuffer.wrap(cipherTextWithIv);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] plainTextBytes = cipher.doFinal(cipherText);
            return new String(plainTextBytes, StandardCharsets.UTF_8);

            // ✅ A LINHA ABAIXO É A CORREÇÃO ✅
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            throw new CryptoException("Failed to decrypt data. Check if the key is correct or if the data has been tampered with or is malformed.", e);
        }
    }


}