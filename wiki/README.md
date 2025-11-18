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
| `BITBUCKET_BASE_URL` | Base da API (`https://api.bitbucket.org/2.0`). |
| `BITBUCKET_WORKSPACE` | Workspace Bitbucket que hospeda o repositório de markdown. |
| `BITBUCKET_REPO` | *slug* do repositório. |
| `BITBUCKET_BRANCH` | Branch onde os arquivos `.md` são persistidos. |
| `BITBUCKET_TOKEN` | Token de acesso (Bearer). |
| `BITBUCKET_LOCAL_PATH` | Pasta usada em modo mock quando o Bitbucket não está configurado (padrão `./bitbucket-local`). |
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

- Use os botões "Nova Pasta" e "Nova Página" no topo da aplicação para criar estruturas hierárquicas diretamente pelo frontend. O editor embutido em Markdown sincroniza automaticamente cada página com o repositório configurado no Bitbucket ou, se as credenciais não estiverem configuradas, com o diretório local definido em `BITBUCKET_LOCAL_PATH`.

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

## Fluxo Bitbucket

1. Cada página possui o campo `bitbucketPath` apontando para o arquivo `.md` no repositório configurado.
2. O `BitbucketService` faz `GET`/`POST` (multipart) na API do Bitbucket para ler/escrever.
3. O histórico de versões (`pageHistory`) registra `commitHash`, autor e notas.

## Permissões

- Tudo é guardado na coleção `permissions` e aplicado com herança de pasta -> página.
- Os controllers validam `VIEW`, `EDIT`, `CREATE` e `DELETE` antes de executar as ações.

## Editor Markdown

- O frontend usa `@uiw/react-md-editor` com upload de imagens via `/api/uploads` (arquivo salvo no Bitbucket e URL retornada).
- Visualização com `MDEditor.Markdown`, garantindo integração por link direto (`/pages/:pageId`).


