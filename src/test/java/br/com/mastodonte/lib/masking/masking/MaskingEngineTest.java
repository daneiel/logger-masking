package br.com.mastodonte.lib.masking.masking;

import br.com.mastondonte.lib.masking.masking.CustomMaskingStrategy;
import br.com.mastondonte.lib.masking.masking.Mask;
import br.com.mastondonte.lib.masking.masking.MaskingEngine;
import br.com.mastondonte.lib.masking.masking.MaskingStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes para o Motor de Mascaramento (MaskingEngine)")
class MaskingEngineTest {

    @Nested
    @DisplayName("Estratégias Padrão")
    class StandardStrategies {

        private record TestData(
                @Mask(strategy = MaskingStrategy.FULL) String full,
                @Mask(strategy = MaskingStrategy.KEEP_LAST_4) String keepLast4,
                @Mask(strategy = MaskingStrategy.KEEP_FIRST_4) String keepFirst4,
                @Mask(strategy = MaskingStrategy.EMAIL) String email,
                @Mask(strategy = MaskingStrategy.CPF_CNPJ) String cpf,
                @Mask(strategy = MaskingStrategy.CPF_CNPJ) String cnpj
        ) {}

        @Test
        @DisplayName("Deve aplicar todas as estratégias de mascaramento corretamente")
        void shouldApplyAllStrategiesCorrectly() {
            // Arrange
            var data = new TestData(
                    "secret123",
                    "1234567890",
                    "1234567890",
                    "john.doe@example.com",
                    "12345678901",
                    "12345678901234"
            );

            // Act
            String result = MaskingEngine.mask(data);

            // Assert
            assertAll("Validação de todas as estratégias",
                    () -> assertTrue(result.contains("full=*********")),
                    () -> assertTrue(result.contains("keepLast4=******7890")),
                    () -> assertTrue(result.contains("keepFirst4=1234******")),
                    () -> assertTrue(result.contains("email=j******e@example.com")),
                    () -> assertTrue(result.contains("cpf=***.456.789-**")),
                    () -> assertTrue(result.contains("cnpj=**.345.678/****-**"))
            );
        }
    }

    @Nested
    @DisplayName("Estratégia Customizada")
    class CustomStrategy {

        // Uma implementação de estratégia customizada para o teste
        public static class ReverseMask implements CustomMaskingStrategy {
            public ReverseMask() {} // Construtor válido
            @Override
            public String mask(String value) {
                return new StringBuilder(value).reverse().toString();
            }
        }

        private record CustomData(
                @Mask(strategy = MaskingStrategy.CUSTOM, customStrategy = ReverseMask.class)
                String customField
        ) {}

        @Test
        @DisplayName("Deve aplicar a estratégia customizada definida pelo usuário")
        void shouldApplyCustomStrategy() {
            // Arrange
            var data = new CustomData("ABC-123");

            // Act
            String result = MaskingEngine.mask(data);

            // Assert
            assertTrue(result.contains("customField=321-CBA"));
        }

        @Test
        @DisplayName("Deve lançar exceção se a estratégia for CUSTOM mas nenhuma classe for fornecida")
        void shouldThrowExceptionForMissingCustomStrategyClass() {
            // Arrange
            record BadData(@Mask(strategy = MaskingStrategy.CUSTOM) String field) {}
            var data = new BadData("test");

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> MaskingEngine.mask(data));
        }

        // --- NOVO TESTE ADICIONADO ---
        @Test
        @DisplayName("Deve lançar MaskingException se a classe customizada não tiver um construtor público sem argumentos")
        void shouldThrowExceptionWhenCustomStrategyHasNoPublicNoArgConstructor() {
            // Arrange: Esta classe de estratégia só tem um construtor que aceita um argumento.
            class StrategyWithArgsConstructor implements CustomMaskingStrategy {
                public StrategyWithArgsConstructor(String prefix) {}
                @Override
                public String mask(String value) { return "masked"; }
            }

            // O record usa a estratégia mal configurada.
            record DataWithBadStrategy(
                    @Mask(strategy = MaskingStrategy.CUSTOM, customStrategy = StrategyWithArgsConstructor.class)
                    String field
            ) {}

            var data = new DataWithBadStrategy("test");

            // Act & Assert: Verifica se a nossa MaskingException é lançada.
            var exception = assertThrows(MaskingEngine.MaskingException.class, () -> {
                MaskingEngine.mask(data);
            });

            // Opcional, mas bom: verifica se a causa raiz foi a exceção de reflexão esperada.
            assertEquals(NoSuchMethodException.class, exception.getCause().getClass());
        }
    }
}
