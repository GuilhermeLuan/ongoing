# Dashboard Module (dashboard/)

Módulo responsável por agregar e calcular estatísticas consolidadas das assinaturas do usuário, fornecendo visão
financeira geral.

## Componentes Principais

### DashboardController

Endpoint REST para recuperar dashboard consolidado:

- `GET /api/v1/dashboard` - Retorna estatísticas agregadas do usuário autenticado

**Extração do userId**:

```java
Long userId = ((UserPrincipal) auth.getPrincipal()).id();
```

**Por que não precisa validar null aqui?**

O Spring Security **garante** que se o código do controller executar, o `Authentication` está válido:

1. `SpringSecurityConfig` (linha 37): `.anyRequest().authenticated()` - endpoint requer autenticação
2. `JwtAuthFilter`: Se JWT inválido/ausente, não seta autenticação no SecurityContext
3. Spring Security: Verifica autenticação ANTES de chamar controller - retorna 401 se não autenticado

**Responsabilidade do Controller**: Extrair dados da requisição HTTP (headers, params, body, **auth**)

**Responsabilidade do Service**: Lógica de negócio (cálculos, validações de domínio)

Extrair `userId` do `Authentication` é responsabilidade do controller porque:

- É um dado da infraestrutura HTTP/Security
- Service não deve conhecer `Authentication` (acoplamento com Spring Security)
- Service recebe apenas dados de domínio: `getDashboard(Long userId)`

### DashboardService

Lógica de negócio para cálculo de estatísticas consolidadas.

**Método principal**:

```java
public DashboardResponse getDashboard(Long userId)
```

**Fluxo**:

1. Busca assinaturas ativas do usuário via `SubscriptionsService`
2. Se vazio, retorna dashboard zerado (early return)
3. Converte todas assinaturas para BRL usando `ExchangeRateService`
4. Calcula 4 métricas principais:
    - `spendingByCategory` - Gasto mensal agrupado por categoria
    - `monthlyAverage` - Média mensal total
    - `thisMonthTotal` - Total com vencimento no mês atual
    - `yearlyTotal` - Projeção anual total
5. Retorna `DashboardResponse` com dados consolidados

**Dependências**:

- `ExchangeRateService` - Converte valores para BRL
- `SubscriptionsService` - Busca assinaturas ativas do usuário

#### Cálculos Detalhados

##### 1. Conversão para BRL (convertToBrl)

```java
private ConvertedSubscription convertToBrl(Subscriptions subscription) {
    BigDecimal priceInBrl = exchangeRateService.convertToBrl(
            subscription.getValue(),
            subscription.getCurrency()
    );
    return new ConvertedSubscription(subscription, priceInBrl);
}
```

Transforma `Subscriptions` em `ConvertedSubscription` - uma projeção que já contém o valor convertido para BRL.

##### 2. Gasto por Categoria (calculateSpendingByCategory)

```java
private List<CategorySpending> calculateSpendingByCategory(List<ConvertedSubscription> subscriptions) {
    return subscriptions.stream()
            .collect(Collectors.groupingBy(
                    s -> s.subscription().getCategory(),  // Agrupa por categoria
                    Collectors.reducing(
                            BigDecimal.ZERO,
                            ConvertedSubscription::monthlyPrice,  // Soma os preços mensais
                            BigDecimal::add
                    )
            ))
            .entrySet().stream()
            .map(e -> new CategorySpending(
                    e.getKey().getId(),
                    e.getKey().getName(),
                    e.getValue()
            ))
            .sorted(Comparator.comparing(CategorySpending::total).reversed())  // Maior primeiro
            .toList();
}
```

**Pattern**: Stream API com `groupingBy` + `reducing`

**Output**: Lista de categorias ordenadas por gasto (maior → menor)

##### 3. Média Mensal (calculateMonthlyAverage)

```java
private BigDecimal calculateMonthlyAverage(List<ConvertedSubscription> subscriptions) {
    return subscriptions.stream()
            .map(ConvertedSubscription::monthlyPrice)  // Converte cada billing cycle para mensal
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
}
```

