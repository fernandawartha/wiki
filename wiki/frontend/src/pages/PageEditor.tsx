import MDEditor from "@uiw/react-md-editor";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useOutletContext, useParams } from "react-router-dom";
import { api } from "../services/api";
import { LayoutOutletContext } from "../components/Layout";
import { PageResponse } from "../types";

const statusLabels: Record<PageResponse["status"], string> = {
  DRAFT: "Rascunho",
  PUBLISHED: "Publicado",
  ARCHIVED: "Arquivado"
};

const parseTags = (value: string) =>
  value
    .split(",")
    .map((tag) => tag.trim())
    .filter(Boolean);

const formatTagsInput = (tags?: string[]) => (tags?.length ? tags.join(", ") : "");

const PageEditor = () => {
  const { pageId } = useParams();
  const navigate = useNavigate();
  const { refreshTree, registerNavigationGuard } = useOutletContext<LayoutOutletContext>();
  const [page, setPage] = useState<PageResponse | null>(null);
  const [content, setContent] = useState<string>("");
  const [title, setTitle] = useState("");
  const [summary, setSummary] = useState("");
  const [tagsInput, setTagsInput] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!pageId) return;
    api.get<PageResponse>(`/pages/${pageId}`).then((response) => {
      const p = response.data;
      setPage(p);
      setTitle(p.title);
      setSummary(p.summary ?? "");
      setContent(p.content);
      setTagsInput(formatTagsInput(p.tags));
    });
  }, [pageId]);

  const normalizedTags = useMemo(() => parseTags(tagsInput), [tagsInput]);

  const hasChanges = useMemo(() => {
    if (!page) return false;
    const initialTags = page.tags ?? [];
    const sameTags = JSON.stringify(initialTags) === JSON.stringify(normalizedTags);
    return (
      title !== page.title ||
      summary !== (page.summary ?? "") ||
      content !== page.content ||
      !sameTags
    );
  }, [content, normalizedTags, page, summary, title]);

  useEffect(() => {
    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      if (!hasChanges) return;
      event.preventDefault();
      event.returnValue = "";
    };
    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => window.removeEventListener("beforeunload", handleBeforeUnload);
  }, [hasChanges]);

  const handleSave = useCallback(
    async (status?: PageResponse["status"], options?: { redirect?: boolean }) => {
      if (!page) return;
      setSaving(true);
      try {
        await api.put<PageResponse>(`/pages/${page.id}`, {
          title,
          summary,
          folderId: page.folderId,
          status: status ?? page.status,
          bitbucketPath: page.bitbucketPath,
          content,
          tags: normalizedTags
        });
        refreshTree();
        if (status === "PUBLISHED") {
          await api.post(`/pages/${page.id}/publish`, { notes: "Publicado via UI" });
        }
        if (options?.redirect ?? true) {
          navigate(`/pages/${page.id}`);
        }
      } finally {
        setSaving(false);
      }
    },
    [content, navigate, normalizedTags, page, refreshTree, summary, title]
  );

  const saveDraftSilently = useCallback(() => handleSave(undefined, { redirect: false }), [handleSave]);

  const confirmNavigation = useCallback(async () => {
    if (!hasChanges) return true;
    const wantsToSave = window.confirm("Você deseja salvar o rascunho antes de sair desta página?");
    if (wantsToSave) {
      await saveDraftSilently();
      return true;
    }
    return window.confirm("Deseja sair sem salvar as alterações?");
  }, [hasChanges, saveDraftSilently]);

  useEffect(() => {
    if (!registerNavigationGuard) return undefined;
    if (!hasChanges) {
      registerNavigationGuard(null);
      return undefined;
    }
    const guard = {
      shouldBlock: () => hasChanges && !saving,
      confirm: () => confirmNavigation()
    };
    registerNavigationGuard(guard);
    return () => registerNavigationGuard(null);
  }, [confirmNavigation, hasChanges, registerNavigationGuard, saving]);

  const handleUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;
    const formData = new FormData();
    formData.append("file", file);
    const response = await api.post<{ url: string }>("/uploads", formData, {
      headers: { "Content-Type": "multipart/form-data" }
    });
    setContent((prev) => `${prev}\n![${file.name}](${response.data.url})`);
  };

  if (!page) {
    return <p className="muted">Carregando editor...</p>;
  }

  return (
    <div className="page-editor" data-color-mode="light">
      <section className="page-editor__banner">
        <div>
          <p className="eyebrow">Editor</p>
          <h1>{page.title}</h1>
          <p className="page-summary">
            {summary || "Adicione um resumo para facilitar a busca interna."}
          </p>
        </div>
        <div className="page-editor__status">
          <span className={`status-badge status-${page.status.toLowerCase()}`}>
            {statusLabels[page.status]}
          </span>
          <small>Status atual</small>
        </div>
      </section>
      <form
        className="page-editor__form"
        onSubmit={(event: FormEvent) => {
          event.preventDefault();
          handleSave();
        }}
      >
        <div className="page-editor__controls">
          <label>
            <span>Título</span>
            <input value={title} onChange={(event) => setTitle(event.target.value)} required />
          </label>
          <label>
            <span>Resumo</span>
            <input value={summary} onChange={(event) => setSummary(event.target.value)} />
          </label>
          <label>
            <span>Tags</span>
            <input
              value={tagsInput}
              onChange={(event) => setTagsInput(event.target.value)}
              placeholder="ex.: onboarding, suporte, financeiro"
            />
          </label>
          <label className="upload-button">
            <span>Upload de imagem</span>
            <input type="file" hidden onChange={handleUpload} />
          </label>
        </div>
        <div className="page-editor__editor">
          <span className="eyebrow">Conteúdo Markdown</span>
          <MDEditor value={content} onChange={(value) => setContent(value ?? "")} height={600} />
        </div>
        <div className="page-editor__actions">
          <button type="submit" className="btn btn-secondary" disabled={saving}>
            Salvar rascunho
          </button>
          <button
            type="button"
            className="btn btn-primary"
            disabled={saving}
            onClick={() => handleSave("PUBLISHED")}
          >
            Publicar
          </button>
        </div>
      </form>
    </div>
  );
};

export default PageEditor;
