# Design: Autenticação e Autorização com JWT

**Data:** 2026-01-29
**Status:** Aprovado

---

## Resumo

Sistema de autenticação multi-tenant onde cada usuário vê apenas suas próprias subscriptions. Registro público (
self-service), roles User/Admin, e tokens JWT com refresh token para sessões longas.

---

## Decisões Técnicas

| Decisão              | Escolha            | Motivo                                                      |
|----------------------|--------------------|-------------------------------------------------------------|
| Modelo de acesso     | Multi-tenant       | Cada usuário gerencia suas próprias subscriptions           |
| Registro             | Self-service       | Qualquer pessoa pode criar conta                            |
| Roles                | User + Admin       | Flexível sem over-engineering                               |
| Estratégia de tokens | Access + Refresh   | Mais seguro, permite logout real                            |
| Biblioteca JWT       | jjwt               | Mais comum no ecossistema Spring, mais exemplos disponíveis |
| Dados do usuário     | Email, senha, nome | Mínimo necessário                                           |
| Migração             | Banco limpo        | Não há dados em prod/dev                                    |

---

## Estrutura de Pacotes

```
src/main/java/dev/guilhermeluan/ongoing/
├── auth/
│   ├── AuthController.java        # POST /register, /login, /refresh
│   ├── AuthService.java           # Lógica de autenticação
│   ├── dto/
│   │   ├── RegisterRequest.java   # email, password, name
│   │   ├── LoginRequest.java      # email, password
│   │   ├── AuthResponse.java      # accessToken, refreshToken
│   │   └── RefreshRequest.java    # refreshToken
│   └── jwt/
│       ├── JwtService.java        # Gerar/validar tokens
│       └── JwtAuthFilter.java     # Filtro Spring Security
├── user/
│   ├── User.java                  # Entidade
│   ├── UserRepository.java
│   └── Role.java                  # Enum: USER, ADMIN
└── config/
    └── SecurityConfig.java        # Configuração Spring Security
```

---

## Endpoints

### Públicos (sem autenticação)

| Método | Endpoint                | Descrição                 |
|--------|-------------------------|---------------------------|
| POST   | `/api/v1/auth/register` | Criar nova conta          |
| POST   | `/api/v1/auth/login`    | Autenticar e obter tokens |
| POST   | `/api/v1/auth/refresh`  | Renovar access token      |
| GET    | `/api/v1/status`        | Health check              |

### Protegidos (requer autenticação)

Todos os endpoints de `/api/v1/subscriptions/**`

---

## Entidades

### User

```java

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;  // BCrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;  // USER ou ADMIN

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### RefreshToken

```java

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;  // UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
```

### Role (Enum)

```java
public enum Role {
    USER,
    ADMIN
}
```

---

## Migrações Flyway

### V3__create_users_table.sql

Cria tabela `users` com campos: id, name, email (unique), password, role, created_at, updated_at.

### V4__create_refresh_tokens_table.sql

Cria tabela `refresh_tokens` com campos: id, token (unique), user_id (FK), expires_at.

### V5__add_user_id_to_subscriptions.sql

Adiciona coluna `user_id` (NOT NULL, FK) na tabela `subscriptions`.

---

## Fluxos de Autenticação

### 1. Registro

```
POST /api/v1/auth/register
Request:  { "name": "João", "email": "joao@email.com", "password": "senha123" }
Response: { "accessToken": "eyJ...", "refreshToken": "550e8400-e29b..." }
```

**Lógica:**

1. Valida se email já existe → se sim, retorna 400
2. Faz hash da senha com BCrypt
3. Salva usuário com role `USER`
4. Gera access token (JWT, 15min) + refresh token (UUID, salvo no banco, 7 dias)
5. Retorna ambos os tokens

### 2. Login

```
POST /api/v1/auth/login
Request:  { "email": "joao@email.com", "password": "senha123" }
Response: { "accessToken": "eyJ...", "refreshToken": "550e8400-e29b..." }
```

**Lógica:**

1. Busca usuário por email → não existe? 401
2. Verifica senha com BCrypt → não bate? 401
3. Gera novos tokens
4. Retorna tokens

### 3. Refresh

```
POST /api/v1/auth/refresh
Request:  { "refreshToken": "550e8400-e29b..." }
Response: { "accessToken": "eyJ...", "refreshToken": "novo-token..." }
```

**Lógica:**

1. Busca refresh token no banco → não existe? 401
2. Expirou? Deleta do banco e retorna 401
3. Gera novo access token + novo refresh token (rotação)
4. Deleta refresh token antigo, salva o novo
5. Retorna novos tokens

**Por que rotacionar o refresh token?**
Segurança extra — se alguém roubar o refresh token e usar, o token legítimo do usuário para de funcionar, sinalizando o
problema.

---

## Estrutura do Access Token (JWT)

```json
{
  "sub": "joao@email.com",
  "userId": 1,
  "role": "USER",
  "iat": 1706540400,
  "exp": 1706541300
}
```

- `sub`: email do usuário (subject padrão JWT)
- `userId`: ID para queries no banco
- `role`: para verificações de autorização
- `iat/exp`: issued at / expiration (15 minutos)

---

## JwtService

```java
public class JwtService {
    String generateAccessToken(User user);      // Cria JWT assinado

