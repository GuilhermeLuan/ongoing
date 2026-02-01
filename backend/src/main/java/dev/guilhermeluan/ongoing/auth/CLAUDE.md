# Authentication Module (auth/)

Módulo responsável pela autenticação JWT e gerenciamento de tokens de refresh.

## Componentes Principais

### AuthController

Endpoints REST para autenticação:

- `POST /api/v1/auth/register` - Registro de novos usuários
- `POST /api/v1/auth/login` - Login com email/senha
- `POST /api/v1/auth/refresh` - Renovação do access token usando refresh token

### AuthService

Lógica de negócio da autenticação:

- **register()** - Cria novo usuário, valida email único, gera tokens
- **login()** - Valida credenciais com BCrypt, gera tokens
- **refreshToken()** - Valida refresh token, deleta o usado, gera novos tokens

**Importante**: Todos os métodos são `@Transactional` para garantir atomicidade.

### RefreshTokenService

Gerenciamento específico de refresh tokens:

- **findByTokenOrThrowInvalidCredentialException()** - Busca token ou lança exceção
- **isExpired()** - Verifica se token expirou
- **deleteTokenInNewTransaction()** - Deleta token expirado em transação separada

**Pattern importante**: Tokens expirados são deletados em transação separada (`REQUIRES_NEW`) para evitar rollback se a
exceção for lançada.

### JWT Package (jwt/)

#### JwtService

Serviço para criação e validação de JWT tokens:

- **createToken()** - Gera JWT com claims: userId, role, email (subject)
- **extractUsername/UserId/Role()** - Extrai claims do token
- **isTokenValid()** - Valida assinatura e expiração

Usa JJWT (io.jsonwebtoken) com HMAC-SHA256.

#### JwtAuthFilter

Filtro Spring Security que intercepta requests:

1. Extrai JWT do header `Authorization: Bearer <token>`
2. Valida token com JwtService
3. Cria UserPrincipal e seta no SecurityContext
4. Permite acesso ao endpoint se autenticado

#### UserPrincipal

Record que representa o usuário autenticado no contexto do Spring Security.
Contém apenas `id` e `email` - usado para extrair dados do usuário autenticado nos controllers.

## DTOs

Todos são Java records em `dto/`:

- **RegisterRequest** - name, email, password (validações com Bean Validation)
- **LoginRequest** - email, password
- **RefreshRequest** - refreshToken
- **AuthResponse** - accessToken, refreshToken (retornado no registro/login/refresh)

## Fluxo de Autenticação

1. **Register/Login**:
    - Valida credenciais
    - Gera access token JWT (curta duração)
    - Gera refresh token UUID (longa duração, salvo no banco)
    - Retorna ambos tokens

2. **Request Autenticado**:
    - Cliente envia JWT no header Authorization
    - JwtAuthFilter valida e seta UserPrincipal no SecurityContext
    - Controller acessa via `Authentication auth` parameter

3. **Token Refresh**:
    - Cliente envia refresh token
    - Sistema valida, deleta o refresh token usado
    - Gera novos access + refresh tokens (rotation)

## Segurança

- Senhas hasheadas com BCrypt (via PasswordEncoder)
- JWT assinado com chave secreta (HMAC-SHA256)
- Refresh tokens são UUIDs únicos, one-time use (deletados após uso)
- Tokens expirados são automaticamente deletados
- Stateless - sem sessão server-side

## Configuração

Definidas em `application.yaml`:

- `security.jwt.secret` - Chave secreta para assinar JWTs
- `security.jwt.expiration` - Tempo de expiração do access token (ms)
- `security.jwt.refresh-expiration` - Tempo de expiração do refresh token (ms)

## Tratamento de Erros

- Email já existe → `BadRequestException`
- Credenciais inválidas → `InvalidCredentialException` (401)
- Refresh token inválido/expirado → `InvalidCredentialException` (401)

Todos tratados pelo `GlobalExceptionHandler`.

## Testing

Testes de integração em `AuthControllerIT`:

- Testam fluxo completo de registro, login e refresh
- Usam Testcontainers com PostgreSQL real
- Validam geração e rotação de tokens
