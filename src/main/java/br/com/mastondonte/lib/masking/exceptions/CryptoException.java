package br.com.mastondonte.lib.masking.exceptions;

public class CryptoException extends RuntimeException {
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
