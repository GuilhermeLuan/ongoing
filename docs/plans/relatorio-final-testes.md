# Relatório Final de Testes — Ongoing

**Disciplina:** Teste de Software
**Data:** 2026-05-02
**Repositório:** https://github.com/GuilhermeLuan/ongoing

---

## 1. Introdução ao Sistema Desenvolvido

O **Ongoing** é uma plataforma de gerenciamento de assinaturas recorrentes. O usuário cadastra serviços como Netflix,
Spotify, etc., e o sistema acompanha datas de renovação, valores e categorias, além de enviar notificações por e-mail
antes do vencimento.

**Stack principal:**

- Backend: Spring Boot 4, Java 25, PostgreSQL, Flyway, JWT
- Frontend: Next.js 14, TypeScript, Tailwind CSS
- Testes: JUnit 5, Mockito (unitários), Testcontainers + RestAssured (integração)

---

## 2. Planejamento de Testes

O plano cobre três Histórias de Usuário definidas nos documentos em `docs/plano de testes/`:

| ID      | História de Usuário         | Tipo de Teste | Classe de Teste            |
|---------|-----------------------------|---------------|----------------------------|
| HU01    | Criar Assinatura            | Unitário      | `SubscriptionsServiceTest` |
| HU02    | Realizar Logon              | Unitário      | `AuthServiceTest`          |
| HU03    | Realizar Cadastro           | Unitário      | `AuthServiceTest`          |
| HU02/03 | Busca de usuário por e-mail | Unitário      | `UserServiceTest`          |

**Critérios de cobertura:** Caminho feliz + cenários de erro para cada operação de serviço.

**Restrições:** Apenas Mockito + JUnit 5. Sem `@SpringBootTest` nem Testcontainers nas classes unitárias.

---

## 3. Descrição dos Casos de Teste

### 3.1 HU01 — Criar Assinatura (`SubscriptionsServiceTest`)

| Caso de Teste | Método                                  | Cenário                      | Resultado Esperado                                     |
|---------------|-----------------------------------------|------------------------------|--------------------------------------------------------|
| CT-01         | `calculateNextBillingDate`              | Ciclo MONTHLY                | Retorna startDate + 1 mês                              |
| CT-02         | `calculateNextBillingDate`              | Ciclo YEARLY                 | Retorna startDate + 1 ano                              |
| CT-03         | `calculateNextBillingDate`              | Ciclo nulo                   | Lança `BadRequestException`                            |
| CT-04         | `calculateNextBillingDate`              | Ciclo QUARTERLY              | Retorna startDate + 3 meses                            |
| CT-05         | `calculateNextBillingDate`              | Ciclo SEMI_ANNUAL            | Retorna startDate + 6 meses                            |
| CT-06         | `calculateNextBillingDate`              | Ciclo WEEKLY                 | Retorna startDate + 7 dias                             |
| CT-07         | `calculateNextBillingDate`              | Ciclo BIWEEKLY               | Retorna startDate + 14 dias                            |
| CT-08         | `save`                                  | Assinatura válida com userId | Seta `user` e `nextPaymentDate`; chama repository.save |
| CT-09         | `findAll`                               | Filtro `name` presente       | Delega para `findWithFilters`                          |
| CT-10         | `findAll`                               | Sem filtros                  | Delega para `findAllByUserId`                          |
| CT-11         | `findByIdOrThrowNotFoundException`      | ID existente                 | Retorna assinatura                                     |
| CT-12         | `findByIdOrThrowNotFoundException`      | ID inexistente               | Lança `NotFoundException`                              |
| CT-13         | `deleteById`                            | ID existente                 | Chama `repository.deleteById`                          |
| CT-14         | `deleteById`                            | ID inexistente               | Lança `NotFoundException` sem deletar                  |
| CT-15         | `update`                                | Assinatura existente         | Recalcula `nextPaymentDate` e chama save               |
| CT-16         | `findRenewalSubscriptionsGroupedByUser` | 2 usuários, 3 assinaturas    | Retorna mapa com 2 entradas, agrupadas corretamente    |

### 3.2 HU02 — Realizar Logon (`AuthServiceTest`)

| Caso de Teste | Método  | Cenário               | Resultado Esperado                                    |
|---------------|---------|-----------------------|-------------------------------------------------------|
| CT-17         | `login` | Credenciais válidas   | Retorna `AuthResponse` com accessToken e refreshToken |
| CT-18         | `login` | E-mail não encontrado | Lança `InvalidCredentialException`                    |
| CT-19         | `login` | Senha incorreta       | Lança `InvalidCredentialException`                    |

### 3.3 HU03 — Realizar Cadastro (`AuthServiceTest`)

| Caso de Teste | Método     | Cenário             | Resultado Esperado                                     |
|---------------|------------|---------------------|--------------------------------------------------------|
| CT-20         | `register` | E-mail novo         | Retorna `AuthResponse` com tokens gerados              |
| CT-21         | `register` | E-mail já existente | Lança `BadRequestException("Email already exists")`    |
| CT-22         | `register` | Verificação de hash | `passwordEncoder.encode` chamado; senha salva hasheada |

### 3.4 HU02/HU03 — Busca de Usuário por E-mail (`UserServiceTest`)

| Caso de Teste | Método        | Cenário            | Resultado Esperado        |
|---------------|---------------|--------------------|---------------------------|
| CT-23         | `findByEmail` | E-mail existente   | Retorna `User` correto    |
| CT-24         | `findByEmail` | E-mail inexistente | Lança `NotFoundException` |

---

## 4. Resultados das Execuções e Evidências

> **A PREENCHER** após execução formal dos testes.

**Comando de execução:**

```bash
cd backend
./mvnw test -Dtest="SubscriptionsServiceTest,AuthServiceTest,UserServiceTest"
```

**Resultado esperado da saída Maven:**

```
Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**Evidências a anexar:**

- Captura de tela do terminal com `BUILD SUCCESS`
- Relatório Surefire em `backend/target/surefire-reports/`

---

## 5. Conclusão e Lições Aprendidas

> **A PREENCHER** após análise dos resultados.

**Pontos a abordar:**

- Cobertura atingida vs. planejada
- Defeitos encontrados durante a criação dos testes (descobertas de design)
- Dificuldades encontradas (ex: campos `@Value` que precisam de `ReflectionTestUtils`)
- Valor dos testes unitários isolados (sem banco de dados) para velocidade de feedback
- Diferença entre testes unitários e de integração neste projeto

---

## 6. Link para o Repositório no GitHub

https://github.com/GuilhermeLuan/ongoing