**Importante**: Não é média aritmética! É a **soma** dos preços mensais normalizados.

Exemplo:

- Netflix (R$ 40/mês) + Spotify (R$ 20/mês) = R$ 60/mês
- Se uma assinatura é anual (R$ 120/ano), divide por 12 = R$ 10/mês
- Total: R$ 70/mês

##### 4. Total do Mês Atual (calculateThisMonthTotal)

```java
private BigDecimal calculateThisMonthTotal(List<ConvertedSubscription> subscriptions) {
    YearMonth currentMonth = YearMonth.now();

    return subscriptions.stream()
            .filter(s -> s.isDueIn(currentMonth))  // Apenas assinaturas com vencimento este mês
            .map(ConvertedSubscription::priceInBrl)  // Valor original (não normalizado)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
}
```

**Diferença crucial**: Usa `priceInBrl` (valor original), não `monthlyPrice` (normalizado).

Exemplo:

- Se Netflix (R$ 40/mês) vence este mês → soma R$ 40
- Se plano anual (R$ 120/ano) vence este mês → soma R$ 120 (não R$ 10!)
- Se plano anual vence mês que vem → não conta

##### 5. Total Anual (calculateYearlyTotal)

```java
private BigDecimal calculateYearlyTotal(List<ConvertedSubscription> subscriptions) {
    return subscriptions.stream()
            .map(ConvertedSubscription::yearlyPrice)  // Converte cada billing cycle para anual
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
}
```

Projeta quanto o usuário gastará em 12 meses.

Exemplo:

- Netflix (R$ 40/mês) × 12 = R$ 480/ano
- Plano anual (R$ 120/ano) = R$ 120/ano
- Total: R$ 600/ano

### ConvertedSubscription

**Record de projeção** que combina uma `Subscription` com seu valor já convertido para BRL.

**Por que existe?**

Separar responsabilidades:

1. `ExchangeRateService` converte moeda (uma vez)
2. `ConvertedSubscription` faz cálculos de billing cycles (múltiplas vezes)
3. Evita conversões repetidas - performance e consistência

**Métodos principais**:

#### monthlyPrice()

Normaliza qualquer billing cycle para equivalente mensal:

```java
public BigDecimal monthlyPrice() {
    return switch (normalizedBillingCycleName(subscription.getBillingCycle())) {
        case "monthly" -> scaled(priceInBrl);
        case "quarterly" -> scaled(priceInBrl.divide(BigDecimal.valueOf(3), 8, RoundingMode.HALF_UP));
        case "semi-annual" -> scaled(priceInBrl.divide(BigDecimal.valueOf(6), 8, RoundingMode.HALF_UP));
        case "annual" -> scaled(priceInBrl.divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP));
        case "weekly" ->
                scaled(priceInBrl.multiply(BigDecimal.valueOf(52)).divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP));
        case "bi-weekly" ->
                scaled(priceInBrl.multiply(BigDecimal.valueOf(26)).divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP));
        default -> throw new IllegalStateException("Unsupported billing cycle: " + subscription.getBillingCycle());
    };
}
```

**Pattern**: Switch expression com normalização de string (lowercase, trim)

**Precisão**: Divide com 8 casas decimais intermediárias, retorna 2 casas finais (scaled)

**Cálculos semanais**:

- Weekly: 52 semanas/ano ÷ 12 meses ≈ 4.33 semanas/mês
- Bi-weekly: 26 períodos/ano ÷ 12 meses ≈ 2.17 períodos/mês

#### yearlyPrice()

Projeta qualquer billing cycle para equivalente anual:

```java
public BigDecimal yearlyPrice() {
    return switch (normalizedBillingCycleName(subscription.getBillingCycle())) {
        case "monthly" -> scaled(priceInBrl.multiply(BigDecimal.valueOf(12)));
        case "quarterly" -> scaled(priceInBrl.multiply(BigDecimal.valueOf(4)));
        case "semi-annual" -> scaled(priceInBrl.multiply(BigDecimal.valueOf(2)));
        case "annual" -> scaled(priceInBrl);
        case "weekly" -> scaled(priceInBrl.multiply(BigDecimal.valueOf(52)));
        case "bi-weekly" -> scaled(priceInBrl.multiply(BigDecimal.valueOf(26)));
        default -> throw new IllegalStateException("Unsupported billing cycle: " + subscription.getBillingCycle());
    };
}
```

