# Plano: Resolver CORS com Next.js Rewrites

## Contexto

O backend roda no Railway com **private networking** (`ongoing.railway.internal:6969`), inacessível ao browser do
usuário. O frontend (Next.js) faz chamadas à API via axios direto do browser (client-side), causando dois problemas:

1. **Em produção (Railway):** O browser não consegue alcançar o backend privado → status code `(null)`
2. **Em dev com URLs externas:** A origin do frontend não está em `CORS_ALLOWED_ORIGINS` → 403

## Solução: Next.js Rewrites (Proxy Server-Side)

O Next.js server atua como proxy: o browser faz requests ao próprio Next.js, que redireciona internamente para o
backend via private networking.

```
Produção (Railway):
[Browser] → https://frontend.railway.app/api/v1/auth/login
                    ↓ (Next.js rewrite, server-side)
           http://ongoing.railway.internal:6969/api/v1/auth/login

Dev local (sem mudança):
[Browser] → http://localhost:6969/api/v1/auth/login (direto, CORS normal)
```

## Mudanças de Código (1 arquivo)

### `frontend/next.config.mjs`

Adicionar `rewrites()` que só é ativado quando `BACKEND_INTERNAL_URL` está definido:

```js
/** @type {import('next').NextConfig} */
const nextConfig = {
        async rewrites() {
            const backendUrl = process.env.BACKEND_INTERNAL_URL;

            if (!backendUrl) return [];

            return [
                {
                    source: "/api/v1/:path*",
                    destination: `${backendUrl}/api/v1/:path*`,
                },
            ];
        },
    };

export default nextConfig;
```

## Configuração de Env Vars

### Serviço frontend no Railway

```env
NEXT_PUBLIC_API_URL=/api/v1
BACKEND_INTERNAL_URL=http://ongoing.railway.internal:6969
```

- `NEXT_PUBLIC_API_URL=/api/v1` — URL relativa, browser chama a mesma origin do Next.js
- `BACKEND_INTERNAL_URL` — Sem `NEXT_PUBLIC_`, só acessível server-side

### Serviço backend no Railway

Nenhuma mudança necessária. CORS não é necessário em produção porque o Next.js faz a request server-side (mesma rede
Railway, sem browser envolvido).

### Dev local

Sem mudança. `NEXT_PUBLIC_API_URL=http://localhost:6969/api/v1` continua funcionando via CORS normal
(`http://localhost:3000` já está na allowlist padrão do backend).

## Por que essa abordagem?

- **Segurança:** Backend continua privado, sem exposição à internet
- **Simplicidade:** 1 arquivo de código + 2 env vars
- **Zero CORS em produção:** Browser fala só com Next.js (mesma origin)
- **Dev local inalterado:** Sem rewrite, axios chama backend direto

## Ordem de Implementação

```
1. Alterar next.config.mjs (adicionar rewrites)
2. Build + lint para validar
3. No Railway: adicionar env vars no serviço frontend
4. Deploy e testar login
```

## Verificação

1. `cd frontend && npm run build` — sem erros
2. No Railway: login deve funcionar sem erros CORS
3. Dev local: `npm run dev` → login em `http://localhost:3000` → deve funcionar como antes
