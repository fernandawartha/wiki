import MDEditor from "@uiw/react-md-editor";
import { FormEvent, useEffect, useState } from "react";
import { useNavigate, useOutletContext, useParams } from "react-router-dom";
import { api } from "../services/api";
import { LayoutOutletContext } from "../components/Layout";
import { PageResponse } from "../types";

const statusLabels: Record<PageResponse["status"], string> = {
  DRAFT: "Rascunho",
  PUBLISHED: "Publicado",
  ARCHIVED: "Arquivado"
};

const PageEditor = () => {
  const { pageId } = useParams();
  const navigate = useNavigate();
  const { refreshTree } = useOutletContext<LayoutOutletContext>();
  const [page, setPage] = useState<PageResponse | null>(null);
  const [content, setContent] = useState<string>("");
  const [title, setTitle] = useState("");
  const [summary, setSummary] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!pageId) return;
    api.get<PageResponse>(`/pages/${pageId}`).then((response) => {
      const p = response.data;
      setPage(p);
      setTitle(p.title);
      setSummary(p.summary ?? "");
      setContent(p.content);
    });
  }, [pageId]);

  const handleSave = async (status?: PageResponse["status"]) => {
    if (!page) return;
    setSaving(true);
    try {
      await api.put<PageResponse>(`/pages/${page.id}`, {
        title,
        summary,
        folderId: page.folderId,
        status: status ?? page.status,
        bitbucketPath: page.bitbucketPath,
        content
      });
      refreshTree();
      if (status === "PUBLISHED") {
        await api.post(`/pages/${page.id}/publish`, { notes: "Publicado via UI" });
      }
      navigate(`/pages/${page.id}`);
    } finally {
      setSaving(false);
    }
  };

  const handleUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;
    const formData = new FormData();
    formData.append("file", file);
    const response = await api.post<{ url: string }>("/uploads", formData, {
      headers: { "Content-Type": "multipart/form-data" }
    });
    setContent((prev) => `${prev}
![${file.name}](${response.data.url})`);
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
            <span>T\u00edtulo</span>
            <input value={title} onChange={(event) => setTitle(event.target.value)} required />
          </label>
          <label>
            <span>Resumo</span>
            <input value={summary} onChange={(event) => setSummary(event.target.value)} />
          </label>
          <label className="upload-button">
            <span>Upload de imagem</span>
            <input type="file" hidden onChange={handleUpload} />
          </label>
        </div>
        <div className="page-editor__editor">
          <span className="eyebrow">Conte\u00fado Markdown</span>
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
