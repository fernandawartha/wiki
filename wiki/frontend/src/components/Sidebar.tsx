import { useMemo } from "react";
import { TreeNode } from "../types";

interface SidebarProps {
  nodes: TreeNode[];
  loading: boolean;
  selectedNodeId?: string | null;
  onSelectPage: (pageId: string) => void;
  onSelectFolder: (folderId: string | null) => void;
}

const Sidebar: React.FC<SidebarProps> = ({
  nodes,
  loading,
  selectedNodeId,
  onSelectPage,
  onSelectFolder
}) => {
  const flatTree = useMemo(() => {
    const result: Array<TreeNode & { depth: number }> = [];
    const walk = (items: TreeNode[], depth: number) => {
      items.forEach((item) => {
        result.push({ ...item, depth });
        if (item.children.length) {
          walk(item.children, depth + 1);
        }
      });
    };
    walk(nodes, 0);
    return result;
  }, [nodes]);

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <p className="eyebrow">Navegação</p>
        <strong>Conteúdo estruturado</strong>
      </div>
      {loading ? (
        <p className="muted">Carregando estrutura...</p>
      ) : (
        <ul className="tree-list">
          {flatTree.map((node) => {
            const isSelected = selectedNodeId === node.id;
            const handleClick =
              node.type === "PAGE"
                ? () => onSelectPage(node.id)
                : () => onSelectFolder(node.id);
            return (
              <li
                key={node.id}
                className={`node node-${node.type.toLowerCase()} ${
                  isSelected ? "node-selected" : ""
                }`}
                style={{ paddingLeft: `${node.depth * 16}px` }}
              >
                <button
                  type="button"
                  className={`node-button node-button--${node.type.toLowerCase()}`}
                  onClick={handleClick}
                >
                  <span className={`node-dot node-dot--${node.type.toLowerCase()}`} aria-hidden />
                  <span className="node-text">{node.label}</span>
                </button>
              </li>
            );
          })}
        </ul>
      )}
    </aside>
  );
};

export default Sidebar;
