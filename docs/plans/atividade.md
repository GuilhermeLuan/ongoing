# Plano: Testes Unitários + Relatório Final — Atividade Acadêmica

## Contexto

Atividade acadêmica de Teste de Software. Os documentos em `docs/plano de testes/` definem três Histórias de Usuário (
HU01-03) e seus planos de teste. O objetivo desta atividade é:

1. Criar testes unitários cobrindo as funcionalidades das HUs
2. Gerar um rascunho do Relatório Final (Tópico 4.5 do roteiro) em MD
3. Criar um `CLAUDE.md` em `docs/plans/` com contexto da atividade

Testes unitários **não** devem usar `@SpringBootTest` nem Testcontainers — apenas Mockito + JUnit 5, seguindo o padrão
existente em `SubscriptionsServiceTest`.

---

## Mapeamento HU → Serviço → Testes

| HU      | Funcionalidade                                              | Serviço/Classe         | Arquivo de Teste                          |
|---------|-------------------------------------------------------------|------------------------|-------------------------------------------|
| HU01    | Criar Assinatura (cálculo de data, validação, persistência) | `SubscriptionsService` | `SubscriptionsServiceTest.java` (ampliar) |
| HU02    | Realizar Logon                                              | `AuthService`          | `AuthServiceTest.java` (novo)             |
| HU03    | Realizar Cadastro                                           | `AuthService`          | `AuthServiceTest.java` (novo)             |
| HU02/03 | Busca de usuário por e-mail                                 | `UserService`          | `UserServiceTest.java` (novo)             |

---

## Tarefa 1 — Ampliar `SubscriptionsServiceTest` (HU01)

**Arquivo:** `backend/src/test/java/dev/guilhermeluan/ongoing/subscriptions/SubscriptionsServiceTest.java`

Já existe com 7 testes de `calculateNextBillingDate`. Adicionar `@Mock UserRepository userRepository` e os novos testes:

| Teste                                                                            | Cenário                                                                                                     |
|----------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| `save_ShouldSetUserAndCalculateNextPaymentDate_WhenValidSubscription`            | `getReferenceById` retorna user mock; verifica que `user` e `nextPaymentDate` foram definidos antes do save |
| `findAll_ShouldDelegateToFindWithFilters_WhenAnyFilterPresent`                   | `name != null` → chama `findWithFilters`, não `findAllByUserId`                                             |
| `findAll_ShouldDelegateToFindAllByUserId_WhenNoFilters`                          | todos null → chama `findAllByUserId`                                                                        |
| `findByIdOrThrowNotFoundException_ShouldReturnSubscription_WhenFound`            | `Optional.of(sub)` → retorna sub                                                                            |
| `findByIdOrThrowNotFoundException_ShouldThrow_WhenNotFound`                      | `Optional.empty()` → lança `NotFoundException`                                                              |
| `deleteById_ShouldDeleteSubscription_WhenExists`                                 | chama `deleteById` no repository                                                                            |
| `deleteById_ShouldThrow_WhenNotFound`                                            | lança `NotFoundException` antes de deletar                                                                  |
| `update_ShouldRecalculateNextPaymentDate_WhenCalled`                             | verifica que `nextPaymentDate` é recalculada e `save` chamado                                               |
| `findRenewalSubscriptionsGroupedByUser_ShouldGroupByUser_WhenSubscriptionsExist` | retorna mapa `User → List<Subscriptions>`                                                                   |

---

## Tarefa 2 — Criar `AuthServiceTest` (HU02 + HU03)

**Arquivo (novo):** `backend/src/test/java/dev/guilhermeluan/ongoing/auth/AuthServiceTest.java`

Setup:

```java

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @InjectMocks
    AuthService authService;
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtService jwtService;
    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @Mock
    RefreshTokenService refreshTokenService;
    // refreshTokenExpiration via ReflectionTestUtils.setField
}
```

**HU03 — Realizar Cadastro:**

| Teste                                                            | Cenário                                                                      |
|------------------------------------------------------------------|------------------------------------------------------------------------------|
| `register_ShouldReturnAuthResponse_WhenEmailIsNew`               | `existsByEmail` → false; tokens gerados; retorna `AuthResponse`              |
| `register_ShouldThrowBadRequestException_WhenEmailAlreadyExists` | `existsByEmail` → true → lança `BadRequestException("Email already exists")` |
| `register_ShouldHashPassword_BeforeSaving`                       | verifica que `passwordEncoder.encode(request.password())` é chamado          |

**HU02 — Realizar Logon:**

| Teste                                                                  | Cenário                                                                 |
|------------------------------------------------------------------------|-------------------------------------------------------------------------|
| `login_ShouldReturnAuthResponse_WhenCredentialsAreValid`               | user encontrado; `matches` → true; retorna `AuthResponse`               |
| `login_ShouldThrowInvalidCredentialException_WhenEmailNotFound`        | `findByEmail` → empty → lança `InvalidCredentialException`              |
| `login_ShouldThrowInvalidCredentialException_WhenPasswordDoesNotMatch` | user encontrado; `matches` → false → lança `InvalidCredentialException` |

---

## Tarefa 3 — Criar `UserServiceTest` (HU02/HU03)

**Arquivo (novo):** `backend/src/test/java/dev/guilhermeluan/ongoing/user/UserServiceTest.java`

| Teste                                                        | Cenário                                        |
|--------------------------------------------------------------|------------------------------------------------|
| `findByEmail_ShouldReturnUser_WhenEmailExists`               | `Optional.of(user)` → retorna user             |
| `findByEmail_ShouldThrowNotFoundException_WhenEmailNotFound` | `Optional.empty()` → lança `NotFoundException` |

---

## Tarefa 4 — Rascunho do Relatório Final

**Arquivo:** `docs/plans/relatorio-final-testes.md`

Estrutura baseada no Tópico 4.5 do roteiro (seções de execução serão preenchidas após rodar os testes):

1. Introdução ao Sistema Desenvolvido
2. Planejamento de Testes
3. Descrição dos Casos de Teste (HU01, HU02, HU03)
4. Resultados das Execuções e Evidências *(A PREENCHER)*
5. Conclusão e Lições Aprendidas *(A PREENCHER)*
6. Link para o Repositório no GitHub

---

## Tarefa 5 — Criar `docs/plans/CLAUDE.md`

Arquivo de contexto para o Claude com informações sobre a atividade acadêmica, arquivos de teste criados e localização
do relatório final.

---

## Arquivos Críticos

| Papel                  | Caminho                                                                                       |
|------------------------|-----------------------------------------------------------------------------------------------|
| Serviço HU01           | `backend/src/main/java/dev/guilhermeluan/ongoing/subscriptions/SubscriptionsService.java`     |
| Serviço HU02/03        | `backend/src/main/java/dev/guilhermeluan/ongoing/auth/AuthService.java`                       |
| Serviço usuário        | `backend/src/main/java/dev/guilhermeluan/ongoing/user/UserService.java`                       |
| Teste padrão existente | `backend/src/test/java/dev/guilhermeluan/ongoing/subscriptions/SubscriptionsServiceTest.java` |
| Exceções               | `backend/src/main/java/dev/guilhermeluan/ongoing/exception/`                                  |

---

## Convenção de Nomenclatura

```
methodName_ShouldExpectedResult_WhenCondition
```

---

## Verificação

```bash
cd backend
./mvnw test -Dtest="SubscriptionsServiceTest,AuthServiceTest,UserServiceTest"
```

Todos os testes devem passar sem `@SpringBootTest`.
