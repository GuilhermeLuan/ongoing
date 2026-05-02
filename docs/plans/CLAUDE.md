# Contexto da Atividade Acadêmica — Teste de Software

## Objetivo

Atividade de Teste de Software que cobre três Histórias de Usuário (HU01-03) com testes unitários usando Mockito + JUnit
5. Sem `@SpringBootTest` nem Testcontainers nas classes unitárias.

## Histórias de Usuário

| ID   | Funcionalidade    | Serviço                |
|------|-------------------|------------------------|
| HU01 | Criar Assinatura  | `SubscriptionsService` |
| HU02 | Realizar Logon    | `AuthService`          |
| HU03 | Realizar Cadastro | `AuthService`          |

## Arquivos de Teste Criados

| Arquivo                         | Localização                                                      | Testes |
|---------------------------------|------------------------------------------------------------------|--------|
| `SubscriptionsServiceTest.java` | `backend/src/test/java/dev/guilhermeluan/ongoing/subscriptions/` | 16     |
| `AuthServiceTest.java`          | `backend/src/test/java/dev/guilhermeluan/ongoing/auth/`          | 6      |
| `UserServiceTest.java`          | `backend/src/test/java/dev/guilhermeluan/ongoing/user/`          | 2      |

**Total: 24 testes unitários.**

## Documentos da Atividade

- Planos de teste HU01-03: `docs/plano de testes/`
- Rascunho do Relatório Final: `docs/plans/relatorio-final-testes.md`
- Plano de implementação: `docs/plans/atividade.md`

## Convenção de Nomenclatura dos Testes

```
methodName_ShouldExpectedResult_WhenCondition
```

## Executar os Testes

```bash
cd backend
./mvnw test -Dtest="SubscriptionsServiceTest,AuthServiceTest,UserServiceTest"
```

## Notas Técnicas

- Campo `@Value("${security.jwt.refresh-expiration}")` em `AuthService` precisa de `ReflectionTestUtils.setField` no
  `@BeforeEach` do `AuthServiceTest`.
- `SubscriptionsService` recebe `UserRepository` via construtor — o mock deve ser declarado para que `@InjectMocks`
  funcione corretamente.
