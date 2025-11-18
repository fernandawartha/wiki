export type TreeNodeType = "FOLDER" | "PAGE";

export interface TreeNode {
  id: string;
  label: string;
  type: TreeNodeType;
  parentId?: string | null;
  children: TreeNode[];
}

export interface User {
  id: string;
  username: string;
  name: string;
  email: string;
  groups: string[];
}

export interface PageResponse {
  id: string;
  title: string;
  folderId: string;
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED";
  summary?: string;
  tags?: string[];
  bitbucketPath: string;
  content: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface SearchResult {
  id: string;
  title: string;
  summary?: string;
  folderId: string;
  status: PageResponse["status"];
}
