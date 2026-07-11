# Endpoints do Estudante

## `GET /api/v1/estudante/dashboard`

Retorna métricas reais do usuário autenticado para alimentar o dashboard da web.

Autenticação: obrigatória.

Resposta:

```json
{
  "questoesResolvidas": 2,
  "acertos": 1,
  "erros": 1,
  "aproveitamento": 50.0,
  "ultimasRespostas": [
    {
      "idQuestion": "Q3D687A42A8254BE",
      "disciplina": "Língua Portuguesa",
      "respostaSelecionada": "C",
      "gabarito": "B",
      "acertou": false,
      "respondidaEm": "2026-07-09T11:25:39.636406Z"
    }
  ],
  "desempenhoPorDisciplina": [
    {
      "disciplina": "Direito Constitucional",
      "total": 1,
      "acertos": 1,
      "erros": 0,
      "aproveitamento": 100.0
    }
  ]
}
```

Observações:

- Os números são calculados a partir de `respostas_questoes_usuario`.
- Quando não há histórico, os totais retornam zerados e as listas retornam vazias.
