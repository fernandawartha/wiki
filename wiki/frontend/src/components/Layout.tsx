import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../services/api";
import { SearchResult, TreeNode } from "../types";
import Sidebar from "./Sidebar";
import CreateFolderModal from "./CreateFolderModal";
import CreatePageModal from "./CreatePageModal";

interface NavigationGuard {
  shouldBlock: () => boolean;
  confirm: () => Promise<boolean>;
}

export interface LayoutOutletContext {
  refreshTree: () => void;
  registerNavigationGuard?: (guard: NavigationGuard | null) => void;
}

const statusLabels: Record<SearchResult["status"], string> = {
  DRAFT: "Rascunho",
  PUBLISHED: "Publicado",
  ARCHIVED: "Arquivado"
};

const Layout = () => {
  const { user, token, logout } = useAuth();
  const [tree, setTree] = useState<TreeNode[]>([]);
  const [loadingTree, setLoadingTree] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
  const [searchMessage, setSearchMessage] = useState<string | null>(null);
  const [searchLoading, setSearchLoading] = useState(false);
  const searchAbortRef = useRef<AbortController | null>(null);
  const navigationGuardRef = useRef<NavigationGuard | null>(null);
  const [syncedAt, setSyncedAt] = useState<Date | null>(null);
  const navigate = useNavigate();
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
  const [selectedFolderId, setSelectedFolderId] = useState<string | null>(null);
  const [folderModalOpen, setFolderModalOpen] = useState(false);
  const [pageModalOpen, setPageModalOpen] = useState(false);

  const loadTree = useCallback(() => {
    setLoadingTree(true);
    api
      .get<TreeNode[]>("/folders/tree")
      .then((response) => {
        setTree(response.data);
        setSyncedAt(new Date());
      })
      .finally(() => setLoadingTree(false));
  }, []);

  useEffect(() => {
    loadTree();
  }, [loadTree]);

  useEffect(() => {
    const query = searchTerm.trim();
    if (!query) {
      if (searchAbortRef.current) {
        searchAbortRef.current.abort();
        searchAbortRef.current = null;
      }
      setSearchResults([]);
      setSearchLoading(false);
      setSearchMessage(null);
      return;
    }
    if (query.length < 2) {
      if (searchAbortRef.current) {
        searchAbortRef.current.abort();
        searchAbortRef.current = null;
      }
      setSearchResults([]);
      setSearchLoading(false);
      setSearchMessage("Digite ao menos 2 caracteres para buscar.");
      return;
    }

    const controller = new AbortController();
    searchAbortRef.current = controller;
    setSearchLoading(true);
    setSearchMessage(null);

    api
      .get<SearchResult[]>("/search", {
        params: { q: query },
        signal: controller.signal
      })
      .then((response) => {
        setSearchResults(response.data);
        if (!response.data.length) {
          setSearchMessage("Nenhum conteúdo encontrado.");
        }
      })
      .catch((error) => {
        if ((error as any)?.code === "ERR_CANCELED" || (error as Error).name === "CanceledError") {
          return;
        }
        setSearchMessage("Não foi possível buscar no momento.");
      })
      .finally(() => {
        if (searchAbortRef.current === controller) {
          setSearchLoading(false);
          searchAbortRef.current = null;
        }
      });

    return () => controller.abort();
  }, [searchTerm]);

  const registerNavigationGuard = useCallback((guard: NavigationGuard | null) => {
    navigationGuardRef.current = guard;
  }, []);

  const runWithNavigationGuard = useCallback(
    (next: () => void) => {
      const guard = navigationGuardRef.current;
      if (guard && guard.shouldBlock()) {
        guard
          .confirm()
          .then((proceed) => {
            if (proceed) {
              next();
            }
          })
          .catch(() => undefined);
      } else {
        next();
      }
    },
    []
  );

  const handleSearchChange = (value: string) => {
    setSearchTerm(value);
  };

  const handleSelectPage = (pageId: string) => {
    runWithNavigationGuard(() => {
      setSelectedNodeId(pageId);
      setSearchResults([]);
      setSearchTerm("");
      navigate(`/pages/${pageId}`);
    });
  };

  const handleSelectFolder = (folderId: string | null) => {
    setSelectedNodeId(folderId);
    setSelectedFolderId(folderId);
  };

  const folderOptions = useMemo(() => {
    const options: { id: string; label: string }[] = [];
    const traverse = (nodes: TreeNode[], prefix = "") => {
      nodes.forEach((node) => {
        if (node.type === "FOLDER") {
          const label = prefix ? `${prefix} / ${node.label}` : node.label;
          options.push({ id: node.id, label });
          traverse(node.children, label);
        }
      });
    };
    traverse(tree);
    return options;
  }, [tree]);

  const syncLabel = useMemo(() => {
    if (loadingTree) return "Sincronizando estrutura...";
    if (!syncedAt) return "Estrutura pronta";
    return `Atualizado ${syncedAt.toLocaleString("pt-BR", {
      day: "2-digit",
      month: "short",
      hour: "2-digit",
      minute: "2-digit"
    })}`;
  }, [loadingTree, syncedAt]);

  const heroCopy = token
    ? "Organize e publique playbooks com consistência visual e clareza."
    : "Consuma os conteúdos oficiais e conheça os processos críticos da Handit.";

  const shouldShowSearchPanel = searchLoading || searchResults.length > 0 || !!searchMessage;

  return (
    <div className="layout">
      <header className="header header--light">
        <div className="header__primary">
          <button type="button" className="logo" onClick={() => runWithNavigationGuard(() => navigate("/"))}>
            <div className="logo-mark">H</div>
            <div className="logo-copy">
              <span className="eyebrow">Handit Wiki</span>
              <strong>Documentação viva</strong>
              <p>{heroCopy}</p>
            </div>
          </button>
          <div className="user-card user-card--light">
            <span className="user-card__badge">{token ? "Colaborador" : "Visitante"}</span>
            <strong className="user-card__name">{user?.name ?? "Visitante"}</strong>
            <span className="user-card__email">{user?.email ?? "Autenticação desativada"}</span>
            <p className="user-card__hint">
              {token
                ? "Você está autenticado e pode criar conteúdos."
                : "Modo somente leitura para consultas rápidas."}
            </p>
            {token && (
              <button type="button" className="btn btn-ghost" onClick={logout}>
                Sair
              </button>
            )}
          </div>
        </div>
        <div className="header__secondary">
          <div className="search">
            <div className="search-field search-field--light">
              <span className="search-icon" aria-hidden />
              <input
                value={searchTerm}
                onChange={(event) => handleSearchChange(event.target.value)}
                placeholder="Busque por páginas, squads ou palavras-chave"
              />
            </div>
            {shouldShowSearchPanel && (
              <div className="search-results" role="listbox">
                {searchLoading && <div className="search-feedback">Buscando...</div>}
                {searchMessage && <div className="search-feedback">{searchMessage}</div>}
                {searchResults.map((result) => (
                  <button
                    type="button"
                    className="search-result"
                    key={result.id}
                    onClick={() => handleSelectPage(result.id)}
                  >
                    <div className="search-result__copy">
                      <strong>{result.title}</strong>
                      {result.summary && <p>{result.summary}</p>}
                    </div>
                    <span className={`status-badge status-${result.status.toLowerCase()}`}>
                      {statusLabels[result.status]}
                    </span>
                  </button>
                ))}
              </div>
            )}
          </div>
          <div className="header__actions">
            <button
              type="button"
              className="btn btn-secondary btn-secondary--light"
              onClick={() => setFolderModalOpen(true)}
            >
              Nova pasta
            </button>
            <button type="button" className="btn btn-primary" onClick={() => setPageModalOpen(true)}>
              Nova página
            </button>
          </div>
          <span className="sync-pill sync-pill--light">{syncLabel}</span>
        </div>
      </header>
      <div className="content-area">
        <Sidebar
          nodes={tree}
          loading={loadingTree}
          onSelectPage={handleSelectPage}
          onSelectFolder={handleSelectFolder}
          selectedNodeId={selectedNodeId}
        />
        <main className="main-content">
          <Outlet context={{ refreshTree: loadTree, registerNavigationGuard }} />
        </main>
      </div>
      <CreateFolderModal
        open={folderModalOpen}
        onClose={() => setFolderModalOpen(false)}
        parentOptions={folderOptions}
        defaultParentId={selectedFolderId}
        onCreated={loadTree}
      />
      <CreatePageModal
        open={pageModalOpen}
        onClose={() => setPageModalOpen(false)}
        folderOptions={folderOptions}
        defaultFolderId={selectedFolderId}
        onCreated={loadTree}
      />
    </div>
  );
};

export default Layout;
