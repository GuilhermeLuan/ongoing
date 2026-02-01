# Exception Module

Módulo de tratamento centralizado de exceções da aplicação.

## Componentes Principais

### GlobalExceptionHandler

Classe anotada com `@RestControllerAdvice` que intercepta exceções de todos os controllers.

**Handlers implementados**:

#### NotFoundException

```java
@ExceptionHandler(NotFoundException.class)
- Status: 404 NOT_FOUND
- Retorna: DefaultErrorMessage com status e mensagem
- Uso: Recurso não encontrado (subscription, user, etc.)
```

#### BadRequestException

```java
@ExceptionHandler(BadRequestException.class)
- Status: 400 BAD_REQUEST
- Retorna: DefaultErrorMessage com status e mensagem
- Uso: Regras de negócio violadas (email duplicado, etc.)
```

#### InvalidCredentialException

```java
@ExceptionHandler(InvalidCredentialException.class)
- Status: 401 UNAUTHORIZED
- Retorna: DefaultErrorMessage com status e mensagem
- Uso: Credenciais inválidas (login, refresh token)
```

#### MethodArgumentNotValidException

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
- Status: 400 BAD_REQUEST
- Retorna: ApiError com detalhes de validação
- Uso: Validação de Bean Validation falhou (@Valid)
- Combina todas mensagens de erro em uma string separada por vírgula
```

#### HttpMessageNotReadableException

```java
@ExceptionHandler(HttpMessageNotReadableException.class)
- Status: 400 BAD_REQUEST
- Retorna: DefaultErrorMessage genérica
- Uso: JSON malformado ou conversão de tipos falhou
```

## Exception Classes

### NotFoundException

```java
public class NotFoundException extends ResponseStatusException {
    - Extends: ResponseStatusException
    - Status: HttpStatus.NOT_FOUND
    - Constructor: (HttpStatus, String message)
}
```

### BadRequestException

```java
public class BadRequestException extends ResponseStatusException {
    - Extends: ResponseStatusException
    - Status: HttpStatus.BAD_REQUEST
    - Constructor: (HttpStatus, String message)
}
```

### InvalidCredentialException

```java
public class InvalidCredentialException extends ResponseStatusException {
    - Extends: ResponseStatusException
    - Status: HttpStatus.UNAUTHORIZED
    - Constructor: (HttpStatus, String message)
}
```

## Response DTOs

### DefaultErrorMessage

Record simples para erros padrão:

```java
public record DefaultErrorMessage(
    int status,
    String message
)
```

Exemplo de resposta:

```json
{
  "status": 404,
  "message": "Subscription not found"
}
```

### ApiError

Record detalhado para erros de validação:

```java
public record ApiError(
    String timestamp,    // ISO-8601 format
    int status,          // HTTP status code
    String error,        // Status reason phrase
    String path,         // Request URI
    String message       // Validation errors concatenated
)
```

Exemplo de resposta:

```json
{
  "timestamp": "2024-01-31T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/v1/subscriptions",
  "message": "name must not be blank, price must be positive"
}
```

## Padrões de Uso

### Lançando Exceções

```java
// Service layer
if (!subscriptionExists) {
    throw new NotFoundException(HttpStatus.NOT_FOUND, "Subscription not found");
}

if (emailAlreadyExists) {
    throw new BadRequestException(HttpStatus.BAD_REQUEST, "Email already exists");
}

if (invalidPassword) {
    throw new InvalidCredentialException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
}
```

### Validação com Bean Validation

```java
// DTO com anotações
public record SubscriptionRequest(
    @NotBlank(message = "name must not be blank")
    String name,

    @Positive(message = "price must be positive")
    BigDecimal price
)

// Controller
@PostMapping
public ResponseEntity<?> create(@Valid @RequestBody SubscriptionRequest request) {
    // Se validação falhar, MethodArgumentNotValidException é lançada
    // GlobalExceptionHandler captura e retorna ApiError
}
```

## Benefícios da Centralização

1. **Consistência**: Todas exceções retornam formato padronizado
2. **DRY**: Evita try-catch repetitivo nos controllers
3. **Separação de Responsabilidades**: Controllers focam em lógica, não em error handling
4. **Fácil Manutenção**: Mudanças no formato de erro em um único lugar

## Ordem de Precedência

Spring processa handlers na ordem mais específica primeiro:

1. Exceções customizadas específicas (NotFoundException, etc.)
2. Exceções Spring padrão (MethodArgumentNotValidException, etc.)
3. Exception genérica (se houver @ExceptionHandler(Exception.class))

## Testing

Testes de integração verificam:

- Status codes corretos
- Formato de resposta consistente
- Mensagens de erro adequadas
- Validação de Bean Validation
