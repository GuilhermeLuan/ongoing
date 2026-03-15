# Roadmap do Projeto Ongoing

**Última atualização:** 17/02/2026

## Status Atual

O Ongoing é uma plataforma full-stack de gerenciamento de assinaturas recorrentes, construída como monorepo com Spring Boot (backend) e Next.js (frontend).

---

## ✅ Fase 1: Fundação (Concluída)

### Backend - API REST
- [x] Configuração inicial Spring Boot 4 + Java 25 (migrado para Java 17)
- [x] PostgreSQL + Flyway migrations
- [x] Entidades JPA (User, Subscription, RefreshToken)
- [x] CRUD de assinaturas com paginação
- [x] Cálculo automático de datas de renovação
- [x] Filtros por nome, categoria e status
- [x] MapStruct para mapeamento DTO ↔ Entity
- [x] Testcontainers para testes de integração
- [x] Virtual threads habilitadas

### Frontend - Landing Page & Dashboard
- [x] Next.js 14 com App Router
- [x] TypeScript + Tailwind CSS
- [x] Design system customizado (cores, componentes UI)
- [x] Landing page responsiva (Hero, Features, Pricing, CTA)
- [x] Dashboard com layout modular (Sidebar, Header)
- [x] Componentes de visualização de assinaturas
- [x] Mock data para desenvolvimento

---

## ✅ Fase 2: Autenticação & Segurança (Concluída)

### JWT Authentication
- [x] Sistema de autenticação com JWT
- [x] Endpoints: register, login, refresh
- [x] Token rotation (refresh token de uso único)
- [x] Refresh token armazenado no banco
- [x] Access token em memória (frontend)
- [x] Roles e autorização (USER, ADMIN)

### Frontend Auth
- [x] AuthContext com React Context API
- [x] Silent refresh ao carregar app
- [x] Interceptors Axios para refresh automático
- [x] Telas de login e registro
- [x] Proteção de rotas (ProtectedRoute, GuestRoute)
- [x] Token storage utilities

---

## ✅ Fase 3: Notificações (Concluída)

### Email Reminders
- [x] Sistema de notificações por email
- [x] Templates HTML responsivos
- [x] Scheduled task para envio automático
- [x] Lógica de agrupamento (diário/semanal)
- [x] Controle de frequência por usuário
- [x] Spring Mail + JavaMail

---

## ✅ Fase 4: Onboarding (Concluída) 🎉

### Backend
- [x] Campo `onboardingCompleted` na entidade User
- [x] Migration Flyway (V6)
- [x] Endpoint PATCH /api/v1/users/me
- [x] UserController e UserService
- [x] AuthResponse inclui dados do usuário (name, email, onboardingCompleted)

### Frontend - Wizard de 4 Passos
- [x] Route group `/onboarding`
- [x] Layout limpo (sem sidebar)
- [x] **Passo 1:** Boas-vindas com animações
- [x] **Passo 2:** Adicionar primeira assinatura
  - Sugestões de serviços populares (Netflix, Spotify, etc)
  - Formulário manual completo
  - Integração real com POST /api/v1/subscriptions
- [x] **Passo 3:** Tour animado do dashboard
  - Mockup interativo com highlights
  - Auto-advance com navegação manual
- [x] **Passo 4:** Tela de sucesso com confetti
  - Resumo da assinatura criada
  - Próximos passos
  - PATCH onboardingCompleted: true
- [x] ProgressBar com indicador de progresso
- [x] Animações CSS puras (fadeInUp, scaleIn, confetti)
- [x] Redirecionamento automático após registro

### Integrações
- [x] Registro redireciona para /onboarding
- [x] AuthContext atualizado para incluir onboardingCompleted
- [x] User service para updates de perfil
- [x] Route guard no OnboardingWizard (redireciona se já completado)

---

## 🚧 Fase 5: Integração Frontend ↔ Backend (Em Progresso)

### Dashboard Real
- [ ] Conectar dashboard aos endpoints reais
- [ ] Substituir mock data por chamadas API
- [ ] Carregamento e estados de erro
- [ ] Refresh automático de dados

### Subscription Management
- [ ] Formulário de criação integrado
- [ ] Edição inline de assinaturas
- [ ] Filtros funcionais
- [ ] Paginação server-side

### Route Protection
- [ ] Middleware Next.js para proteção de rotas
- [ ] Redirect para /login se não autenticado
- [ ] Redirect para /onboarding se não completado
- [ ] Redirect para /dashboard se onboarding completo

