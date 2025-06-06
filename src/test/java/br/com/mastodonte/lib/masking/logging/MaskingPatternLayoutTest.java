package br.com.mastodonte.lib.masking.logging;

import br.com.mastondonte.lib.masking.logging.MaskingPatternLayout;
import br.com.mastondonte.lib.masking.masking.Mask;
import br.com.mastondonte.lib.masking.masking.MaskingStrategy;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes para o Layout de Log com Mascaramento (MaskingPatternLayout)")
class MaskingPatternLayoutTest {

    // Um "Appender" customizado que armazena os eventos de log em uma lista em memória
    private static class ListAppender extends AppenderBase<ILoggingEvent> {
        private final List<ILoggingEvent> events = new ArrayList<>();

        @Override
        protected void append(ILoggingEvent eventObject) {
            events.add(eventObject);
        }
    }

    private Logger logger;
    private ListAppender listAppender;
    private MaskingPatternLayout layout; // Movido para ser uma variável de instância

    @BeforeEach
    void setUp() {
        // 1. Configura o logger e o appender em memória
        logger = (Logger) LoggerFactory.getLogger("TestLogger");
        listAppender = new ListAppender();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.INFO);
        logger.setAdditive(false);

        // 2. ✅ CORREÇÃO: Inicializa o layout e o associa ao contexto do logger
        layout = new MaskingPatternLayout();
        layout.setContext(logger.getLoggerContext()); // <-- Linha crucial para resolver o NPE
        layout.setPattern("%msg");
        layout.start();
    }

    @AfterEach
    void tearDown() {
        // Limpa o ambiente de log
        logger.detachAppender(listAppender);
        layout.stop();
        listAppender.stop();
    }

    private record UserData(
            @Mask(strategy = MaskingStrategy.EMAIL)
            String email,
            String nonSensitiveData
    ) {}

    @Test
    @DisplayName("Deve mascarar os campos anotados ao logar um objeto como argumento")
    void shouldMaskAnnotatedFieldsWhenLoggingObject() {
        // Arrange
        var user = new UserData("sensitive.data@company.com", "public info");

        // Act
        // Chamamos o logger da forma como um desenvolvedor faria na aplicação
        logger.info("Logando usuário: {}", user);

        // Assert
        List<ILoggingEvent> loggedEvents = listAppender.events;
        assertEquals(1, loggedEvents.size(), "Um evento de log deveria ter sido capturado.");

        // Agora usamos o layout que foi INICIALIZADO CORRETAMENTE no setUp()
        String formattedMessage = layout.doLayout(loggedEvents.get(0));

        // Verificamos o resultado final
        String expectedMaskedEmail = "email=s************a@company.com";
        String expectedPublicData = "nonSensitiveData=public info";

        assertTrue(formattedMessage.contains(expectedMaskedEmail), "O email deveria estar mascarado na mensagem final.");
        assertTrue(formattedMessage.contains(expectedPublicData), "O dado não sensível deveria estar visível.");
        assertFalse(formattedMessage.contains("sensitive.data@company.com"), "O email original não deveria aparecer no log.");
    }
}
