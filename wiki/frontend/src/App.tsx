import { Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Layout from "./components/Layout";
import PageView from "./pages/PageView";
import PageEditor from "./pages/PageEditor";

const App = () => (
  <AuthProvider>
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Navigate to="/pages/home" replace />} />
        <Route path="/pages/:pageId" element={<PageView />} />
        <Route path="/pages/:pageId/edit" element={<PageEditor />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  </AuthProvider>
);

export default App;
