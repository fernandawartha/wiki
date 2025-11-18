#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_PORT="${BACKEND_PORT:-5174}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"

kill_port() {
  local port="$1"
  local pids=""

  if command -v lsof >/dev/null 2>&1; then
    pids="$(lsof -t -i tcp:"$port" -s tcp:listen 2>/dev/null || true)"
  elif command -v fuser >/dev/null 2>&1; then
    pids="$(fuser "$port"/tcp 2>/dev/null || true)"
  fi

  if [[ -n "${pids// /}" ]]; then
    echo "Encerrando processos na porta $port: $pids"
    kill $pids 2>/dev/null || true
  else
    echo "Nenhum processo encontrado para porta $port ou utilitário (lsof/fuser) indisponível."
  fi
}

start_backend() {
  echo "[backend] liberando porta $BACKEND_PORT e iniciando Spring Boot..."
  kill_port "$BACKEND_PORT"
  pushd "$ROOT_DIR/backend" >/dev/null
  mvn spring-boot:run
  popd >/dev/null
}

start_frontend() {
  echo "[frontend] liberando porta $FRONTEND_PORT e iniciando Vite..."
  kill_port "$FRONTEND_PORT"
  pushd "$ROOT_DIR/frontend" >/dev/null
  npm install
  npm run dev
  popd >/dev/null
}

start_backend &
BACKEND_PID=$!

start_frontend &
FRONTEND_PID=$!

trap 'echo; echo "Encerrando serviços..."; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null || true' INT TERM
wait $BACKEND_PID
wait $FRONTEND_PID