#### isDueIn(YearMonth)

Verifica se a assinatura tem vencimento em um mês específico:

```java
public boolean isDueIn(YearMonth month) {
    if (subscription.getNextPaymentDate() == null) {
        return false;
    }
    return YearMonth.from(subscription.getNextPaymentDate()).equals(month);
}
```

Usado para calcular `thisMonthTotal` - apenas assinaturas que **efetivamente** cobrarão no mês atual.

### DTOs

Todos são Java records em `dto/`:

#### DashboardResponse

Response consolidado retornado ao cliente:

```java
public record DashboardResponse(
        List<CategorySpending> spendingByCategory,  // Gasto por categoria (ordenado)
        BigDecimal monthlyAverage,                   // Soma dos preços mensais normalizados
        BigDecimal thisMonthTotal,                   // Total com vencimento este mês
        BigDecimal yearlyTotal,                      // Projeção anual
        Currency currency,                           // Sempre BRL
        LocalDate exchangeRateDate                   // Data da cotação usada
)
```

**Validações**: Bean Validation com `@NotNull`, `@PositiveOrZero`

**Importante**: Todos os valores monetários são **sempre em BRL**.

#### CategorySpending

Gasto total agrupado por categoria:

```java
public record CategorySpending(
        Long categoryId,
        String categoryName,
        BigDecimal total  // Soma dos preços mensais normalizados desta categoria
)
```

## Fluxo de Dados Completo

```
Cliente autenticado
    ↓
GET /api/v1/dashboard
    ↓
DashboardController
  - Extrai userId do Authentication (Spring Security garante válido)
    ↓
DashboardService.getDashboard(userId)
  ↓
SubscriptionsService.findActiveByUserId(userId)
  - Retorna List<Subscriptions>
    ↓
Se vazio → createEmptyDashboard() → Response zerado
    ↓
Se não vazio → Converte todas para BRL
  ↓
ExchangeRateService.convertToBrl(valor, moeda)
  - Retorna BigDecimal (valor em BRL)
    ↓
Cria List<ConvertedSubscription>
    ↓
Calcula 4 métricas em paralelo:
  - spendingByCategory (agrupa por categoria)
  - monthlyAverage (soma preços mensais)
  - thisMonthTotal (soma apenas vencimentos deste mês)
  - yearlyTotal (projeta 12 meses)
    ↓
Retorna DashboardResponse
    ↓
ResponseEntity.ok(response)
    ↓
Cliente recebe JSON
```

## Decisões de Design

### Por que ConvertedSubscription é um record separado?

**Separação de responsabilidades**:

- `Subscription` (entidade JPA) → domínio de persistência
- `ConvertedSubscription` (record) → domínio de cálculo

**Performance**:

- Converte moeda UMA vez por assinatura
- Usa o valor convertido em MÚLTIPLOS cálculos (monthly, yearly, category)

**Alternativa rejeitada**: Converter dentro de cada cálculo → múltiplas chamadas ao ExchangeRateService

### Por que normalizar billing cycle com lowercase?

**Robustez**: Banco pode ter "Monthly", "monthly", " Monthly " - todos funcionam

**Pattern matching**: Switch expression exige case exato - normalização garante match

### Por que 8 casas decimais intermediárias?

**Precisão em divisões**:

- Weekly → mensal: divide por 4.333... (dízima)
- Usar apenas 2 casas geraria erro de arredondamento cumulativo

**Exemplo**:

- R$ 10,00/semana
- Com 2 casas: (10 × 52) / 12 = 520 / 12 = 43.33 → R$ 43,33/mês
- Com 8 casas: (10 × 52) / 12 = 43.33333333 → R$ 43,33/mês (arredondado no final)

### Por que thisMonthTotal usa priceInBrl, não monthlyPrice?

**Semântica diferente**:

