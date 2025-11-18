import { FormEvent, useEffect, useState } from "react";
import Modal from "./Modal";
import { api } from "../services/api";

interface Option {
  id: string;
  label: string;
}

interface CreateFolderModalProps {
  open: boolean;
  onClose: () => void;
  parentOptions: Option[];
  defaultParentId?: string | null;
  onCreated: () => void;
}

const CreateFolderModal = ({
  open,
  onClose,
  parentOptions,
  defaultParentId,
  onCreated
}: CreateFolderModalProps) => {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [parentId, setParentId] = useState(defaultParentId ?? "");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (open) {
      setParentId(defaultParentId ?? "");
    }
  }, [defaultParentId, open]);

  const resetAndClose = () => {
    setName("");
    setDescription("");
    setParentId(defaultParentId ?? "");
    onClose();
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      await api.post("/folders", {
        name,
        description,
        parentFolderId: parentId || null
      });
      onCreated();
      resetAndClose();
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal open={open} title="Nova pasta" onClose={resetAndClose}>
      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          Nome
          <input value={name} onChange={(e) => setName(e.target.value)} required />
        </label>
        <label>
          Descrição
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} />
        </label>
        <label>
          Pasta pai
          <select value={parentId ?? ""} onChange={(e) => setParentId(e.target.value)}>
            <option value="">(Raiz)</option>
            {parentOptions.map((option) => (
              <option value={option.id} key={option.id}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
        <div className="modal-actions">
          <button type="button" className="ghost" onClick={resetAndClose} disabled={loading}>
            Cancelar
          </button>
          <button type="submit" disabled={loading || !name.trim()}>
            {loading ? "Criando..." : "Criar pasta"}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default CreateFolderModal;
