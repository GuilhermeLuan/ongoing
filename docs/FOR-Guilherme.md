# FOR-Guilherme.md

## O Que Diabos é o "Ongoing"?

Sabe aquele momento do mês em que você olha o extrato do cartão e pensa: "Peraí, eu ainda pago Netflix, Spotify, Amazon
Prime, aquela academia que eu não vou há 3 meses, e... o que é essa cobrança de R$29,90?"

O **Ongoing** é a solução para esse caos. É uma API REST que te ajuda a rastrear todas as suas assinaturas recorrentes —
de streaming a academia, de software a seguro. Ele calcula automaticamente quando vem a próxima cobrança, organiza por
categoria, e te dá visibilidade total sobre para onde está indo seu dinheiro todo mês.

---

## A Arquitetura: Como um Restaurante Bem Organizado

Imagina um restaurante. Você não quer que o garçom vá até a cozinha pegar os ingredientes, cozinhar, e ainda lavar a
louça, né? Cada um tem seu papel:

- **Garçom (Controller)**: Recebe o pedido do cliente e leva para a cozinha
- **Chef (Service)**: Sabe a receita, combina os ingredientes, faz a mágica acontecer
- **Despensa (Repository)**: Guarda e busca os ingredientes
- **Cardápio (DTO)**: O que o cliente vê — não precisa saber que o molho especial leva 47 ingredientes

É exatamente assim que o Ongoing funciona:

```
Cliente faz request → Controller recebe → Service processa → Repository busca/salva → Resposta volta
```

### A Estrutura de Pastas

```
src/main/java/dev/guilhermeluan/ongoing/
├── subscriptions/          # Tudo sobre assinaturas mora aqui
│   ├── entities/           # Os "objetos reais" do banco
│   ├── dto/                # O que entra e sai da API
│   ├── Controller          # Endpoints REST
│   ├── Service             # Regras de negócio
│   ├── Repository          # Acesso ao banco
│   └── Mapper              # Traduz DTO ↔ Entity
├── status/                 # Health check da aplicação
└── exception/              # Tratamento centralizado de erros
```

Essa organização é chamada **package-by-feature** (organizado por funcionalidade). É como organizar sua casa por
cômodos (cozinha, quarto, banheiro) ao invés de por tipo de objeto (todas as cadeiras juntas, todos os copos juntos).
Faz muito mais sentido!

---

## As Tecnologias: Por Que Essas Escolhas?

### Java 25 + Spring Boot 4

A dupla mais sólida do mundo enterprise. Spring Boot é tipo um canivete suíço — faz de tudo e faz bem. A versão 4 traz
melhorias de performance e suporte nativo a recursos modernos do Java.

### Virtual Threads (A Revolução Silenciosa)

Antes: cada request ocupava uma thread do sistema operacional. Com 200 threads, você atendia 200 requests simultâneos.
Acabou? Fila.

Agora: Virtual threads são gerenciadas pela JVM, são leves como penas. Você pode ter milhares rodando sem suar. É como
trocar de carros por bicicletas numa cidade congestionada — muito mais gente se move ao mesmo tempo.

```yaml
spring:
  threads:
    virtual:
      enabled: true  # Uma linha. Só isso. Magia.
```

### PostgreSQL + Flyway

PostgreSQL é o banco "adulto" — robusto, confiável, usado por empresas sérias. Mas o pulo do gato é o **Flyway**.

Sabe quando você faz uma mudança no banco local e esquece de fazer em produção? Ou pior, faz diferente? Flyway versiona
seu banco de dados como se fosse código. Cada migration é um arquivo SQL numerado:

```
V1__creates_subscription_table.sql     # Cria as tabelas
V1.1__insert_domains_value.sql         # Popula dados iniciais
V2__add_currency_constraint.sql        # Adiciona constraint
```

Quando a aplicação sobe, Flyway verifica: "Quais migrations já rodei? Quais faltam?" E aplica só o que falta. Deploy em
produção sem medo!

### MapStruct (O Tradutor Invisível)

Converter DTO para Entity na mão é tedioso e propenso a erro:

```java
// Jeito chato e perigoso
entity.setName(dto.getName());
entity.setValue(dto.getValue());
entity.setNotify(dto.getNotifyUser());  // Ops, esqueci esse campo!
// ... mais 15 campos
```

