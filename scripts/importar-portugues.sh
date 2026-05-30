#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-https://concurseiro-api-lnae.onrender.com}"
ADMIN_TOKEN="${ADMIN_TOKEN:-}"
DISCIPLINA_NOME="Português"

if [[ -z "$ADMIN_TOKEN" ]]; then
  echo "Erro: informe o token de admin na variável ADMIN_TOKEN."
  echo "Exemplo: ADMIN_TOKEN='seu_token' BASE_URL='$BASE_URL' bash scripts/importar-portugues.sh"
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

importar_assunto() {
  local disciplina_id="$1"
  local assunto_nome="$2"
  shift 2

  local assunto_id
  assunto_id=$(get_or_create_assunto "$disciplina_id" "$assunto_nome")
  echo "Assunto: $assunto_nome (id=$assunto_id)"

  local subassunto
  for subassunto in "$@"; do
    get_or_create_subassunto "$assunto_id" "$subassunto"
  done
}

echo "Importando catálogo de Português em: $BASE_URL"

disciplina_id=$(get_or_create_disciplina "$DISCIPLINA_NOME")
echo "Disciplina: $DISCIPLINA_NOME (id=$disciplina_id)"

importar_assunto "$disciplina_id" "Interpretação e compreensão de texto" \
  "Ideia principal" "Ideias secundárias" "Informações explícitas" "Informações implícitas" "Inferência" "Intenção do autor" "Sentido literal" "Sentido figurado" "Pressupostos" "Subentendidos" "Tese do texto" "Argumentos do texto" "Ponto de vista do autor" "Ironia" "Humor" "Crítica" "Ambiguidade textual" "Relação entre texto verbal e não verbal" "Intertextualidade"

importar_assunto "$disciplina_id" "Gênero textual" \
  "Notícia" "Reportagem" "Entrevista" "Artigo de opinião" "Editorial" "Crônica" "Conto" "Fábula" "Lenda" "Romance" "Carta" "E-mail" "Diário" "Resenha" "Charge" "Tirinha" "Propaganda" "Manual" "Receita" "Bula" "Regulamento" "Texto dissertativo-argumentativo" "Texto narrativo" "Texto descritivo" "Texto injuntivo" "Texto expositivo"

importar_assunto "$disciplina_id" "Fonologia" \
  "Fonema" "Letra" "Vogal" "Semivogal" "Consoante" "Encontro vocálico" "Ditongo" "Tritongo" "Hiato" "Encontro consonantal" "Dígrafo" "Sílaba" "Separação silábica" "Classificação das palavras quanto ao número de sílabas" "Classificação das palavras quanto à tonicidade" "Oxítona" "Paroxítona" "Proparoxítona"

importar_assunto "$disciplina_id" "Ortografia" \
  "Escrita correta das palavras" "Uso de S, SS, C, Ç e SC" "Uso de G e J" "Uso de X e CH" "Uso de Z e S" "Uso de H" "Uso dos porquês" "Mal e mau" "Mas e mais" "Onde e aonde" "A, há e à" "Senão e se não" "Acerca de, a cerca de e há cerca de" "Hífen" "Novo Acordo Ortográfico"

importar_assunto "$disciplina_id" "Acentuação gráfica" \
  "Acento agudo" "Acento circunflexo" "Acento grave" "Acentuação das oxítonas" "Acentuação das paroxítonas" "Acentuação das proparoxítonas" "Acentuação dos monossílabos tônicos" "Acentuação dos hiatos" "Acentuação dos ditongos abertos" "Acento diferencial" "Regras do Novo Acordo Ortográfico"

importar_assunto "$disciplina_id" "Morfologia" \
  "Substantivo" "Artigo" "Adjetivo" "Numeral" "Pronome" "Verbo" "Advérbio" "Preposição" "Conjunção" "Interjeição" "Flexão de gênero" "Flexão de número" "Flexão de grau" "Flexão verbal" "Tempos verbais" "Modos verbais" "Vozes verbais" "Formação de palavras" "Derivação" "Composição"

