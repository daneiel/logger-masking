

# Logger-Masking

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/yourcompany/java-masking-lib)
[![Maven Central](https://img.shields.io/maven-central/v/com.yourcompany.security/java-masking-lib)](https://search.maven.org/artifact/com.yourcompany.security/java-masking-lib)

Uma biblioteca Java leve, poderosa e "plug-and-play" para mascarar dados sensíveis em objetos de forma declarativa usando anotações.

## Funcionalidades

* **Mascaramento por Anotações:** Adicione `@Mask` aos campos de seus POJOs e eles serão mascarados.
* **Estratégias Pré-definidas:** Inclui estratégias comuns como `FULL`, `KEEP_LAST_4`, `EMAIL`, `CPF_CNPJ` e mais.
* **Totalmente Extensível:** Crie sua própria lógica de mascaramento implementando a interface `CustomMaskingStrategy`.
* **Integração Automática com Logback:** Basta uma linha de alteração no seu `logback.xml` para mascarar dados em todos os seus logs automaticamente.
* **Modo de Depuração Seguro:** Registre valores originais de forma criptografada (`AES/GCM`) em um log separado.

## 1. Instalação

**Maven:**
```xml
<dependency>
    <groupId>br.com.mastodonte.lib.masking</groupId>
    <artifactId>logger-masking</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 2. Exemplo de implementacao
### Padrao
```java
public class User {
@Mask(strategy = MaskingStrategy.KEEP_LAST_4)
private String creditCard = "1234-5678-9012-3456";
}

User user = new User();
String masked = MaskingEngine.mask(user); // User{creditCard=****************3456}
```

### Customizado
```java
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

String data = new CustomData("ABC-123");

// Act
String result = MaskingEngine.mask(data); // CustomData{customField=321-CBA}
```