MapStruct gera esse código automaticamente em tempo de compilação. Você só define a interface:

```java

@Mapper
public interface SubscriptionsMapper {
    SubscriptionResponseDto toSubscriptionResponse(Subscriptions subscription);
}
```

E ele cria a implementação perfeita. Se você renomear um campo e esquecer de atualizar, **não compila**. Erro em tempo
de build é muito melhor que erro em produção às 3h da manhã.

### BigDecimal para Dinheiro (NUNCA use double!)

```java
double a = 0.1 + 0.2;
System.out.

println(a);  // 0.30000000000000004  🤯
```

Floating point é aproximação. Para dinheiro, você precisa de precisão exata. `BigDecimal` é mais lento, mas correto.
Quando você cobra R$9,99 do cliente, ele paga R$9,99 — não R$9,990000000001.

### Testcontainers (Testes de Verdade)

Testes com banco H2 em memória são mentirosos. Funcionam no teste, quebram em produção porque PostgreSQL tem
comportamentos diferentes.

Testcontainers sobe um PostgreSQL **real** em Docker durante os testes. Você testa contra o mesmo banco que vai rodar em
produção. É mais lento? Sim. Vale a pena? Absolutamente.

```java

@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
```

---

## Dashboard: Quando Dados Precisam de "Unidade" Comum

O dashboard (gastos por categoria, total do mes, media mensal, projeção anual) tem um problema classico: cada assinatura
vem em um ciclo e (as vezes) em uma moeda diferente. Se voce somar tudo "cru", vira salada.

Pra resolver isso, a gente cria uma pequena abstracao: `ConvertedSubscription`.

- Ele carrega a `Subscriptions` original + o `priceInBrl` (valor ja convertido)
- Ele sabe calcular o "equivalente mensal" (`monthlyPrice()`) e o "custo anual" (`yearlyPrice()`) com base no billing
  cycle
- Ele tambem ajuda a responder a pergunta: "isso vence neste mes?" (`isDueIn(YearMonth)`)

Em outras palavras: a entity continua sendo o "dado bruto" do banco, e o record vira a "unidade de conta" do dashboard.

Licao pratica: sempre que voce for agregar valores (soma, media, ranking), garanta que todo mundo esta na mesma unidade
antes de fazer conta.

---

## O Fluxo de uma Request: Anatomia Completa

Vamos seguir o caminho de uma request POST para criar uma assinatura:

### 1. Request Chega no Controller

```
POST /api/v1/subscriptions
{
  "name": "Netflix",
  "value": 55.90,
  "billingCycleId": 1,
  "categoryId": 1
}
```

### 2. Validação Automática

O Spring valida o DTO automaticamente pelas annotations:

```java
public record SubscriptionRequestDto(
    @NotBlank(message = "Name is required")
    String name,

    @NotNull(message = "Value is required")
    @Positive(message = "Value must be positive")
    BigDecimal value,
    // ...
) {}
```

Se falhar, o `GlobalExceptionHandler` captura e retorna um erro bonitinho:

```json
{
  "status": 400,
  "message": "Name is required, Value must be positive"
}
```

### 3. Service Processa

O serviço recebe o DTO, usa o Mapper para converter em Entity, calcula a próxima data de cobrança, e salva:

```java
public SubscriptionResponseDto save(SubscriptionRequestDto dto) {
    var subscription = mapper.toSubscription(dto);
    calculateNextBillingDate(subscription);  // Calcula próximo pagamento
    return mapper.toSubscriptionResponse(repository.save(subscription));
}
```

### 4. Repository Persiste

Spring Data JPA gera a query automaticamente. Você não escreve SQL para operações básicas.

### 5. Resposta Volta

```json
{
  "id": 1,
  "name": "Netflix",
  "value": 55.90,
  "nextPaymentDate": "2024-02-15",
  "active": true
}
```

---

## Tratamento de Erros: Uma Estratégia Centralizada

