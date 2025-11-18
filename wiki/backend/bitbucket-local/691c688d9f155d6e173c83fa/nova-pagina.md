# Novo conteúdo
# Contexto 18/11 – Handit-Wiki

## Visão Geral
- Repositório estruturado em `backend/` (Spring Boot + MongoDB) e `frontend/` (React + Vite).
- Dockerfile e `docker-compose.yml` permitem subir MongoDB, backend e frontend.
- `README.md` documenta requisitos, variáveis de ambiente (Bitbucket, JWT, super admin, etc.) e instruções de execução.
- `run-services.sh` cuida de encerrar processos antigos e reiniciar backend (porta 5174) e frontend (porta 5173) automaticamente.

## Backend
- Java 17 + Spring Boot 3.2.5.
- Segurança controlada por `SecurityConfig`. Atualmente todas as rotas estão liberadas enquanto o login fica desativado.
- `UserService` retorna automaticamente o super admin (configurado em `security.super-admins`) quando não há usuário autenticado.
- `BitbucketService` opera em modo mock caso as credenciais não estejam configuradas, gravando arquivos Markdown em `backend/bitbucket-local/` (UTF-8 forçado para leitura/escrita).
- Modelos principais (`users`, `folders`, `pages`, `permissions`, histórico). Pastas e páginas ficam no MongoDB (banco `handitwiki`).
- `PageService` usa `findPageOrThrow` para retornar 404 quando a página não existe, evitando erros 500.
- Permissões: o criador herda todos os níveis (respeitando super admin) e o frontend filtra o que o usuário pode ver.

## Frontend
- React + Vite + TypeScript.
- Layout com sidebar, busca global, botões “Nova Pasta” e “Nova Página” (modais para criar estruturas hierárquicas e editar Markdown via `@uiw/react-md-editor`). Uploads vão para `/api/uploads`.
- Página principal (`Layout`) mostra o nome do usuário quando existe token e “Visitante” caso contrário; o botão “Sair” só aparece quando há sessão.
- Rotas protegidas foram temporariamente removidas: o app carrega diretamente o layout e as rotas de visualização/edição (`/pages/:pageId`, `/pages/:pageId/edit`). O login não é necessário por enquanto.
- Proxy do Vite aponta `/api` para `http://localhost:5174`.

## Fluxo de Criação/Visualização
1. Criar pasta/página via botões no topo da sidebar → registro salvo no MongoDB e conteúdo Markdown gravado em `backend/bitbucket-local/...` (ou Bitbucket real se configurado).
2. Permissões são automaticamente concedidas ao criador; super admins têm acesso total.
3. Ao clicar na página, o frontend chama `GET /api/pages/{id}`; o backend busca o documento, lê o arquivo Markdown (UTF-8) e retorna conteúdo renderizado corretamente.

## Observações Importantes
- Tirar o login do fluxo foi uma decisão temporária para reduzir bloqueios; basta reativar o guard no frontend e restaurar as regras em `SecurityConfig` quando quiser exigir autenticação novamente.
- Caso configure o Bitbucket real, defina `BITBUCKET_BASE_URL`, `BITBUCKET_WORKSPACE`, `BITBUCKET_REPO`, `BITBUCKET_BRANCH`, `BITBUCKET_TOKEN` e reinicie o backend – o conteúdo passará a ser versionado no repositório remoto.
- Scripts úteis:
  - `run-services.sh` (no diretório `wiki/`) – inicia backend e frontend, liberando portas antes.
  - `docker-compose up --build` – sobe MongoDB, backend e frontend em contêineres.



