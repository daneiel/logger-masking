package br.com.mastondonte.lib.masking.masking;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MaskingEngine {
    private static final Map<Class<? extends CustomMaskingStrategy>, CustomMaskingStrategy> strategyCache = new ConcurrentHashMap<>();

    private MaskingEngine() {}

    public static String mask(Object data) {
        if (data == null) {
            return "null";
        }
        Class<?> clazz = data.getClass();
        String fieldsRepresentation = Stream.of(clazz.getDeclaredFields())
                .map(field -> formatField(field, data))
                .collect(Collectors.joining(", "));
        return clazz.getSimpleName() + "{" + fieldsRepresentation + "}";
    }

    private static String formatField(Field field, Object data) {
        try {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = field.get(data);
            if (value == null) {
                return fieldName + "=null";
            }
            if (field.isAnnotationPresent(Mask.class) && value instanceof String) {
                Mask maskAnnotation = field.getAnnotation(Mask.class);
                String stringValue = (String) value;
                if (maskAnnotation.strategy() == MaskingStrategy.CUSTOM) {
                    return fieldName + "=" + applyCustomMask(stringValue, maskAnnotation.customStrategy());
                }
                return fieldName + "=" + applyMask(stringValue, maskAnnotation.strategy());
            } else {
                return fieldName + "=" + value.toString();
            }
        } catch (IllegalAccessException e) {
            throw new MaskingException("Failed to access field: " + field.getName(), e);
        }
    }

    private static String applyCustomMask(String value, Class<? extends CustomMaskingStrategy> customStrategyClass) {
        if (customStrategyClass == None.class) {
            throw new IllegalArgumentException("MaskingStrategy.CUSTOM requires a valid customStrategy class.");
        }
        CustomMaskingStrategy strategy = strategyCache.computeIfAbsent(customStrategyClass, key -> {
            try {
                return key.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new MaskingException("Failed to instantiate custom strategy: " + key.getName(), e);
            }
        });
        return strategy.mask(value);
    }

    private static String applyMask(String value, MaskingStrategy strategy) {
        if (value == null || value.isEmpty()) return value;
        return switch (strategy) {
            case FULL -> "*".repeat(value.length());
            case KEEP_LAST_4 -> (value.length() <= 4) ? "*".repeat(value.length()) : "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
            case KEEP_FIRST_4 -> (value.length() <= 4) ? "*".repeat(value.length()) : value.substring(0, 4) + "*".repeat(value.length() - 4);
            case CPF_CNPJ -> maskCpfCnpj(value);
            case EMAIL -> maskEmail(value);
            default -> throw new UnsupportedOperationException("Strategy not implemented: " + strategy);
        };
    }

    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "*".repeat(email.length());
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (localPart.length() <= 2) return "*".repeat(localPart.length()) + domain;
        return localPart.charAt(0) + "*".repeat(localPart.length() - 2) + localPart.charAt(localPart.length() - 1) + domain;
    }

    private static String maskCpfCnpj(String doc) {
        String digitsOnly = doc.replaceAll("\\D", "");
        if (digitsOnly.length() == 11) return "***." + digitsOnly.substring(3, 6) + "." + digitsOnly.substring(6, 9) + "-**";
        if (digitsOnly.length() == 14) return "**." + digitsOnly.substring(2, 5) + "." + digitsOnly.substring(5, 8) + "/****-**";
        return "*".repeat(doc.length());
    }

    public static class MaskingException extends RuntimeException {
        public MaskingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