Ao invés de try-catch espalhado pelo código, temos um `GlobalExceptionHandler`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(new ApiError(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        // Coleta todos os erros, ordena, e junta em uma mensagem
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .sorted()
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ApiError(400, errors));
    }
}
```

**Por que isso é genial?**

1. **Consistência**: Toda resposta de erro segue o mesmo formato
2. **Manutenção**: Um lugar só para modificar
3. **Logging**: Adicionar logs é trivial
4. **Métricas**: Fácil adicionar contadores de erros

---

## Lições Aprendidas e Armadilhas

### 1. O Bug do "Magic Number"

No código atual, o cálculo de billing date usa IDs hardcoded:

```java
if(billingCycleId ==1L){
        return startDate.

plusMonths(1);  // Mensal
}else if(billingCycleId ==2L){
        return startDate.

plusYears(1);   // Anual
}
```

**O problema**: Se alguém reordenar os billing cycles no banco, quebra tudo silenciosamente.

**A lição**: Nunca confie em IDs. Use enums ou códigos textuais (`MONTHLY`, `ANNUAL`). IDs são detalhes de
implementação.

### 2. Por Que Separar Request e Response DTOs?

Parece duplicação, mas:

- **Request** tem validações (`@NotBlank`, `@Positive`)
- **Response** tem campos calculados (`nextPaymentDate`)
- **Request** recebe IDs (`categoryId: 1`)
- **Response** pode expandir objetos (`category: { id: 1, name: "Streaming" }`)

Se você usar um DTO só, vai expor campos internos ou aceitar dados que não deveria.

### 3. FetchType.LAZY é Seu Amigo

```java

@ManyToOne(fetch = FetchType.LAZY)
private Category category;
```

Sem LAZY, cada vez que você busca uma subscription, o JPA busca também category, billingCycle, paymentMethod... mesmo
que você não precise. Com LAZY, ele só busca quando você acessa o campo.

**Armadilha**: Se você acessar um campo LAZY fora de uma transação, toma `LazyInitializationException`. A solução é usar
DTOs (que são carregados dentro da transação) ou `@Transactional` no service.

### 4. Testes de Integração Paralelos

```xml

<forkCount>2C</forkCount>  <!-- 2 vezes o número de CPUs -->
```

Isso acelera os testes, mas cuidado: seus testes não podem depender de estado compartilhado. Cada teste deve ser
independente.

### 5. Profiles São Poderosos

```yaml
# Dev: mostra tudo, auto-cria tabelas
spring.jpa.show-sql: true
spring.jpa.hibernate.ddl-auto: update

# Prod: esconde tudo, só valida
spring.jpa.show-sql: false
spring.jpa.hibernate.ddl-auto: validate
```

**Nunca** use `ddl-auto: update` em produção. Hibernate pode decidir dropar uma coluna que ele acha "desnecessária".

---

## Como Bons Engenheiros Pensam

### 1. Defesa em Profundidade

O projeto valida moeda em 3 lugares:

- **DTO**: Enum `Currency` no Java
- **Entity**: Mesmo enum
- **Banco**: `CHECK (currency IN ('BRL', 'USD', 'EUR'))`

Parece redundante? É proposital. Se uma camada falhar, as outras pegam.

### 2. Fail Fast

Validações acontecem na entrada (Controller). Se o dado é inválido, falha imediatamente com erro claro. Não deixa dados
ruins entrarem no sistema para falhar misteriosamente depois.

### 3. Imutabilidade Quando Possível

DTOs são `record` — imutáveis por padrão. Você não pode acidentalmente modificar um DTO no meio do processamento e
causar bugs estranhos.

### 4. Convenção Sobre Configuração

Spring Boot assume padrões sensatos. Você só configura o que é diferente. Menos código = menos bugs.

### 5. Testes São Documentação Executável

Olhe os testes de integração:

```java

@Test
void findById_ReturnsSubscription_WhenIdExists()

