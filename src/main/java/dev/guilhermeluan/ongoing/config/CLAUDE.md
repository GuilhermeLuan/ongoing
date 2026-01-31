# Config Module

Módulo de configuração do Spring Security e outras configurações globais.

## Componentes Principais

### SpringSecurityConfig

Classe de configuração principal do Spring Security.

**Configurações**:

#### SecurityFilterChain

```java

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
```

**CSRF**: Desabilitado

- Aplicação é stateless (JWT)
- CSRF protection não necessário para APIs REST stateless

**Session Management**: STATELESS

- Sem sessão server-side
- Cada request é independente
- Autenticação via JWT em cada request

**Authorization Rules**:

```java
.authorizeHttpRequests(authorize ->authorize
        .

requestMatchers("/api/v1/auth/**","/api/v1/status").

permitAll()
    .

anyRequest().

authenticated()
)
```

Endpoints públicos (sem autenticação):

- `/api/v1/auth/**` - Login, registro, refresh
- `/api/v1/status` - Health check

Todos outros endpoints requerem autenticação JWT.

**Filtros**:

```java
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter .class)
```

`JwtAuthFilter` é executado ANTES do filtro padrão de autenticação:

1. Intercepta request
2. Extrai e valida JWT
3. Seta autenticação no SecurityContext
4. Passa para próximo filtro

#### PasswordEncoder

```java

@Bean
public PasswordEncoder passwordEncoder()
```

Retorna `BCryptPasswordEncoder`:

- Algoritmo de hashing de senha industry-standard
- Inclui sal aleatório automaticamente
- Custo padrão: 10 rounds (2^10 = 1024 iterações)
- Usado pelo AuthService para hash de senhas

## Fluxo de Request

### Request Público (sem autenticação)

```
Client → SpringSecurity → JwtAuthFilter (skip) → Controller → Response
```

### Request Protegido (com JWT)

```
Client (JWT no header)
  → SpringSecurity
  → JwtAuthFilter
    - Extrai JWT
    - Valida assinatura e expiração
    - Carrega User via UserService
    - Cria UserPrincipal
    - Seta no SecurityContext
  → Controller (acessa via Authentication parameter)
  → Response
```

### Request Protegido (sem JWT ou JWT inválido)

```
Client
  → SpringSecurity
  → JwtAuthFilter (não seta autenticação)
  → SpringSecurity (verifica autenticação)
  → 401 UNAUTHORIZED
```

## Integração com Outros Módulos

### JwtAuthFilter (auth/jwt/)

Filtro customizado injetado via constructor injection:

```java
private final JwtAuthFilter jwtAuthFilter;

public SpringSecurityConfig(JwtAuthFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
}
```

### PasswordEncoder

Usado por:

- `AuthService` - Hashear senha no registro
- Spring Security - Validar senha no login (automaticamente)

## Configuração de CORS (futuro)

Atualmente não configurado. Para adicionar CORS:

```java
.cors(cors ->cors.

configurationSource(corsConfigurationSource()))
```

## Testing

Em testes de integração:

- Security está ativo por padrão
- Necessário autenticar requests protegidos
- Testes em `AuthControllerIT` demonstram fluxo de autenticação
- Testes em `SubscriptionsControllerIT` usam JWT válido

## Best Practices Implementadas

1. **Stateless Authentication**: Escalável e simples
2. **JWT em Header**: Authorization: Bearer <token>
3. **BCrypt para Senhas**: Resistente a rainbow tables e brute force
4. **Filtro Before UsernamePasswordAuthenticationFilter**: Padrão recomendado
5. **Disable CSRF para APIs REST**: Correto para stateless APIs

## Segurança Adicional (futuro)

Possíveis melhorias:

- CORS configuration para frontend específico
- Rate limiting para prevenir brute force
- HTTPS obrigatório em produção
- Refresh token rotation (já implementado)
- Logout (invalidar refresh tokens)
