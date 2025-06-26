package br.com.mastondonte.lib.masking.logging;

import br.com.mastondonte.lib.masking.masking.MaskingEngine;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import org.slf4j.Marker;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.event.KeyValuePair;
import org.slf4j.helpers.MessageFormatter; // Importe a classe correta


public class MaskingPatternLayout extends PatternLayout {

    @Override
    public String doLayout(ILoggingEvent event) {
        // Envolve o evento original em nosso decorator para mascarar os argumentos
        return super.doLayout(new MaskedLoggingEvent(event) {
            @Override
            public List<Marker> getMarkerList() {
                return List.of();
            }

            @Override
            public long getTimeStamp() {
                return 0;
            }

            @Override
            public int getNanoseconds() {
                return 0;
            }

            @Override
            public long getSequenceNumber() {
                return 0;
            }

            @Override
            public List<KeyValuePair> getKeyValuePairs() {
                return List.of();
            }
        });
    }

    /**
     * Classe interna que implementa ILoggingEvent corretamente.
     */
    private static abstract class MaskedLoggingEvent implements ILoggingEvent {
        private final ILoggingEvent originalEvent;
        private final transient Object[] maskedArgumentArray;
        private transient String formattedMessage; // CAMPO ADICIONADO: Cache para a mensagem formatada

        public MaskedLoggingEvent(ILoggingEvent originalEvent) {
            this.originalEvent = originalEvent;
            if (originalEvent.getArgumentArray() != null) {
                Object[] originalArgs = originalEvent.getArgumentArray();
                this.maskedArgumentArray = new Object[originalArgs.length];
                for (int i = 0; i < originalArgs.length; i++) {
                    // Substitui o argumento por sua representação em String mascarada
                    this.maskedArgumentArray[i] = MaskingEngine.mask(originalArgs[i]);
                }
            } else {
                this.maskedArgumentArray = null;
            }
        }

        /**
         * MÉTODO CORRIGIDO E IMPLEMENTADO:
         * Gera a mensagem de log formatada usando os argumentos JÁ MASCARADOS.
         */
        @Override
        public String getFormattedMessage() {
            // Usa um cache para evitar reformatar a mesma mensagem repetidamente
            if (formattedMessage == null) {
                formattedMessage = MessageFormatter.arrayFormat(getMessage(), getArgumentArray()).getMessage();
            }
            return formattedMessage;
        }

        @Override
        public Object[] getArgumentArray() {
            return maskedArgumentArray;
        }

        // --- Delegação dos outros métodos para o evento original ---
        @Override public String getThreadName() { return originalEvent.getThreadName(); }
        @Override public ch.qos.logback.classic.Level getLevel() { return originalEvent.getLevel(); }
        @Override public String getMessage() { return originalEvent.getMessage(); }
        @Override public String getLoggerName() { return originalEvent.getLoggerName(); }
        @Override public LoggerContextVO getLoggerContextVO() { return originalEvent.getLoggerContextVO(); }
        @Override public IThrowableProxy getThrowableProxy() { return originalEvent.getThrowableProxy(); }
        @Override public StackTraceElement[] getCallerData() { return originalEvent.getCallerData(); }
        @Override public boolean hasCallerData() { return originalEvent.hasCallerData(); }
        @Override public Marker getMarker() { return originalEvent.getMarker(); }
        @Override public Map<String, String> getMDCPropertyMap() { return originalEvent.getMDCPropertyMap(); }
        @Override public Map<String, String> getMdc() { return originalEvent.getMdc(); }
        @Override public Instant getInstant() { return originalEvent.getInstant(); }
        @Override public void prepareForDeferredProcessing() { originalEvent.prepareForDeferredProcessing(); }
    }
}