---

## 📋 Fase 6: Features Avançadas (Planejado)

### Analytics & Insights
- [ ] Gráfico de gastos por categoria (Chart.js ou Recharts)
- [ ] Visualização de tendências mensais
- [ ] Comparação mês a mês
- [ ] Métricas: total gasto, média, maior assinatura

### Subscription Features
- [ ] Upload de logo customizado
- [ ] Suporte a múltiplas moedas (USD, EUR, BRL)
- [ ] Métodos de pagamento
- [ ] Tags customizadas
- [ ] Histórico de pagamentos

### User Experience
- [ ] Dark mode persistente
- [ ] Filtros avançados (múltiplas categorias, range de valor)
- [ ] Busca global
- [ ] Exportação de dados (CSV, PDF)
- [ ] Notificações in-app

---

## 🔮 Fase 7: Performance & Qualidade (Planejado)

### Backend
- [ ] Cache com Redis
- [ ] Rate limiting (Bucket4j)
- [ ] Logs estruturados
- [ ] Metrics com Micrometer
- [ ] Health checks avançados

### Frontend
- [ ] Server-side rendering onde aplicável
- [ ] Image optimization
- [ ] Code splitting
- [ ] Service Worker para offline support
- [ ] Performance monitoring

### Testing
- [ ] Cobertura de testes >80%
- [ ] E2E tests com Playwright
- [ ] Visual regression tests
- [ ] Load testing

---

## 🚀 Fase 8: Deployment & DevOps (Planejado)

### Infrastructure
- [ ] Docker Compose para desenvolvimento
- [ ] Containerização (backend + frontend)
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Deploy em Railway ou Render
- [ ] PostgreSQL gerenciado
- [ ] Variáveis de ambiente seguras

### Monitoring
- [ ] Error tracking (Sentry)
- [ ] Analytics (Google Analytics ou Plausible)
- [ ] Uptime monitoring
- [ ] Backup automático do banco

---

## 🎯 Próximos Passos Imediatos

1. **Testar onboarding end-to-end** em ambiente local
2. **Implementar route guards** para dashboard
3. **Conectar dashboard** aos endpoints reais
4. **Adicionar loading states** em toda a aplicação
5. **Implementar error boundaries** no frontend

---

## 📝 Notas de Arquitetura

### Backend
- **Package-by-feature**: Organização modular por domínio
- **Layered architecture**: Controller → Service → Repository
- **DTO pattern**: Separação entre API e entidades do banco
- **Migration-first**: Flyway para versionamento de schema

### Frontend
- **App Router**: Next.js 14 com convenções modernas
- **Feature-based structure**: Código organizado por feature
- **Compound components**: UI components reutilizáveis
- **Service layer**: Separação de lógica de API

---

## 📊 Métricas de Progresso

| Área                  | Status | Progresso |
|-----------------------|--------|-----------|
| Backend API           | ✅      | 100%      |
| Autenticação          | ✅      | 100%      |
| Email Notifications   | ✅      | 100%      |
| Onboarding            | ✅      | 100%      |
| Frontend Landing      | ✅      | 100%      |
| Dashboard UI          | 🚧     | 70%       |
| Backend Integration   | 🚧     | 40%       |
| Advanced Features     | 📋     | 0%        |
| Testing               | 🚧     | 50%       |
| Deployment            | 📋     | 0%        |

**Legenda:** ✅ Concluído | 🚧 Em Progresso | 📋 Planejado

---

## 🎓 Lições Aprendidas

### Migração Java 25 → 17
- Alguns métodos modernos (`Locale.of()`, `List.getFirst()`) não existem no Java 17
- Sempre verificar compatibilidade de APIs ao downgrade de versão

### Onboarding UX
- Wizard linear reduz decisões e aumenta completude
- Sugestões pré-preenchidas reduzem fricção
- Animações criam sensação de progressão
- Confetti celebra conquistas (gamification)

### Auth Architecture
- Token rotation previne replay attacks
- Refresh token no banco permite invalidação granular
- Silent refresh melhora UX (sem logout forçado)
- AuthContext centraliza lógica e evita prop drilling

---

## 🤝 Contribuindo

Este é um projeto pessoal, mas feedback e sugestões são bem-vindos!

Para propor features ou reportar bugs, abra uma issue no repositório.
