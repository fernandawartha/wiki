import MDEditor from "@uiw/react-md-editor";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../services/api";
import { PageResponse } from "../types";

const statusLabels: Record<PageResponse["status"], string> = {
  DRAFT: "Rascunho",
  PUBLISHED: "Publicado",
  ARCHIVED: "Arquivado"
};

const formatDate = (value?: string) => {
  if (!value) return null;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return null;
  return date.toLocaleString("pt-BR", {
    day: "2-digit",
    month: "long",
    hour: "2-digit",
    minute: "2-digit"
  });
};

const PageView = () => {
  const { pageId } = useParams();
  const [page, setPage] = useState<PageResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    if (!pageId) return;
    setLoading(true);
    api
      .get<PageResponse>(`/pages/${pageId}`)
      .then((response) => setPage(response.data))
      .finally(() => setLoading(false));
  }, [pageId]);

  const metadata = useMemo(() => {
    if (!page) return { updated: null, created: null };
    return {
      updated: formatDate(page.updatedAt ?? page.createdAt),
      created: formatDate(page.createdAt)
    };
  }, [page]);

  if (loading) {
    return <p className="muted">Carregando página...</p>;
  }

  if (!page) {
    return <p className="muted">Página não encontrada</p>;
  }

  return (
    <div className="page-view">
      <header className="page-view__header">
        <div>
          <p className="eyebrow">Página</p>
          <h1>{page.title}</h1>
          <p className="page-summary">{page.summary || "Este conteúdo ainda não possui um resumo."}</p>
          {page.tags?.length ? (
            <div className="page-tags">
              {page.tags.map((tag, index) => (
                <span key={`${tag}-${index}`} className="tag-pill">
                  {tag}
                </span>
              ))}
            </div>
          ) : null}
          <div className="page-meta">
            <span className={`status-badge status-${page.status.toLowerCase()}`}>
              {statusLabels[page.status]}
            </span>
            {metadata.updated && <span className="meta-item">Atualizado {metadata.updated}</span>}
            {metadata.created && <span className="meta-item">Criado {metadata.created}</span>}
          </div>
        </div>
        <div className="page-view__header-actions">
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => navigate(`/pages/${page.id}/edit`)}
          >
            Editar página
          </button>
        </div>
      </header>
      <section className="page-view__content" data-color-mode="light">
        <MDEditor.Markdown source={page.content} />
      </section>
    </div>
  );
};

export default PageView;
