import { createContext, useContext, useEffect, useState } from "react";
import { api, setAuthToken } from "../services/api";
import { User } from "../types";

interface AuthContextValue {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children
}) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(
    localStorage.getItem("handitWikiToken")
  );
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      setAuthToken(token);
      api.get<User>("/users/me")
        .then((response) => setUser(response.data))
        .catch(() => setAuthToken(null))
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [token]);

  const login = async (username: string, password: string) => {
    const response = await api.post<{ token: string; user: User }>(
      "/auth/login",
      { username, password }
    );
    setAuthToken(response.data.token);
    setToken(response.data.token);
    setUser(response.data.user);
  };

  const logout = () => {
    setAuthToken(null);
    setUser(null);
    setToken(null);
  };

  const value: AuthContextValue = {
    user,
    token,
    loading,
    login,
    logout
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
};