importar_assunto "$disciplina_id" "Sintaxe" \
  "Frase" "Oração" "Período" "Sujeito" "Predicado" "Objeto direto" "Objeto indireto" "Complemento nominal" "Agente da passiva" "Adjunto adnominal" "Adjunto adverbial" "Aposto" "Vocativo" "Período simples" "Período composto" "Orações coordenadas" "Orações subordinadas" "Função sintática dos termos"

importar_assunto "$disciplina_id" "Concordância" \
  "Concordância verbal" "Concordância nominal" "Concordância com sujeito simples" "Concordância com sujeito composto" "Concordância com verbo haver" "Concordância com verbo fazer" "Concordância com expressões partitivas" "Concordância com pronomes relativos" "Concordância com adjetivos" "Concordância com as palavras anexo, incluso, obrigado, mesmo, meio e bastante" "Concordância em expressões como é proibido, é necessário e é bom"

importar_assunto "$disciplina_id" "Regência" \
  "Regência verbal" "Regência nominal" "Verbos transitivos diretos" "Verbos transitivos indiretos" "Verbos transitivos diretos e indiretos" "Verbos intransitivos" "Verbos com mudança de sentido" "Uso correto das preposições" "Regência de nomes" "Regência de adjetivos" "Regência de substantivos" "Regência de advérbios"

importar_assunto "$disciplina_id" "Crase" \
  "Conceito de crase" "Uso obrigatório da crase" "Uso proibido da crase" "Uso facultativo da crase" "Crase antes de palavra feminina" "Crase antes de horas" "Crase em locuções femininas" "Crase com nomes próprios femininos" "Crase com pronomes possessivos femininos" "Crase depois da preposição até" "Casos especiais de crase"

importar_assunto "$disciplina_id" "Pontuação" \
  "Vírgula" "Ponto final" "Ponto e vírgula" "Dois-pontos" "Ponto de interrogação" "Ponto de exclamação" "Reticências" "Aspas" "Travessão" "Parênteses" "Pontuação em enumerações" "Pontuação em vocativo" "Pontuação em aposto" "Pontuação em orações coordenadas" "Pontuação em orações subordinadas" "Pontuação no discurso direto"

importar_assunto "$disciplina_id" "Semântica" \
  "Sentido das palavras" "Denotação" "Conotação" "Sinonímia" "Antonímia" "Homonímia" "Paronímia" "Polissemia" "Ambiguidade" "Campo semântico" "Hiperonímia" "Hiponímia" "Relação de sentido pelo contexto"

importar_assunto "$disciplina_id" "Coesão e coerência" \
  "Coesão textual" "Coerência textual" "Conectivos" "Referência pronominal" "Substituição lexical" "Repetição controlada" "Progressão textual" "Relação de causa" "Relação de consequência" "Relação de oposição" "Relação de conclusão" "Relação de explicação" "Relação de comparação" "Relação de finalidade" "Continuidade das ideias" "Não contradição"

importar_assunto "$disciplina_id" "Figuras de linguagem" \
  "Metáfora" "Comparação" "Metonímia" "Catacrese" "Sinestesia" "Hipérbole" "Eufemismo" "Ironia" "Antítese" "Paradoxo" "Personificação" "Pleonasmo" "Elipse" "Zeugma" "Anáfora" "Hipérbato" "Aliteração" "Assonância" "Onomatopeia"

importar_assunto "$disciplina_id" "Redação oficial" \
  "Conceito de redação oficial" "Princípios da redação oficial" "Clareza" "Concisão" "Objetividade" "Formalidade" "Impessoalidade" "Padronização" "Coesão" "Coerência" "Uso da norma-padrão" "Ofício" "Memorando" "Ata" "Requerimento" "Declaração" "Relatório" "Parecer" "E-mail institucional" "Pronomes de tratamento" "Fecho de comunicação oficial" "Vocativo oficial" "Endereçamento" "Assinatura" "Estrutura dos documentos oficiais"

echo "Importação finalizada com sucesso."
