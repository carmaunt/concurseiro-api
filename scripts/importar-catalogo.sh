#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-https://concurseiro-api-lnae.onrender.com}"
ADMIN_TOKEN="${ADMIN_TOKEN:-}"
ARQUIVO_CATALOGO="${1:-}"

if [[ -z "$ADMIN_TOKEN" ]]; then
  echo "Erro: informe o token de admin na variável ADMIN_TOKEN."
  echo "Exemplo: ADMIN_TOKEN='seu_token' bash scripts/importar-catalogo.sh exemplos/catalogo-portugues.json"
  exit 1
fi

if [[ -z "$ARQUIVO_CATALOGO" ]]; then
  echo "Erro: informe o caminho do arquivo JSON do catálogo."
  echo "Exemplo: bash scripts/importar-catalogo.sh exemplos/catalogo-portugues.json"
  exit 1
fi

if [[ ! -f "$ARQUIVO_CATALOGO" ]]; then
  echo "Erro: arquivo não encontrado: $ARQUIVO_CATALOGO"
  exit 1
fi

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Erro: comando obrigatório não encontrado: $1"
    exit 1
  fi
}

require_command curl
require_command python3

api_get() {
  local path="$1"
  curl -sS \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Accept: application/json" \
    "$BASE_URL$path"
}

api_post() {
  local path="$1"
  local json="$2"
  curl -sS \
    -X POST \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d "$json" \
    "$BASE_URL$path"
}

json_string() {
  python3 -c 'import json,sys; print(json.dumps(sys.argv[1], ensure_ascii=False))' "$1"
}

find_id_by_nome() {
  local nome="$1"
  python3 -c '
import json, sys
nome = sys.argv[1].strip().lower()
data = json.load(sys.stdin)
for item in data:
    if str(item.get("nome", "")).strip().lower() == nome:
        print(item.get("id"))
        break
' "$nome"
}

extract_id() {
  python3 -c 'import json,sys; print(json.load(sys.stdin).get("id", ""))'
}

read_disciplina_nome() {
  python3 -c '
import json, sys
with open(sys.argv[1], encoding="utf-8") as f:
    data = json.load(f)
print(data["disciplina"].strip())
' "$ARQUIVO_CATALOGO"
}

read_assuntos() {
  python3 -c '
import json, sys
with open(sys.argv[1], encoding="utf-8") as f:
    data = json.load(f)
for assunto in data.get("assuntos", []):
    print(assunto["nome"].strip())
' "$ARQUIVO_CATALOGO"
}

read_subassuntos() {
  local assunto_nome="$1"
  python3 -c '
import json, sys
arquivo = sys.argv[1]
assunto_nome = sys.argv[2].strip().lower()
with open(arquivo, encoding="utf-8") as f:
    data = json.load(f)
for assunto in data.get("assuntos", []):
    if assunto.get("nome", "").strip().lower() == assunto_nome:
        for subassunto in assunto.get("subassuntos", []):
            print(str(subassunto).strip())
        break
' "$ARQUIVO_CATALOGO" "$assunto_nome"
}

validate_catalogo() {
  python3 -c '
import json, sys
arquivo = sys.argv[1]
with open(arquivo, encoding="utf-8") as f:
    data = json.load(f)
if not isinstance(data, dict):
    raise SystemExit("O JSON precisa ser um objeto.")
if not str(data.get("disciplina", "")).strip():
    raise SystemExit("O campo disciplina é obrigatório.")
assuntos = data.get("assuntos")
if not isinstance(assuntos, list) or not assuntos:
    raise SystemExit("O campo assuntos precisa ser uma lista não vazia.")
for i, assunto in enumerate(assuntos, start=1):
    if not isinstance(assunto, dict):
        raise SystemExit(f"O assunto #{i} precisa ser um objeto.")
    if not str(assunto.get("nome", "")).strip():
        raise SystemExit(f"O assunto #{i} não possui nome.")
    subassuntos = assunto.get("subassuntos", [])
    if not isinstance(subassuntos, list):
        raise SystemExit(f"Os subassuntos de {assunto.get('nome')} precisam ser uma lista.")
print("JSON validado com sucesso.")
' "$ARQUIVO_CATALOGO"
}

get_or_create_disciplina() {
  local nome="$1"
  local id
  id=$(api_get "/api/v1/catalogo/disciplinas" | find_id_by_nome "$nome" || true)

  if [[ -n "$id" ]]; then
    echo "$id"
    return
  fi

  local nome_json
  nome_json=$(json_string "$nome")
  api_post "/api/v1/admin/catalogo/disciplinas" "{\"nome\":$nome_json}" | extract_id
}

get_or_create_assunto() {
  local disciplina_id="$1"
  local nome="$2"
  local id
  id=$(api_get "/api/v1/catalogo/disciplinas/$disciplina_id/assuntos" | find_id_by_nome "$nome" || true)

  if [[ -n "$id" ]]; then
    echo "$id"
    return
  fi

  local nome_json
  nome_json=$(json_string "$nome")
  api_post "/api/v1/admin/catalogo/assuntos" "{\"disciplinaId\":$disciplina_id,\"nome\":$nome_json}" | extract_id
}

get_or_create_subassunto() {
  local assunto_id="$1"
  local nome="$2"
  local id
  id=$(api_get "/api/v1/catalogo/assuntos/$assunto_id/subassuntos" | find_id_by_nome "$nome" || true)

  if [[ -n "$id" ]]; then
    echo "    - já existe: $nome"
    return
  fi

  local nome_json
  nome_json=$(json_string "$nome")
  api_post "/api/v1/admin/catalogo/subassuntos" "{\"assuntoId\":$assunto_id,\"nome\":$nome_json}" >/dev/null
  echo "    - criado: $nome"
}

validate_catalogo

disciplina_nome=$(read_disciplina_nome)
echo "Importando catálogo em: $BASE_URL"
echo "Disciplina: $disciplina_nome"

disciplina_id=$(get_or_create_disciplina "$disciplina_nome")
echo "Disciplina ID: $disciplina_id"

while IFS= read -r assunto_nome; do
  [[ -z "$assunto_nome" ]] && continue

  assunto_id=$(get_or_create_assunto "$disciplina_id" "$assunto_nome")
  echo "Assunto: $assunto_nome (id=$assunto_id)"

  while IFS= read -r subassunto_nome; do
    [[ -z "$subassunto_nome" ]] && continue
    get_or_create_subassunto "$assunto_id" "$subassunto_nome"
  done < <(read_subassuntos "$assunto_nome")
done < <(read_assuntos)

echo "Importação finalizada com sucesso."