@Test
void findById_ThrowsNotFoundException_WhenIdDoesNotExist()
```

Os nomes dizem exatamente o que o sistema faz. E ao contrário de documentação escrita, testes falham se ficarem
desatualizados.

---

## O Que Esse Projeto Te Ensina

1. **Arquitetura em camadas** não é burocracia — é organização que escala
2. **Tipagem forte** (DTOs, Enums) previne erros antes de acontecerem
3. **Testes de integração** com banco real pegam bugs que mocks escondem
4. **Migrations** versionadas são obrigatórias para qualquer projeto sério
5. **Tratamento de erros centralizado** mantém APIs consistentes
6. **Virtual threads** são o futuro da concorrência em Java
7. **MapStruct** elimina código tedioso sem runtime overhead

---

## Próximos Passos Naturais

Se quiser evoluir o projeto:

1. **Paginação**: `GET /subscriptions?page=0&size=10`
2. **Filtros**: `GET /subscriptions?category=streaming&active=true`
3. **Dashboard**: Endpoint que soma gastos por categoria
4. **Notificações**: Job que avisa antes da cobrança
5. **Multi-tenancy**: Suporte a múltiplos usuários
6. **Cache**: Redis para queries frequentes

---

## O Que Mudou Agora: Modulo de Cambio (Phase 2 do Dashboard)

Pra montar um dashboard decente, a gente precisa comparar coisas na mesma moeda.
O problema e que suas assinaturas podem estar em BRL, USD e EUR. Entao a "Phase 2" do plano cria um modulo que:

- Busca taxas de cambio em uma API externa
- Converte valores para BRL
- Usa cache (Redis) pra nao ficar batendo na API toda hora

### O Jeito Moderno do Spring Boot 4: HttpExchange

Antes, o normal era usar `RestTemplate` (antigo) ou escrever muito codigo em cima de `WebClient`.
No Spring moderno, da pra declarar um cliente HTTP como uma interface e anotar com `@HttpExchange`.
O Spring cria um proxy pra voce e voce chama como se fosse um metodo Java comum.

No projeto, isso ficou assim:

- `backend/src/main/java/dev/guilhermeluan/ongoing/exchange/ExchangeRateClient.java`
- `backend/src/main/java/dev/guilhermeluan/ongoing/exchange/ExchangeRateClientConfig.java`

E a regra do jogo e simples:

- A API externa retorna as taxas com base em uma moeda (por padrao, USD)
- A gente deriva `USD->BRL` e `EUR->BRL`
- O resto do sistema so enxerga "valor em BRL" e segue a vida

### Caching: o truque que salva o limite de requests

O metodo `getRatesToBrl()` em `backend/src/main/java/dev/guilhermeluan/ongoing/exchange/ExchangeRateService.java` esta
anotado com:

`@Cacheable(value = "exchange-rates", key = "'BRL'")`

Ou seja: na primeira chamada, ele bate na API e guarda o resultado; nas proximas, ele volta do Redis.
Isso e exatamente o tipo de otimizacao "pequena" que vira grande quando voce tem varios usuarios acessando o dashboard.

---

## Preparando o terreno do Dashboard: DTOs (Phase 3)

Antes de escrever a logica do dashboard, a gente define o contrato de resposta.
Esses records vivem em:

- `backend/src/main/java/dev/guilhermeluan/ongoing/dashboard/dto/CategorySpending.java`
- `backend/src/main/java/dev/guilhermeluan/ongoing/dashboard/dto/DashboardResponse.java`

Detalhe importante: como `Category` no seu modelo e uma entidade (tem `id` e `name`), o dashboard manda os dois.
O frontend pode usar `categoryId` como chave estavel e `categoryName` como label.

A arquitetura atual suporta tudo isso sem grandes refatorações. Esse é o sinal de um bom design.

---

## Correção Quentinha: Silent Refresh vs React Strict Mode

Sabe aquele bug fantasma que só aparece em desenvolvimento? Descobrimos que o Next.js 14 roda o React em modo estrito, o que monta e desmonta componentes duas vezes para caçar efeitos colaterais. Nosso `AuthContext` fazia o silent refresh em um `useEffect` sem guarda. Resultado: duas requisições usando o mesmo refresh token. A primeira funcionava, a segunda tomava 401 e limpava tudo, derrubando a sessão e quebrando o `GuestRoute` (achava que ninguém estava logado).

O conserto foi simples, mas certeiro: adicionamos um `useRef` chamado `refreshAttempted`. Ele garante que o silent refresh só roda uma vez, mesmo que o React tente ser esperto. Agora o usuário mantém a sessão ao recarregar a página e, se tentar acessar `/login` já logado, é redirecionado corretamente para `/dashboard`. Moral da história: em ambientes com Strict Mode, sempre trate efeitos como se fossem executados duas vezes.

---

## Correção Quentinha: CORS para Frontend em Produção

Outro bug clássico de deploy: tudo funciona localmente e quebra no navegador em produção com erro de CORS. O backend não
estava respondendo com os headers de origem permitida, então o browser bloqueava chamadas do frontend no Railway.

A solução foi configurar CORS dentro do `SecurityFilterChain`, com origens vindas de ambiente (`CORS_ALLOWED_ORIGINS`)
em vez de hardcode. Isso deixa o backend flexível para dev e produção, libera preflight `OPTIONS`, permite credenciais e
mantém métodos/headers explícitos (`GET, POST, PUT, DELETE, PATCH, OPTIONS` e `Authorization, Content-Type`).

Lição prática: quando frontend e backend estão em domínios diferentes, CORS não é detalhe opcional; é parte da
infraestrutura da aplicação e deve ser tratado como configuração de ambiente.

---

## Correção Quentinha: Performance Mobile e Safe Areas (iOS Safari)

Esse foi um daqueles bugs que parecem "só visual", mas na prática afetam confiança e sensação de qualidade: barra preta
no topo em iPhone, travadinhas ao abrir modal e animações custosas em telas menores.

O pacote de correções teve três ideias-chave:

1. **Respeitar a área segura do dispositivo**  
   O layout passou a usar `viewportFit: "cover"` e os componentes fixos/sticky (header, sidebar e modal) receberam
   `env(safe-area-inset-*)`. Isso evita vazamento visual perto do notch e deixa o app "encaixado" no device certo.

2. **Scroll lock sem recalcular tudo no Safari**  
   Em vez de `overflow: hidden` no `body`, o modal fixa o body com `position: fixed`, guarda o `scrollY` e restaura ao
   fechar. Resultado: menos custo de layout e abertura mais fluida no mobile.

3. **Efeitos caros só quando fazem sentido**  
   Blur pesado e animações infinitas ficaram restritos ao desktop (`md:`). No mobile, reduzimos blur e removemos
   animações contínuas decorativas. Também trocamos `transition-all` por transições de propriedades específicas.

Lição prática: performance mobile raramente melhora com "uma bala de prata". Ela melhora quando você elimina pequenas
fontes de custo em camadas (layout, animação, GPU e comportamento de scroll).

---

## Ajuste fino: Menu hambúrguer da Landing no iOS

Depois das otimizações mobile, apareceu um detalhe chato na landing em iPhone: ao abrir o menu hambúrguer, a sensação
visual ainda ficava ruim e os CTAs ("Entrar" e "Começar grátis") podiam ficar "colados".

A correção foi simples e certeira no `Header`:

- header mobile passa a manter fundo sólido branco quando o menu está aberto (evita contraste ruim no topo);
- dropdown mobile virou um painel com espaçamento/safe-area melhor no rodapé;
- links dos CTAs viraram blocos (`className="block"`), com `flex-col + gap`, garantindo separação consistente entre os
  botões.

Lição prática: em UI mobile, detalhes de `display` (inline vs block) e estado visual do header aberto/fechado impactam
mais do que parece — especialmente no Safari iOS.

---

## Ajuste global: fundo preto no iOS (login/register/dashboard)

Na sequência, apareceu um comportamento clássico de Safari em iPhone: mesmo com telas claras, alguns contextos ainda
exibiam fundo preto (principalmente em login/register e em transições).

Correção aplicada em camadas:

- forçamos `color-scheme: light` no `:root` e `themeColor: "#ffffff"` no `viewport` da aplicação;
- removemos a troca automática para variáveis escuras por `prefers-color-scheme: dark`;
- ajustamos wrappers principais para `min-h-[100dvh]` (auth/app/loading), reduzindo artefatos de viewport no iOS.
- na autenticação, o container mobile também passou a carregar o gradiente da marca, para manter continuidade visual no
  topo.

Lição prática: no iOS, o problema nem sempre é "a tela". Muitas vezes é combinação de color scheme + viewport dinâmica.

---

## Conclusão

O Ongoing não é só um CRUD de assinaturas. É um exemplo de como estruturar uma aplicação Spring Boot moderna seguindo
boas práticas da indústria. Cada decisão técnica tem um porquê, cada padrão resolve um problema real.

Quando você entender não só o *que* esse código faz, mas *por que* ele faz dessa forma, você terá dado um grande passo
como engenheiro de software.

Agora vai lá e adiciona aquela feature que você está pensando. A base está sólida. 🚀
