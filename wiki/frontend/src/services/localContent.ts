import { PageResponse, SearchResult, TreeNode } from "../types";

const localTree: TreeNode[] = [
  {
    id: "folder-inicio",
    label: "Início",
    type: "FOLDER",
    parentId: null,
    children: [
      {
        id: "home",
        label: "Boas-vindas",
        type: "PAGE",
        parentId: "folder-inicio",
        children: []
      }
    ]
  },
  {
    id: "folder-operacoes",
    label: "Operações",
    type: "FOLDER",
    parentId: null,
    children: [
      {
        id: "incident-response",
        label: "Resposta a incidentes",
        type: "PAGE",
        parentId: "folder-operacoes",
        children: []
      },
      {
        id: "postmortem-template",
        label: "Template de post-mortem",
        type: "PAGE",
        parentId: "folder-operacoes",
        children: []
      }
    ]
  },
  {
    id: "folder-produto",
    label: "Produto",
    type: "FOLDER",
    parentId: null,
    children: [
      {
        id: "design-system",
        label: "Design system",
        type: "PAGE",
        parentId: "folder-produto",
        children: []
      }
    ]
  }
];

const localPages: Record<string, PageResponse> = {
  home: {
    id: "home",
    title: "Visão geral da wiki",
    folderId: "folder-inicio",
    status: "PUBLISHED",
    summary: "Entenda como navegar, colaborar e manter os playbooks atualizados.",
    tags: ["onboarding", "handbook"],
    bitbucketPath: "local/content/home.md",
    content: `# Bem-vindo à Handit Wiki

Esta é uma cópia local apenas para desenvolvimento. Use este ambiente para validar o layout, busca e leitura dos conteúdos antes de conectar no backend oficial.

## Como está organizado?

- **Início**: visão geral da plataforma e boas práticas de contribuição.
- **Operações**: procedimentos de monitoramento, resposta a incidentes e comunicados.
- **Produto**: referências de design system, guidelines e trilhas de descoberta.

## Mantendo o conteúdo atualizado

1. Centralize as decisões e playbooks neste repositório.
2. Revise periodicamente os processos críticos.
3. Publique os aprendizados relevantes para o restante da equipe.
`
  },
  "incident-response": {
    id: "incident-response",
    title: "Plano de resposta a incidentes",
    folderId: "folder-operacoes",
    status: "PUBLISHED",
    summary: "Passo a passo para classificar, comunicar e resolver incidentes em produção.",
    tags: ["SRE", "operacoes"],
    bitbucketPath: "local/content/incident-response.md",
    content: `# Resposta a incidentes

1. **Detecte** o incidente (alerta automático ou reporte manual).
2. **Classifique** o impacto nas áreas de cliente, receita e reputação.
3. **Acione** o canal #incident-room e atribua um comandante.
4. **Comunique** o status a cada 30 minutos até a normalização.
5. **Documente** o pós-incidente usando o template padrão.

> Nunca trabalhe sozinho. Escale cedo e registre cada decisão.
`
  },
  "postmortem-template": {
    id: "postmortem-template",
    title: "Template de post-mortem",
    folderId: "folder-operacoes",
    status: "DRAFT",
    summary: "Formato recomendado para consolidar aprendizados depois de um incidente.",
    tags: ["incidentes", "qualidade"],
    bitbucketPath: "local/content/postmortem-template.md",
    content: `# Template de post-mortem

## Contexto
- Data e horário
- Serviços afetados
- Clientes impactados

## Linha do tempo
- 12:03 - Alerta inicial
- 12:10 - Investigação inicia
- 12:45 - Mitigação aplicada

## Aprendizados
- O alerta precisa de runbook atualizado.
- Automação de rollback priorizada para Q1.
`
  },
  "design-system": {
    id: "design-system",
    title: "Guia rápido do design system",
    folderId: "folder-produto",
    status: "PUBLISHED",
    summary: "Componentes, tokens e diretrizes para manter a consistência visual.",
    tags: ["design", "ux"],
    bitbucketPath: "local/content/design-system.md",
    content: `# Design system Handit

### Tokens principais
- **cores**: brand-green, brand-blue, graphite.
- **tipografia**: Mulish 400/500/700.
- **espaçamento**: escala de 4px.

### Componentes obrigatórios
- Botões primário/secundário/ghost.
- Cards com sombras suaves e bordas de 16px.
- Campos de busca com feedback instantâneo.

Sempre priorize acessibilidade (contraste AA) e reutilize componentes expostos no Storybook interno.
`
  }
};

const normalizeText = (value: string) =>
  value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();

const makeSearchBlob = (page: PageResponse) =>
  normalizeText(
    [
      page.title,
      page.summary ?? "",
      page.content,
      Array.isArray(page.tags) ? page.tags.join(" ") : ""
    ].join(" ")
  );

const localDataFlag = import.meta.env.VITE_USE_LOCAL_DATA;
export const isLocalContentEnabled =
  localDataFlag === "true" || (localDataFlag === undefined && import.meta.env.DEV);

export const getLocalTree = async (): Promise<TreeNode[]> => JSON.parse(JSON.stringify(localTree));

export const getLocalPage = async (pageId: string): Promise<PageResponse> => {
  const page = localPages[pageId];
  if (!page) {
    throw new Error(`LOCAL_PAGE_NOT_FOUND:${pageId}`);
  }
  return JSON.parse(JSON.stringify(page));
};

export const searchLocalPages = async (query: string): Promise<SearchResult[]> => {
  const normalizedQuery = normalizeText(query.trim());
  if (!normalizedQuery) {
    return [];
  }
  return Object.values(localPages)
    .filter((page) => makeSearchBlob(page).includes(normalizedQuery))
    .map((page) => ({
      id: page.id,
      title: page.title,
      summary: page.summary,
      folderId: page.folderId,
      status: page.status
    }));
};
