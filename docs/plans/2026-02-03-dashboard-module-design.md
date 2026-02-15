# Dashboard Module - Design Document

**Date**: 2026-02-03
**Status**: Planning
**Milestone**: [Dashboard](https://github.com/GuilhermeLuan/ongoing/milestone/4)

## Overview

Implementação do módulo de dashboard com estatísticas de assinaturas do usuário autenticado. O dashboard fornece visão
consolidada de gastos por categoria, médias mensais, total do mês atual e projeção anual, com conversão automática de
moedas para BRL.

## Goals

- Fornecer endpoint REST para estatísticas do dashboard
- Calcular métricas de gastos com assinaturas ativas
- Integrar conversão de moedas (USD/EUR → BRL)
- Implementar cache de taxas de câmbio com Redis
- Garantir isolamento de dados por usuário (multi-tenancy)
- Manter cobertura de testes (unitários + integração)

## Architecture

### Modules

```
backend/src/main/java/dev/guilhermeluan/ongoing/
├── dashboard/              # Novo módulo
│   ├── DashboardController.java
│   ├── DashboardService.java
│   ├── dto/
│   │   ├── CategorySpending.java
│   │   └── DashboardResponse.java
│   └── CLAUDE.md
│
├── exchange/               # Novo módulo
│   ├── ExchangeRateService.java
│   ├── ExchangeRateClient.java
│   ├── dto/
│   │   └── ExchangeRateResponse.java
│   └── CLAUDE.md
│
└── config/
    └── RedisConfig.java    # Nova configuração
```

### Data Flow

```
Client Request
    ↓
DashboardController (JWT auth)
    ↓
DashboardService
    ├─→ SubscriptionsRepository.findActiveByUserId()
    └─→ ExchangeRateService.getRate() → Redis Cache → External API
    ↓
Calculate Metrics
    ├─→ spendingByCategory
    ├─→ monthlyAverage
    ├─→ thisMonthTotal
    └─→ yearlyTotal
    ↓
DashboardResponse (JSON)
```

## Implementation Plan

### Phase 1: Infrastructure Setup

**Issue**: [#52 - feat(infra): configurar Redis para cache](https://github.com/GuilhermeLuan/ongoing/issues/52)

**Objetivo**: Adicionar Redis como banco de dados de cache para taxas de câmbio.

#### Tasks

- [x] Adicionar dependência `spring-boot-starter-data-redis` no pom.xml
- [ ] Adicionar serviço Redis no docker-compose.yml (redis:7-alpine)
- [ ] Criar `RedisConfig.java` com `@EnableCaching`
- [ ] Configurar `RedisCacheManager` com TTL padrão de 24h
- [ ] Adicionar configurações no application.yaml

#### Technical Details

**pom.xml**

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

**docker-compose.yml**

```yaml
redis:
  image: redis:7-alpine
  container_name: ongoing-redis
  ports:
    - "6379:6379"
  networks:
    - ongoing-network
  restart: unless-stopped
```

**config/RedisConfig.java**

```java

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(24))
                .serializeValuesWith(
                        SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
```

**application.yaml**

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  cache:
    type: redis
    redis:
      time-to-live: 86400000  # 24 hours in milliseconds
```

---

### Phase 2: Exchange Rate Module

**Issue
**: [#53 - feat(exchange): criar módulo de conversão de moedas](https://github.com/GuilhermeLuan/ongoing/issues/53)

**Objetivo**: Criar módulo para conversão de moedas usando API externa com cache Redis.

#### Tasks

- [ ] Criar pacote `exchange/`
- [ ] Criar `ExchangeRateClient.java` - cliente HTTP para API externa
- [ ] Criar `ExchangeRateService.java` com `@Cacheable("exchange-rates")`
- [ ] Criar `ExchangeRateResponse.java` (DTO)
- [ ] Configurar URL da API no application.yaml
- [ ] Criar testes unitários `ExchangeRateServiceTest.java`

#### Technical Details

**exchange/ExchangeRateClient.java**

```java

@Component
public class ExchangeRateClient {
    private final RestTemplate restTemplate;

    @Value("${exchange.api.url}")
    private String apiUrl;

    public ExchangeRateResponse fetchRates() {
        return restTemplate.getForObject(apiUrl, ExchangeRateResponse.class);
    }
}
```

**exchange/ExchangeRateService.java**

```java

@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    private final ExchangeRateClient client;

    @Cacheable(value = "exchange-rates", key = "'BRL'")
    public Map<Currency, BigDecimal> getRates() {
        ExchangeRateResponse response = client.fetchRates();
        // Parse e retorna taxas para BRL
        return Map.of(
                Currency.USD, response.rates().get("BRL"),
                Currency.EUR, calculateEurToBrl(response)
        );
    }

    public BigDecimal convertToBrl(BigDecimal amount, Currency from) {
        if (from == Currency.BRL) return amount;
        BigDecimal rate = getRates().get(from);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
```

**exchange/dto/ExchangeRateResponse.java**

```java
public record ExchangeRateResponse(
        String base,
        String date,
        Map<String, BigDecimal> rates
) {}
```

**application.yaml**

```yaml
exchange:
  api:
    url: https://api.exchangerate-api.com/v4/latest/USD
  cache:
    ttl: 86400  # 24 hours
```

**API Externa**

- Provedor: [ExchangeRate-API](https://www.exchangerate-api.com/)
- Gratuita até 1500 requests/mês
- Response: taxas de conversão de USD para todas as moedas

---

### Phase 3: Dashboard DTOs

**Issue**: [#54 - feat(dashboard): criar DTOs de resposta](https://github.com/GuilhermeLuan/ongoing/issues/54)

**Objetivo**: Criar Data Transfer Objects para a resposta do endpoint de dashboard.

#### Tasks

- [ ] Criar pacote `dashboard/dto/`
- [ ] Criar `CategorySpending.java` (record)
- [ ] Criar `DashboardResponse.java` (record)
- [ ] Adicionar validações e documentação

#### Technical Details

**dashboard/dto/CategorySpending.java**

```java
/**
 * Gastos totais por categoria de assinatura.
 *
 * @param categoryName Nome da categoria (STREAMING, GAMING, etc)
 * @param total Total gasto na categoria (em BRL)
 */
public record CategorySpending(
                @NotBlank String categoryName,
                @NotNull @PositiveOrZero BigDecimal total
        ) {}
```

**dashboard/dto/DashboardResponse.java**

```java
/**
 * Estatísticas consolidadas do dashboard do usuário.
 * Todos os valores monetários estão em BRL.
 */
public record DashboardResponse(
                @NotNull List<CategorySpending> spendingByCategory,
                @NotNull @PositiveOrZero BigDecimal monthlyAverage,
                @NotNull @PositiveOrZero BigDecimal thisMonthTotal,
                @NotNull @PositiveOrZero BigDecimal yearlyTotal,
                @NotBlank String currency,
                @NotNull LocalDate exchangeRateDate
        ) {}
```

**Exemplo de Resposta JSON**

```json
{
  "spendingByCategory": [
    {
      "categoryName": "STREAMING",
      "total": 89.90
    },
    {
      "categoryName": "GAMING",
      "total": 44.90
    },
    {
      "categoryName": "PRODUCTIVITY",
      "total": 35.00
    }
  ],
  "monthlyAverage": 156.50,
  "thisMonthTotal": 134.80,
  "yearlyTotal": 1879.60,
  "currency": "BRL",
  "exchangeRateDate": "2026-02-03"
}
```

---

### Phase 4: Dashboard Service

**Issue
**: [#55 - feat(dashboard): implementar DashboardService com lógica de cálculo](https://github.com/GuilhermeLuan/ongoing/issues/55)

**Objetivo**: Implementar lógica de negócio para calcular estatísticas do dashboard.

#### Tasks

- [ ] Criar `DashboardService.java`
- [ ] Implementar `calculateSpendingByCategory()` - gastos agrupados por categoria
- [ ] Implementar `calculateMonthlyAverage()` - média mensal (anuais ÷ 12)
- [ ] Implementar `calculateThisMonthTotal()` - total do mês atual
- [ ] Implementar `calculateYearlyTotal()` - total anual (mensais × 12 + anuais)
- [ ] Integrar com `ExchangeRateService` para conversão de moedas
- [ ] Adicionar query no `SubscriptionsRepository`

#### Business Rules

1. **Assinaturas Ativas**: Apenas assinaturas com `active = true` são consideradas
2. **Conversão de Moeda**: Valores em USD/EUR são convertidos para BRL usando taxas atuais
3. **Billing Cycles**:
    - **MONTHLY**: Aparece em todos os meses, valor × 12 no yearly total
    - **YEARLY**: Aparece apenas no mês de pagamento, valor × 1 no yearly total
4. **This Month Total**: Soma apenas assinaturas cujo `nextBillingDate` cai no mês atual
5. **Ordenação**: Categorias ordenadas por total (maior primeiro)

#### Technical Details

**dashboard/DashboardService.java**

```java

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    private final SubscriptionsRepository subscriptionsRepository;
    private final ExchangeRateService exchangeRateService;

    public DashboardResponse getDashboard(Long userId) {
        List<Subscriptions> subscriptions = subscriptionsRepository.findActiveByUserId(userId);

        if (subscriptions.isEmpty()) {
            return createEmptyDashboard();
        }

        // Converter todas as assinaturas para BRL
        List<ConvertedSubscription> converted = subscriptions.stream()
                .map(this::convertToBrl)
                .toList();

        return new DashboardResponse(
                calculateSpendingByCategory(converted),
                calculateMonthlyAverage(converted),
                calculateThisMonthTotal(converted),
                calculateYearlyTotal(converted),
                "BRL",
                LocalDate.now()
        );
    }

    private ConvertedSubscription convertToBrl(Subscriptions subscription) {
        BigDecimal priceInBrl = exchangeRateService.convertToBrl(
                subscription.getPrice(),
                subscription.getCurrency()
        );
        return new ConvertedSubscription(subscription, priceInBrl);
    }

    private List<CategorySpending> calculateSpendingByCategory(
            List<ConvertedSubscription> subscriptions
    ) {
        return subscriptions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.subscription().getCategory(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                ConvertedSubscription::monthlyPrice,
                                BigDecimal::add
                        )
                ))
                .entrySet().stream()
                .map(e -> new CategorySpending(e.getKey().name(), e.getValue()))
                .sorted(Comparator.comparing(CategorySpending::total).reversed())
                .toList();
    }

    private BigDecimal calculateMonthlyAverage(List<ConvertedSubscription> subscriptions) {
        return subscriptions.stream()
                .map(ConvertedSubscription::monthlyPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateThisMonthTotal(List<ConvertedSubscription> subscriptions) {
        YearMonth currentMonth = YearMonth.now();

        return subscriptions.stream()
                .filter(s -> {
                    LocalDate nextBilling = s.subscription().getNextBillingDate();
                    return YearMonth.from(nextBilling).equals(currentMonth);
                })
                .map(ConvertedSubscription::priceInBrl)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateYearlyTotal(List<ConvertedSubscription> subscriptions) {
        BigDecimal monthlyTotal = subscriptions.stream()
                .filter(s -> s.subscription().getBillingCycle() == BillingCycle.MONTHLY)
                .map(ConvertedSubscription::priceInBrl)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(12));

        BigDecimal yearlyTotal = subscriptions.stream()
                .filter(s -> s.subscription().getBillingCycle() == BillingCycle.YEARLY)
                .map(ConvertedSubscription::priceInBrl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return monthlyTotal.add(yearlyTotal).setScale(2, RoundingMode.HALF_UP);
    }

    private record ConvertedSubscription(
            Subscriptions subscription,
            BigDecimal priceInBrl
    ) {
        BigDecimal monthlyPrice() {
            return subscription.getBillingCycle() == BillingCycle.MONTHLY
                    ? priceInBrl
                    : priceInBrl.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        }
    }
}
```

**subscriptions/SubscriptionsRepository.java** (adicionar método)

```java

@Query("SELECT s FROM Subscriptions s WHERE s.user.id = :userId AND s.active = true")
List<Subscriptions> findActiveByUserId(@Param("userId") Long userId);
```

---

### Phase 5: Dashboard Controller

**Issue
**: [#56 - feat(dashboard): criar endpoint GET /api/v1/dashboard](https://github.com/GuilhermeLuan/ongoing/issues/56)

**Objetivo**: Criar controller REST com endpoint para retornar estatísticas do usuário autenticado.

#### Tasks

- [ ] Criar `DashboardController.java`
- [ ] Implementar endpoint `GET /api/v1/dashboard`
- [ ] Extrair userId do JWT (UserPrincipal)
- [ ] Retornar DashboardResponse

#### Technical Details

**dashboard/DashboardController.java**

```java

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    private final DashboardService dashboardService;

    /**
     * Retorna estatísticas consolidadas do dashboard do usuário autenticado.
     *
     * @param authentication JWT authentication contendo UserPrincipal
     * @return Estatísticas de gastos com assinaturas
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).id();
        log.info("Fetching dashboard for user {}", userId);

        DashboardResponse response = dashboardService.getDashboard(userId);
        return ResponseEntity.ok(response);
    }
}
```

#### API Specification

**Endpoint**

```
GET /api/v1/dashboard
```

**Headers**

```
Authorization: Bearer <JWT_TOKEN>
```

**Response 200 OK**

```json
{
  "spendingByCategory": [
    {
      "categoryName": "STREAMING",
      "total": 89.90
    },
    {
      "categoryName": "GAMING",
      "total": 44.90
    }
  ],
  "monthlyAverage": 156.50,
  "thisMonthTotal": 134.80,
  "yearlyTotal": 1879.60,
  "currency": "BRL",
  "exchangeRateDate": "2026-02-03"
}
```

**Response 401 Unauthorized**

```json
{
  "timestamp": "2026-02-03T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/v1/dashboard"
}
```

---

### Phase 6: Unit Tests

**Issue
**: [#57 - test(dashboard): testes unitários do DashboardService](https://github.com/GuilhermeLuan/ongoing/issues/57)

**Objetivo**: Criar testes unitários para validar lógica de cálculo do dashboard.

#### Tasks

- [ ] Criar `DashboardServiceTest.java`
- [ ] Testar cálculo com assinaturas apenas em BRL
- [ ] Testar conversão de moeda USD → BRL
- [ ] Testar conversão de moeda EUR → BRL
- [ ] Testar que assinaturas inativas são ignoradas
- [ ] Testar `thisMonthTotal` considera apenas mês atual
- [ ] Testar `monthlyAverage` com assinaturas anuais (÷ 12)
- [ ] Testar `yearlyTotal` com mix de mensais e anuais
- [ ] Testar categorias ordenadas por total (maior primeiro)
- [ ] Testar cenário sem assinaturas (valores zerados)

#### Test Structure

```java

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock
    private SubscriptionsRepository subscriptionsRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private DashboardService dashboardService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "test@example.com", "Test User");

        // Mock exchange rates
        when(exchangeRateService.convertToBrl(any(), eq(Currency.BRL)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(exchangeRateService.convertToBrl(any(), eq(Currency.USD)))
                .thenAnswer(inv -> ((BigDecimal) inv.getArgument(0))
                        .multiply(BigDecimal.valueOf(5.0)));
        when(exchangeRateService.convertToBrl(any(), eq(Currency.EUR)))
                .thenAnswer(inv -> ((BigDecimal) inv.getArgument(0))
                        .multiply(BigDecimal.valueOf(5.5)));
    }

    @Test
    @DisplayName("Should calculate spending by category correctly")
    void shouldCalculateSpendingByCategoryCorrectly() {
        // given
        List<Subscriptions> subscriptions = List.of(
                createSubscription("Netflix", Category.STREAMING, 50.0, BillingCycle.MONTHLY),
                createSubscription("Spotify", Category.STREAMING, 30.0, BillingCycle.MONTHLY),
                createSubscription("Xbox", Category.GAMING, 45.0, BillingCycle.MONTHLY)
        );
        when(subscriptionsRepository.findActiveByUserId(1L)).thenReturn(subscriptions);

        // when
        DashboardResponse response = dashboardService.getDashboard(1L);

        // then
        assertThat(response.spendingByCategory()).hasSize(2);
        assertThat(response.spendingByCategory().get(0))
                .satisfies(cat -> {
                    assertThat(cat.categoryName()).isEqualTo("STREAMING");
                    assertThat(cat.total()).isEqualByComparingTo("80.00");
                });
        assertThat(response.spendingByCategory().get(1))
                .satisfies(cat -> {
                    assertThat(cat.categoryName()).isEqualTo("GAMING");
                    assertThat(cat.total()).isEqualByComparingTo("45.00");
                });
    }

    @Test
    @DisplayName("Should convert USD to BRL correctly")
    void shouldConvertUsdToBrlCorrectly() {
        // Test implementation
    }

    @Test
    @DisplayName("Should ignore inactive subscriptions")
    void shouldIgnoreInactiveSubscriptions() {
        // Test implementation
    }

    @Test
    @DisplayName("Should calculate this month total only for current month")
    void shouldCalculateThisMonthTotalOnlyForCurrentMonth() {
        // Test implementation
    }

    @Test
    @DisplayName("Should calculate monthly average with yearly subscriptions")
    void shouldCalculateMonthlyAverageWithYearlySubscriptions() {
        // Test implementation
    }

    @Test
    @DisplayName("Should calculate yearly total with mix of monthly and yearly")
    void shouldCalculateYearlyTotalWithMixOfMonthlyAndYearly() {
        // Test implementation
    }

    @Test
    @DisplayName("Should order categories by total descending")
    void shouldOrderCategoriesByTotalDescending() {
        // Test implementation
    }

    @Test
    @DisplayName("Should return zeroed values when no subscriptions")
    void shouldReturnZeroedValuesWhenNoSubscriptions() {
        // given
        when(subscriptionsRepository.findActiveByUserId(1L)).thenReturn(List.of());

        // when
        DashboardResponse response = dashboardService.getDashboard(1L);

        // then
        assertThat(response.spendingByCategory()).isEmpty();
        assertThat(response.monthlyAverage()).isEqualByComparingTo("0.00");
        assertThat(response.thisMonthTotal()).isEqualByComparingTo("0.00");
        assertThat(response.yearlyTotal()).isEqualByComparingTo("0.00");
    }
}
```

---

### Phase 7: Integration Tests

**Issue
**: [#58 - test(dashboard): testes de integração do DashboardController](https://github.com/GuilhermeLuan/ongoing/issues/58)

**Objetivo**: Criar testes de integração para validar endpoint completo com JWT e banco real.

#### Tasks

- [ ] Criar `DashboardControllerIT.java`
- [ ] Configurar Testcontainers com PostgreSQL + Redis
- [ ] Testar `GET /api/v1/dashboard` retorna 200 com JWT válido
- [ ] Testar retorna 401 sem JWT
- [ ] Testar multi-tenancy (usuário vê apenas suas assinaturas)
- [ ] Testar integração real com Redis cache
- [ ] Testar resposta com formato JSON correto

#### Test Structure

```java

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class DashboardControllerIT extends BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionsRepository subscriptionsRepository;

    @Autowired
    private JwtService jwtService;

    private String jwtToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = userRepository.save(new User(
                "test@example.com",
                "password123",
                "Test User"
        ));

        jwtToken = jwtService.generateToken(new UserPrincipal(
                testUser.getId(),
                testUser.getEmail(),
                testUser.getName()
        ));
    }

    @Test
    @DisplayName("Should return dashboard for authenticated user")
    void shouldReturnDashboardForAuthenticatedUser() {
        // given: user with subscriptions
        createSubscriptions(testUser);

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<DashboardResponse> response = restTemplate.exchange(
                "/api/v1/dashboard",
                HttpMethod.GET,
                request,
                DashboardResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().currency()).isEqualTo("BRL");
        assertThat(response.getBody().spendingByCategory()).isNotEmpty();
    }

    @Test
    @DisplayName("Should return 401 without JWT")
    void shouldReturn401WithoutJwt() {
        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/dashboard",
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should only show user's own subscriptions (multi-tenancy)")
    void shouldOnlyShowUserOwnSubscriptions() {
        // given: two users with different subscriptions
        User otherUser = userRepository.save(new User(
                "other@example.com",
                "password456",
                "Other User"
        ));

        createSubscriptions(testUser);
        createSubscriptions(otherUser);

        // when: testUser fetches dashboard
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<DashboardResponse> response = restTemplate.exchange(
                "/api/v1/dashboard",
                HttpMethod.GET,
                request,
                DashboardResponse.class
        );

        // then: sees only their subscriptions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // Verify calculations match only testUser's subscriptions
    }

    @Test
    @DisplayName("Should cache exchange rates in Redis")
    void shouldCacheExchangeRatesInRedis() {
        // Test caching behavior
    }
}
```

---

### Phase 8: Documentation

**Issue
**: [#59 - docs(dashboard): criar CLAUDE.md para módulos dashboard e exchange](https://github.com/GuilhermeLuan/ongoing/issues/59)

**Objetivo**: Documentar os módulos dashboard e exchange seguindo padrão do projeto.

#### Tasks

- [ ] Criar `dashboard/CLAUDE.md`
- [ ] Criar `exchange/CLAUDE.md`
- [ ] Documentar componentes, fluxos e regras de negócio
- [ ] Incluir exemplos de uso e configuração

#### dashboard/CLAUDE.md

```markdown
# Dashboard Module

## Overview

Módulo responsável por fornecer estatísticas consolidadas das assinaturas do usuário autenticado, incluindo gastos por
categoria, médias mensais e projeções anuais.

## Components

### DashboardController

- **Endpoint**: `GET /api/v1/dashboard`
- **Auth**: JWT Bearer token obrigatório
- **Response**: Estatísticas consolidadas em BRL

### DashboardService

- Calcula métricas de gastos
- Converte moedas via ExchangeRateService
- Filtra apenas assinaturas ativas do usuário

### DTOs

- **DashboardResponse**: Resposta completa do dashboard
- **CategorySpending**: Gastos por categoria

## Business Rules

1. Apenas assinaturas ativas (`active = true`)
2. Multi-tenancy: usuário vê apenas suas assinaturas
3. Conversão automática para BRL
4. Assinaturas anuais são divididas por 12 na média mensal
5. Categorias ordenadas por total (maior primeiro)

## Usage Example

```bash
curl -X GET http://localhost:6969/api/v1/dashboard \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

## Testing

- **Unit**: `DashboardServiceTest` - lógica de cálculo
- **Integration**: `DashboardControllerIT` - endpoint completo com DB

```

#### exchange/CLAUDE.md

```markdown
# Exchange Rate Module

## Overview

Módulo responsável por buscar e cachear taxas de câmbio para conversão de moedas (USD/EUR → BRL).

## Components

### ExchangeRateService
- Busca taxas via API externa
- Cache Redis com TTL de 24h
- Conversão de valores para BRL

### ExchangeRateClient
- Cliente HTTP para API externa
- Endpoint: `https://api.exchangerate-api.com/v4/latest/USD`

## Caching Strategy

- **Cache Key**: `"exchange-rates:BRL"`
- **TTL**: 24 horas
- **Provider**: Redis
- **Refresh**: Automático após expiração

## Configuration

```yaml
exchange:
  api:
    url: https://api.exchangerate-api.com/v4/latest/USD
  cache:
    ttl: 86400  # 24 hours
```

## Supported Currencies

- **BRL**: Brazilian Real (default/target)
- **USD**: US Dollar
- **EUR**: Euro

## Usage Example

```java
BigDecimal priceInBrl = exchangeRateService.convertToBrl(
        new BigDecimal("10.00"),
        Currency.USD
);
```

```

---

## Testing Strategy

### Unit Tests
- **Scope**: Business logic in isolation
- **Tools**: JUnit 5, Mockito, AssertJ
- **Coverage**: DashboardService calculations, ExchangeRateService caching

### Integration Tests
- **Scope**: Full endpoint with real dependencies
- **Tools**: Spring Boot Test, Testcontainers, RestAssured
- **Coverage**: DashboardController, JWT auth, multi-tenancy, Redis cache

### Test Data
- Multiple users with different subscriptions
- Mix of currencies (BRL, USD, EUR)
- Mix of billing cycles (Monthly, Yearly)
- Active and inactive subscriptions

---

## Migration Path

### Database Changes
**Not required** - reuses existing `subscriptions` table.

### New Configuration
```yaml
# application.yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  cache:
    type: redis

exchange:
  api:
    url: https://api.exchangerate-api.com/v4/latest/USD
```

### Environment Variables

```bash
REDIS_HOST=localhost
REDIS_PORT=6379
```

---

## Rollout Plan

1. **Deploy Infrastructure**: Redis container via docker-compose
2. **Deploy Exchange Module**: API client + caching
3. **Deploy Dashboard Module**: Service + controller
4. **Verify**: Integration tests pass
5. **Monitor**: Cache hit rates, API response times

---

## Monitoring & Observability

### Metrics to Track

- Dashboard endpoint response time
- Exchange rate API latency
- Redis cache hit/miss ratio
- Active subscriptions per user

### Logs

```java
log.info("Fetching dashboard for user {}",userId);
log.

debug("Cache hit for exchange rates: {}",cacheKey);
log.

warn("Exchange rate API failed, using cached value");
```

---

## Future Enhancements

- Support for more currencies (GBP, JPY, CAD)
- Fallback to secondary exchange rate provider
- Historical exchange rate tracking
- User preference for dashboard currency
- Spending trends over time (graphs)

---

## References

- [GitHub Issues #52-#59](https://github.com/GuilhermeLuan/ongoing/milestone/4)
- [ExchangeRate-API Docs](https://www.exchangerate-api.com/docs/overview)
- [Spring Cache Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Redis Spring Boot Starter](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.nosql.redis)
