# Plano de implementação — Testes de notificação por e-mail (backend/notification)

## Problema

Em produção, o disparo de e-mail de renovação não aconteceu. Hoje o fluxo depende de `@Scheduled` em
`NotificationScheduler`, consulta de assinaturas por data (`nextPaymentDate = amanhã`, `active = true`,
`notify = true`), envio via Resend (`EmailService`) e deduplicação por Redis.

## Estado atual (análise do código)

- Existe agendador ativo (`@EnableScheduling`) com cron fixo `0 0 8 * * *`.
- O scheduler captura exceções por usuário e apenas loga erro, sem superfície operacional para validação rápida.
- Já há cobertura de testes unitários/integrados para template, serviço e agrupamento de assinaturas, mas não há teste
  de “disparo real” com evidência de entrega.
- Configuração de `resend.api_key`/`resend.from_email` vem de env vars; `.envTemplate` não documenta variáveis de
  e-mail/redis de produção.

## Objetivo do plano

Criar uma estratégia confiável para validar envio sem depender apenas do cron diário, reduzindo risco de regressão em
produção e melhorando diagnóstico.

## Decisão de escopo (confirmada)

- Validar manualmente via **runner/CLI one-shot** (sem endpoint HTTP de teste).

## Abordagem proposta

1. **Observabilidade e diagnóstico primeiro**
    - Enriquecer logs do job (quantos elegíveis, quantos enviados, quantos falharam, motivo principal).
    - Registrar explicitamente quando não há candidatos (`grouped` vazio).
2. **Gatilho manual seguro para teste operacional**
    - Implementar **runner/CLI one-shot** para executar `sendRenewalReminders` sob demanda.
    - Permitir modo `dryRun=true` (lista elegíveis/contagem) e modo envio real.
    - Política escolhida: **fail-open no runner** (se Redis cair, envia mesmo sem dedupe) e **fail-closed no scheduler
      **.
3. **Testes automatizados de ponta a ponta do fluxo**
    - Teste de integração do scheduler + Redis + serviço de e-mail com mock do Resend (validar tentativa de envio e
      payload).
    - Cenários: elegível, sem elegíveis, chave Redis existente, falha no provider.
4. **Checklist operacional de produção**
    - Validar env vars: `RESEND_API_KEY`, `RESEND_FROM_EMAIL`, `REDIS_*`, timezone do host.
    - Confirmar domínio/remetente no Resend e status de entrega (dashboard/webhooks).

## Alternativas ao “endpoint só para teste”

1. **JobRunner por comando** (execução one-shot via argumento/env em startup) — evita endpoint HTTP.
2. **Cron configurável por propriedade** + janela curta em staging para validar disparo real.
3. **Fila/outbox + worker** para rastreabilidade de tentativas (mais robusto, maior esforço).
4. **Webhook de eventos do provider** (delivered/bounced/blocked) para fechar o ciclo de observabilidade.

## Trade-offs das 3 opções de validação manual

1. **Endpoint interno autenticado (admin)**
    - **Prós:** rápido para operar, não exige restart, facilita `dryRun` e uso por suporte/engenharia.
    - **Contras:** aumenta superfície HTTP sensível, exige controle forte de autorização/auditoria/rate limit.
2. **Comando one-shot (runner/CLI)**
    - **Prós:** menor superfície de ataque (sem endpoint novo), execução explícita e controlada por deploy shell.
    - **Contras:** operação mais manual, normalmente requer acesso de infraestrutura e pode exigir restart/job separado.
3. **Só testes automatizados + logs (sem gatilho manual)**
    - **Prós:** menor risco em produção, foca em qualidade contínua e observabilidade.
    - **Contras:** não resolve validação imediata on-demand em prod quando surge incidente.

## Todos planejados

- `investigate-prod-delivery-gap`: mapear causa provável (dados elegíveis, redis lock, provider, timezone, credenciais).
- `design-safe-manual-trigger`: desenhar runner/CLI one-shot com flags de `dryRun` e envio real, sem endpoint HTTP.
- `add-notification-observability`: especificar logs/métricas mínimas para diagnóstico em produção.
- `define-e2e-notification-tests`: definir suíte de testes automatizados para cobrir envio, dedupe e falha do provider.
- `document-runbook`: documentar checklist operacional e procedimento de validação pós-deploy.

## Notas / riscos

- Runner/CLI exige controle de execução operacional (quem roda, quando roda, e evidência de execução).
- Como o scheduler usa `LocalDate.now()`, timezone do servidor pode deslocar o “amanhã” esperado.
- Falha de Redis/Resend pode impedir envio; hoje isso fica apenas em log.
