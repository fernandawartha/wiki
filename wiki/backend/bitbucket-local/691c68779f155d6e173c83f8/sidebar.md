# Novo conteúdo
# História – Melhorias na Sidebar Customizável (Drag and Drop)

**Tipo:** Story  
**Título:** Melhorias de usabilidade e comportamento na Sidebar Customizável  

---

## 📌 Descrição
Com base no feedback do consultor/PO, foram identificados diversos pontos de melhoria na Sidebar Customizável. O objetivo desta história é corrigir problemas de usabilidade, permitir ações que hoje não estão disponíveis e aprimorar a apresentação visual e o comportamento padrão esperado pelo usuário.



---

## 🎯 Objetivo
Implementar melhorias funcionais e visuais na Sidebar Customizável para garantir melhor experiência do usuário, maior clareza na navegação e adequação ao comportamento esperado de menus e itens clicáveis.

---

## ✅ Critérios de Aceite

1. Deve ser possível definir um texto exclusivo para o menu no momento de cadastro de uma visão.
2. O usuário deve conseguir excluir menus customizados de forma visível e intuitiva.
3. Os espaçamentos entre itens e grupos devem ser ajustados para evitar aparência visual “encavalada”.
4. Itens do menu devem permitir abrir em nova aba usando:
   - CTRL + clique  
   - Botão direito → Abrir em nova guia  
5. A barra de rolagem não deve sobrepor ícones e deve se alinhar corretamente ao container.
6. O separador entre menus customizados e menus padrão deve ser mais visível e intuitivo.
7. O ícone do menu “Fluxos” deve ser trocado por um ícone mais adequado ao conceito de fluxo.
8. Menus devem ser ocultados automaticamente quando o usuário não possuir acesso a nenhum item interno.

---

## 🧩 Subtarefas

### **1. Texto exclusivo para o menu no cadastro de Visão**
- Adicionar campo **“Texto do Menu”** no cadastro/edição de visão.
- Exibir no menu o texto definido nesse campo (se preenchido).
- Manter fallback para nome da visão caso o campo não seja preenchido.

---

### **2. Permitir excluir menus customizados**
- Criar ação clara de exclusão via botão direito ou ícone.
- Atualizar estrutura da sidebar após remoção.
- Garantir que a remoção seja salva corretamente no backend.

---

### **3. Ajustes de espaçamento visual**
- Revisar padding/margens entre itens e grupos.
- Aplicar espaçamento consistente para melhorar legibilidade.

---

### **4. Habilitar abertura em nova guia**
- Ajustar comportamento dos links dos itens da sidebar.
- Suportar:
  - CTRL + clique  
  - Botão direito → Abrir em nova guia

---

### **5. Ajustar comportamento da barra de rolagem**
- Garantir que a scrollbar não sobreponha os ícones.
- Ajustar container ou estilização conforme necessário.

---

### **6. Destacar separador entre grupos de menus**
- Melhorar contraste e estilo do separador.
- Avaliar implementação de separador clicável para expandir/recolher grupos.

---

### **7. Atualizar ícone do menu “Fluxos”**
- Selecionar novo ícone mais representativo.
- Substituir no layout e no cadastro padrão.
- Validar visualmente com o PO.

---

### **8. Ocultar menus sem itens acessíveis**
- Implementar regra de visibilidade:
  - Se o usuário não tem acesso a nenhum item do menu, o menu não deve ser exibido.
- Garantir tratamento correto para menus com itens parcialmente permitidos.

---

## 📎 Status
Pronto para Desenvolvimento


