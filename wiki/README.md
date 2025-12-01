# Handit-Wiki

Handit-Wiki é uma plataforma interna de documentação com backend em Spring Boot/MongoDB e frontend em React + Markdown.

## Estrutura de pastas

```
backend/               # API Spring Boot + MongoDB + integração Bitbucket
frontend/              # App React + TypeScript
Dockerfile(s)          # Contêineres backend/frontend
docker-compose.yml     # Subida completa (Mongo, API, Web)
```

## Requisitos

- Java 17+
- Maven 3.9+
- Node.js 20+
- Docker / Docker Compose (opcional, para orquestração completa)

## Variáveis de ambiente importantes

| Nome | Descrição |
| --- | --- |
| `MONGODB_URI` | URI para o MongoDB. usado pelo backend. |
| `GIT_PROVIDER` | `bitbucket` (padrão) ou `github`. Define qual API será usada para salvar os arquivos `.md`. |
| `BITBUCKET_BASE_URL` | Base da API REST. Use `https://api.bitbucket.org/2.0` (Bitbucket) ou `https://api.github.com` (GitHub). |
| `BITBUCKET_WORKSPACE` | Workspace (Bitbucket) ou owner/organização (GitHub) que hospeda o repositório. |
| `BITBUCKET_REPO` | *slug* do repositório. |
| `BITBUCKET_BRANCH` | Branch onde os arquivos `.md` são persistidos. |
| `BITBUCKET_TOKEN` | Token de acesso (Bearer). Personal access token do Bitbucket ou GitHub. |
| `BITBUCKET_LOCAL_PATH` | Pasta usada em modo mock quando o Bitbucket não está configurado (padrão `./bitbucket-local`). |
| `REPOSITORY_RAW_BASE_URL` | Base opcional para gerar o link público dos uploads. Deixe em branco para usar `https://bitbucket.org` ou `https://raw.githubusercontent.com` automaticamente. |
| `GIT_COMMITTER_NAME` | Nome utilizado nos commits automatizados (padrão `Handit Wiki`). |
| `GIT_COMMITTER_EMAIL` | E-mail utilizado nos commits automatizados (padrão `handit-wiki@local`). |
| `JWT_SECRET` | Segredo JWT. |
| `SUPER_ADMIN_ID` | Usuário/ID com privilégios totais (padrão `fernanda.wartha`). |

## Como rodar localmente

### Backend

```bash
cd backend
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:5174`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

App React em `http://localhost:5173` (proxy para `/api`).

- Use os botões "Nova Pasta" e "Nova Página" no topo da aplicação para criar estruturas hierárquicas diretamente pelo frontend. O editor embutido em Markdown sincroniza automaticamente cada página com o repositório Git configurado (Bitbucket ou GitHub) ou, se as credenciais não estiverem configuradas, com o diretório local definido em `BITBUCKET_LOCAL_PATH`.

### Docker Compose

```bash
docker compose up --build
```

- MongoDB: `mongodb://localhost:27017/handitwiki`
- Backend: `http://localhost:5174`
- Frontend: `http://localhost:5173`

## Testes

```bash
cd backend
mvn test
```

## Fluxo Git (Bitbucket ou GitHub)

1. Cada página possui o campo `bitbucketPath` apontando para o arquivo `.md` no repositório configurado.
2. O `BitbucketService` faz `GET`/`POST` na API do provedor configurado:
   - Bitbucket: requisições `multipart/form-data` para `/repositories/{workspace}/{repo}/src`.
   - GitHub: requisições `PUT` para `/repos/{owner}/{repo}/contents/{path}` usando conteúdo em Base64.
3. O histórico de versões (`pageHistory`) registra `commitHash`, autor e notas.

### Como usar GitHub em desenvolvimento

1. Crie (ou escolha) um repositório GitHub que receberá os arquivos `.md`.
2. Gere um [personal access token](https://github.com/settings/tokens) com permissões de `repo` (ou use um token fine-grained equivalente).
3. Exporte as variáveis antes de subir o backend (exemplo no macOS/Linux):
   ```bash
   export GIT_PROVIDER=github
   export BITBUCKET_BASE_URL=https://api.github.com
   export BITBUCKET_WORKSPACE=seu-usuario-ou-org
   export BITBUCKET_REPO=nome-do-repo
   export BITBUCKET_BRANCH=main
   export BITBUCKET_TOKEN=ghp_xxx # token
   export REPOSITORY_RAW_BASE_URL=https://raw.githubusercontent.com
   export GIT_COMMITTER_NAME="Handit Wiki"
   export GIT_COMMITTER_EMAIL=wiki@seusite.com
   ```
4. Inicie o backend normalmente (`mvn spring-boot:run`). A aplicação continuará gravando a estrutura no MongoDB, mas o conteúdo Markdown irá direto para o GitHub, simulando o comportamento futuro no Bitbucket.

## Permissões

- Tudo é guardado na coleção `permissions` e aplicado com herança de pasta -> página.
- Os controllers validam `VIEW`, `EDIT`, `CREATE` e `DELETE` antes de executar as ações.

## Editor Markdown

- O frontend usa `@uiw/react-md-editor` com upload de imagens via `/api/uploads` (arquivo salvo no Bitbucket e URL retornada).
- Visualização com `MDEditor.Markdown`, garantindo integração por link direto (`/pages/:pageId`).


