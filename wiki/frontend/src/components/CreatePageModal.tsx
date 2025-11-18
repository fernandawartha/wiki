import MDEditor from "@uiw/react-md-editor";
import { FormEvent, useEffect, useMemo, useState } from "react";
import Modal from "./Modal";
import { api } from "../services/api";
import { useNavigate } from "react-router-dom";
import { PageResponse } from "../types";

interface Option {
  id: string;
  label: string;
}

interface CreatePageModalProps {
  open: boolean;
  onClose: () => void;
  folderOptions: Option[];
  defaultFolderId?: string | null;
  onCreated: () => void;
}

const slugify = (value: string) =>
  value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-|-$)+/g, "");

const parseTags = (value: string) =>
  value
    .split(",")
    .map((tag) => tag.trim())
    .filter(Boolean);

const CreatePageModal = ({
  open,
  onClose,
  folderOptions,
  defaultFolderId,
  onCreated
}: CreatePageModalProps) => {
  const navigate = useNavigate();
  const [title, setTitle] = useState("");
  const [summary, setSummary] = useState("");
  const [tagsInput, setTagsInput] = useState("");
  const [folderId, setFolderId] = useState(defaultFolderId ?? "");
  const [content, setContent] = useState<string>("# Novo conteúdo\n");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (open) {
      setFolderId(defaultFolderId ?? "");
    }
  }, [defaultFolderId, open]);

  const pathSuggestion = useMemo(() => {
    const slug = slugify(title || "nova-pagina");
    const prefix = folderId ? `${folderId}/` : "";
    return `${prefix}${slug}.md`;
  }, [title, folderId]);

  const resetAndClose = () => {
    setTitle("");
    setSummary("");
    setTagsInput("");
    setFolderId(defaultFolderId ?? "");
    setContent("# Novo conteúdo\n");
    onClose();
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      const response = await api.post<PageResponse>("/pages", {
        title,
        summary,
        folderId: folderId || null,
        content,
        bitbucketPath: pathSuggestion,
        status: "DRAFT",
        tags: parseTags(tagsInput)
      });
      onCreated();
      resetAndClose();
      navigate(`/pages/${response.data.id}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal open={open} title="Nova página" onClose={resetAndClose}>
      <form className="form-grid" onSubmit={handleSubmit} data-color-mode="light">
        <label>
          Título
          <input value={title} onChange={(e) => setTitle(e.target.value)} required />
        </label>
        <label>
          Resumo
          <input value={summary} onChange={(e) => setSummary(e.target.value)} />
        </label>
        <label>
          Tags
          <input
            value={tagsInput}
            onChange={(e) => setTagsInput(e.target.value)}
            placeholder="separe com vírgulas (ex.: onboarding, financeiro, produto)"
          />
          <small className="muted">
            Use palavras-chave para facilitar a busca e correlacionar conteúdos.
          </small>
        </label>
        <label>
          Pasta
          <select value={folderId ?? ""} onChange={(e) => setFolderId(e.target.value)}>
            <option value="">(Raiz)</option>
            {folderOptions.map((option) => (
              <option key={option.id} value={option.id}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
        <div className="md-editor">
          <span>Conteúdo Markdown</span>
          <MDEditor value={content} onChange={(value) => setContent(value ?? "")} height={300} />
        </div>
        <div className="modal-actions">
          <button type="button" className="ghost" onClick={resetAndClose} disabled={loading}>
            Cancelar
          </button>
          <button type="submit" disabled={loading || !title.trim()}>
            {loading ? "Criando..." : "Criar página"}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default CreatePageModal;
