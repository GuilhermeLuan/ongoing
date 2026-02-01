# User Module

Módulo de gerenciamento de usuários e refresh tokens.

## Componentes Principais

### User Entity

Entidade JPA que representa usuários do sistema:

```java
@Entity
@Table(name = "tb_users")
public class User implements UserDetails {
    - id (Long, auto-increment)
    - name (String)
    - email (String, unique)
    - passwordHash (String) - senha criptografada com BCrypt
    - role (Role enum) - ADMIN ou USER
    - createdAt, updatedAt (timestamps)
}
```

**Spring Security Integration**:
Implementa `UserDetails` para integração com Spring Security:

- `getUsername()` → retorna email
- `getPassword()` → retorna passwordHash
- `getAuthorities()` → retorna role como `ROLE_ADMIN` ou `ROLE_USER`

**Lifecycle Hooks**:

- `@PrePersist` - Define createdAt e updatedAt
- `@PreUpdate` - Atualiza updatedAt

### Role Enum

Papéis de usuário:

- **USER** - Usuário padrão (acesso às próprias assinaturas)
- **ADMIN** - Administrador (acesso total - futuro)

Armazenado como STRING no banco.

### RefreshToken Entity

Tokens de refresh para renovação de JWTs:

```java
@Entity
@Table(name = "tb_refresh_tokens")
public class RefreshToken {
    - id (Long, auto-increment)
    - token (String, unique) - UUID gerado
    - user (User) - ManyToOne relationship
    - expiresAt (LocalDateTime)
    - createdAt (timestamp)
}
```

**One-time use**: Tokens são deletados após uso (token rotation pattern).

### UserRepository

Interface Spring Data JPA:

- **findByEmail()** - Busca usuário por email (usado no login)
- **existsByEmail()** - Verifica se email já existe (usado no registro)

### RefreshTokenRepository

Interface Spring Data JPA:

- **findByToken()** - Busca refresh token
- Repository padrão para operações CRUD

### UserService

Serviço para operações relacionadas a usuários:

- **loadUserByUsername()** - Implementa UserDetailsService do Spring Security
- Usado pelo JwtAuthFilter para carregar dados do usuário

**Importante**: Lança `UsernameNotFoundException` se usuário não encontrado.

## Relacionamentos

### User ← RefreshToken (One-to-Many)

- Um usuário pode ter múltiplos refresh tokens
- Tokens antigos não são deletados automaticamente (apenas os expirados quando usados)
- Configurado com `@ManyToOne` no RefreshToken

### User ← Subscriptions (One-to-Many)

- Um usuário tem múltiplas assinaturas
- Relacionamento via `userId` na entidade Subscriptions
- Não mapeado explicitamente no User (unidirecional)

## Segurança

### Password Hashing

- Senhas nunca armazenadas em texto plano
- BCrypt usado para hashing (via `PasswordEncoder`)
- Sal aleatório incluído automaticamente
- Custo padrão do BCrypt: 10 rounds

### Token Security

- Refresh tokens são UUIDs v4 (impossível de adivinhar)
- Tokens expirados são deletados
- Token rotation: cada refresh gera novo par de tokens

## Campos Automáticos

Timestamps gerenciados automaticamente:

- `createdAt` - Setado no `@PrePersist`
- `updatedAt` - Atualizado no `@PreUpdate`

## Database Schema

Tabelas gerenciadas por Flyway migrations:

**tb_users**:

```sql
- id BIGSERIAL PRIMARY KEY
- name VARCHAR NOT NULL
- email VARCHAR UNIQUE NOT NULL
- password_hash VARCHAR NOT NULL
- role VARCHAR(50) NOT NULL
- created_at TIMESTAMP
- updated_at TIMESTAMP
```

**tb_refresh_tokens**:

```sql
- id BIGSERIAL PRIMARY KEY
- token VARCHAR UNIQUE NOT NULL
- user_id BIGINT REFERENCES tb_users(id)
- expires_at TIMESTAMP NOT NULL
- created_at TIMESTAMP
```

## Uso Típico

1. **Registro**: AuthService cria User com senha hasheada
2. **Login**: UserService carrega User por email, Spring Security valida senha
3. **Token Refresh**: RefreshTokenService busca token, valida, deleta, gera novos
4. **Acesso Protegido**: JwtAuthFilter carrega User e seta no SecurityContext

## Testing

Testes cobrem:

- Criação de usuários com senha hasheada
- Validação de unicidade de email
- Geração e validação de refresh tokens
- Integração com Spring Security
