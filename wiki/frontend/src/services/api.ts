import axios from "axios";

export const api = axios.create({
  baseURL: "/api"
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("handitWikiToken");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export const setAuthToken = (token?: string | null) => {
  if (token) {
    api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    localStorage.setItem("handitWikiToken", token);
  } else {
    delete api.defaults.headers.common["Authorization"];
    localStorage.removeItem("handitWikiToken");
  }
};
