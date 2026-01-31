# Status Module

Módulo simples para health check da aplicação.

## Componentes Principais

### StatusController

Endpoint REST para verificação de saúde:

- `GET /api/v1/status` - Retorna status da aplicação

**Importante**: Endpoint é **público** (não requer autenticação).
Configurado no `SpringSecurityConfig` como `.permitAll()`.

### StatusService

Lógica de negócio para status:

- **getStatus()** - Verifica conexão com banco de dados e retorna status

**Implementação**:

```java
public StatusResponse getStatus() {
    // Executa query simples para verificar conexão com DB
    Long count = statusRepository.count();

    // Se chegou aqui, DB está OK
    return new StatusResponse("ok", "Database connected");
}
```

### StatusRepository

Interface Spring Data JPA:

```java
public interface StatusRepository extends JpaRepository<User, Long> {
    // Usa count() para verificar conexão com DB
}
```

**Pattern**: Usa repository de User para verificar DB (não precisa de tabela específica).

### StatusResponse

Record para resposta:

```java
public record StatusResponse(
        String status,    // "ok" ou "error"
        String message    // Mensagem descritiva
)
```

Exemplo de resposta:

```json
{
  "status": "ok",
  "message": "Database connected"
}
```

## Uso

### Health Check Básico

```bash
curl http://localhost:6969/api/v1/status
```

Resposta (200 OK):

```json
{
  "status": "ok",
  "message": "Database connected"
}
```

### Verificação de Infraestrutura

Útil para:

- Load balancers verificarem se app está saudável
- Monitoring tools (Prometheus, Datadog, etc.)
- CI/CD pipelines verificarem deploy bem-sucedido
- Kubernetes liveness/readiness probes

## Tratamento de Erros

Se banco de dados está offline:

- `statusRepository.count()` lança exceção
- Spring retorna 500 Internal Server Error
- Resposta não é JSON customizado (exceção não tratada propositalmente)

**Futuro**: Pode adicionar try-catch para retornar:

```json
{
  "status": "error",
  "message": "Database connection failed"
}
```

## Segurança

Endpoint é público porque:

- Não expõe informações sensíveis
- Necessário para load balancers/monitoring
- Apenas confirma que app está respondendo

**Não inclui**:

- Informações de versão do DB
- Credenciais ou configurações
- Dados de usuários
- Métricas detalhadas

## Possíveis Melhorias

1. **Health Checks Adicionais**:
    - Verificar Redis (se usado)
    - Verificar APIs externas
    - Verificar espaço em disco

2. **Métricas Detalhadas**:
    - Tempo de resposta do DB
    - Número de conexões ativas
    - Uso de memória/CPU

3. **Spring Boot Actuator**:
    - Usar `/actuator/health` padrão
    - Endpoints de métricas, info, env
    - Mais robusto e feature-rich

4. **Diferentes Níveis de Health**:
    - Liveness: App está rodando?
    - Readiness: App está pronto para receber tráfego?

## Testing

Testes de integração podem verificar:

- Endpoint retorna 200 OK
- Formato de resposta correto
- Verificação real de DB (com Testcontainers)
