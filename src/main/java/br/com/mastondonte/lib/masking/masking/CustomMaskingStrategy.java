package br.com.mastondonte.lib.masking.masking;

@FunctionalInterface
public interface CustomMaskingStrategy {
    String mask(String value);
}

/**
 * Placeholder class for the default value in the @Mask annotation.
 * Should not be used directly.
 */
final class None implements CustomMaskingStrategy {
    @Override
    public String mask(String value) {
        throw new UnsupportedOperationException("None strategy should not be invoked.");
    }
}