    String extractEmail(String token);          // Extrai subject

    Long extractUserId(String token);           // Extrai claim

    boolean isTokenValid(String token);         // Verifica assinatura + expiração
}
```

---

## JwtAuthFilter

Filtro que roda antes de cada request:

1. Pega header `Authorization: Bearer <token>`
2. Não tem token? Deixa passar (Spring Security decide se endpoint é público)
3. Tem token? Valida com `JwtService`
4. Token válido? Cria `Authentication` e coloca no `SecurityContext`
5. Token inválido/expirado? Não autentica (request continua, mas sem usuário)

---

## SecurityConfig

```java

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    return http
            .csrf(csrf -> csrf.disable())  // API stateless não precisa
            .sessionManagement(sess -> sess.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/v1/status").permitAll()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
}
```

---

## Configuração (application.yaml)

```yaml
jwt:
  secret: ${JWT_SECRET}  # Variável de ambiente, mínimo 256 bits
  access-token-expiration: 900000    # 15 minutos em ms
  refresh-token-expiration: 604800000  # 7 dias em ms
```

---

## Integração com Subscriptions

### Mudanças no Repository

```java
Page<Subscriptions> findAllByUserId(Long userId, Pageable pageable);

Optional<Subscriptions> findByIdAndUserId(Long id, Long userId);
```

### Mudanças no Controller

```java

@GetMapping
public Page<SubscriptionResponse> list(Pageable pageable, Authentication auth) {
    Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
    return service.findAll(userId, pageable);
}
```

Isso garante que um usuário **nunca** acesse subscription de outro — a query já filtra no banco.

---

## Tratamento de Erros

| Situação                          | HTTP Status      | Mensagem                   |
|-----------------------------------|------------------|----------------------------|
| Email já cadastrado               | 400 Bad Request  | "Email already registered" |
| Credenciais inválidas             | 401 Unauthorized | "Invalid credentials"      |
| Token expirado/inválido           | 401 Unauthorized | "Token expired or invalid" |
| Refresh token não encontrado      | 401 Unauthorized | "Invalid refresh token"    |
| Acesso negado (role insuficiente) | 403 Forbidden    | "Access denied"            |

### Validações no RegisterRequest

```java
public record RegisterRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {}
```

---

## Estratégia de Testes

### AuthControllerIT

```
Registro:
- POST /register com dados válidos → 201, retorna tokens válidos
- POST /register com email duplicado → 400
- POST /register com email inválido → 400
- POST /register com senha curta → 400

Login:
- POST /login com credenciais corretas → 200, retorna tokens
- POST /login com senha errada → 401
- POST /login com email inexistente → 401

Refresh:
- POST /refresh com token válido → 200, novos tokens funcionam
- POST /refresh com token expirado → 401
- POST /refresh com token inexistente → 401

Proteção de endpoints:
- GET /subscriptions sem token → 401
- GET /subscriptions com token expirado → 401
- GET /subscriptions com token válido → 200
```

### SubscriptionsControllerIT

Adaptar os testes existentes:

- `@BeforeEach`: cria usuário e obtém token
- Todas as requests incluem header `Authorization: Bearer <token>`
- Adicionar teste: usuário A não vê subscriptions do usuário B

---

## Ordem de Implementação

### Fase 1: Infraestrutura

1. Adicionar dependências no `pom.xml` (Spring Security, jjwt)
2. Criar migrations Flyway (users, refresh_tokens, user_id em subscriptions)
3. Configurar properties JWT no `application.yaml`

### Fase 2: Core de Autenticação

4. Criar entidades `User`, `Role`, `RefreshToken`
5. Criar repositories
6. Implementar `JwtService` (gerar/validar tokens)
7. Implementar `JwtAuthFilter`
8. Configurar `SecurityConfig`

### Fase 3: Endpoints de Auth

9. Criar DTOs (RegisterRequest, LoginRequest, AuthResponse, RefreshRequest)
10. Implementar `AuthService`
11. Implementar `AuthController`

### Fase 4: Integração com Subscriptions

12. Modificar `Subscriptions` entity (adicionar relação com User)
13. Atualizar `SubscriptionsRepository` (queries filtradas por userId)
14. Atualizar `SubscriptionsService` e `SubscriptionsController`

### Fase 5: Testes

15. Criar `AuthControllerIT`
16. Adaptar `SubscriptionsControllerIT`

---

## Melhorias Futuras

- [ ] Limpeza agendada de refresh tokens expirados (`@Scheduled` rodando 1x/dia)
- [ ] Endpoint de logout (invalida refresh token específico)
- [ ] "Deslogar de todos os dispositivos" (deleta todos refresh tokens do usuário)
- [ ] Endpoints de admin para listar/gerenciar usuários