- `monthlyAverage`: "Quanto gasto **em média** por mês?" → normaliza tudo para mensal
- `thisMonthTotal`: "Quanto vou **efetivamente pagar** este mês?" → usa valor real da cobrança

**Exemplo**:

- Plano anual de R$ 120 vence em março
- `monthlyAverage`: conta R$ 10/mês (120 ÷ 12)
- `thisMonthTotal` em março: conta R$ 120 (cobrança real)
- `thisMonthTotal` em abril: conta R$ 0 (não vence)

## Integração com Outros Módulos

### SubscriptionsService (subscriptions/)

Fornece assinaturas ativas do usuário:

```java
List<Subscriptions> findActiveByUserId(Long userId)
```

**Critério de "ativa"**: `isActive = true` (definido no SubscriptionsService)

### ExchangeRateService (exchange/)

Converte valores para BRL:

```java
BigDecimal convertToBrl(BigDecimal amount, Currency currency)
```

**Importante**: Se moeda origem já é BRL, retorna valor original (sem conversão)

### Spring Security (auth/jwt/)

Protege endpoint e fornece `Authentication` válido:

- `JwtAuthFilter` valida JWT e seta `UserPrincipal` no SecurityContext
- Controller extrai `userId` do `UserPrincipal`

## Testing

### DashboardServiceTest

Testes unitários com mocks:

- Testa cálculos com diferentes billing cycles
- Testa conversão de moedas
- Testa dashboard vazio
- Testa agrupamento por categoria
- Testa filtro de vencimentos do mês

### DashboardControllerIT

Testes de integração end-to-end:

- Usa Testcontainers com PostgreSQL real
- Testa autenticação JWT
- Testa response completo do endpoint
- Valida estrutura JSON retornada

## Edge Cases Tratados

1. **Usuário sem assinaturas**: Retorna dashboard zerado (não lança exceção)
2. **Subscription sem nextPaymentDate**: `isDueIn()` retorna `false` (não quebra)
3. **Billing cycle não suportado**: Lança `IllegalStateException` (fail fast)
4. **Billing cycle null/vazio**: Lança `IllegalStateException` com mensagem clara
5. **Moeda já em BRL**: ExchangeRateService retorna valor original (otimização)

## Possíveis Melhorias Futuras

1. **Cache**: Cachear dashboard por alguns minutos (dados não mudam frequentemente)
2. **Moeda customizável**: Permitir usuário escolher moeda de exibição (não apenas BRL)
3. **Filtros**: Adicionar query params para filtrar por categoria, período, etc
4. **Métricas adicionais**:
    - Próximas cobranças (próximos 7/30 dias)
    - Comparação mês atual vs mês anterior
    - Tendência de gastos (crescimento/redução)
5. **Paginação**: Para `spendingByCategory` se usuário tiver muitas categorias

## Lições Aprendidas

### 1. Records para Projeções

`ConvertedSubscription` é um exemplo perfeito de **record de projeção**:

- Combina dados de múltiplas fontes (Subscription + valor convertido)
- Imutável por natureza (não precisa de Lombok)
- Métodos helper (monthlyPrice, yearlyPrice) encapsulam lógica de cálculo

### 2. Stream API para Agregações

`calculateSpendingByCategory` mostra poder do Collectors.groupingBy:

- Substitui loops imperativos complexos
- Código declarativo (diz "o que" fazer, não "como")
- Mais legível quando você entende os collectors

### 3. Early Returns para Casos Vazios

```java
if(subscriptions.isEmpty()){
        return

createEmptyDashboard();
}
```

**Benefícios**:

- Evita processamento desnecessário
- Código principal fica no "happy path" (sem aninhamento)
- Mais fácil de ler e testar

### 4. Precisão em Cálculos Financeiros

Sempre use `BigDecimal` para dinheiro:

- `double`/`float` têm erros de arredondamento
- Especifique `RoundingMode` explicitamente
- Use casas decimais intermediárias em divisões complexas

### 5. Separação Controller/Service

Controller extrai dados da request → Service faz lógica de negócio

**Não faça**: Service receber `Authentication` (acoplamento com Spring Security)

**Faça**: Controller extrair `userId` e passar para service
