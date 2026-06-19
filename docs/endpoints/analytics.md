# Analytics do aplicativo

O MVP de analytics registra eventos genéricos do app e entrega métricas agregadas ao painel administrativo. O dashboard não retorna nome, e-mail ou outros dados pessoais.

## Registrar evento

`POST /api/v1/analytics/events`

O endpoint aceita requisições com ou sem JWT. Quando há um JWT válido, `user_id` é obtido exclusivamente do token. Em eventos anônimos, `deviceId` é obrigatório e deve ser um identificador aleatório persistido pelo app, não IMEI, Android ID, telefone ou e-mail.

```json
{
  "eventName": "question_answered",
  "deviceId": "550e8400-e29b-41d4-a716-446655440000",
  "sessionId": "d2719bf4-cc67-4f93-a10c-f7c9c1560a34",
  "screenName": "questao",
  "questionId": "Q123ABC",
  "answerCorrect": true,
  "disciplinaId": 1,
  "assuntoId": 10,
  "subassuntoId": 42,
  "interactionDurationMs": 18500,
  "appVersion": "1.0.10",
  "platform": "android",
  "metadata": {
    "source": "practice"
  }
}
```

Resposta `201 Created`:

```json
{
  "success": true,
  "data": {
    "id": 123,
    "receivedAt": "2026-06-19T15:00:00-03:00"
  }
}
```

### Eventos recomendados

| `eventName` | Quando enviar | Campos úteis |
|---|---|---|
| `app_open` | abertura do processo do app | `deviceId`, `appVersion`, `platform` |
| `session_start` | app entra em primeiro plano | `sessionId` |
| `session_end` | app sai do primeiro plano | `interactionDurationMs` |
| `user_online` | ao iniciar e a cada 60 segundos em primeiro plano | `sessionId` |
| `screen_view` | mudança de rota/tela | `screenName` |
| `filter_applied` | aplicação de cada tipo de filtro | `filterName` e dimensões |
| `question_viewed` | questão exibida | `questionId` e dimensões |
| `question_answered` | resposta confirmada | `questionId`, `answerCorrect` e dimensões |
| `discipline_accessed` | entrada explícita em disciplina | `disciplinaId` |
| `subject_accessed` | entrada explícita em assunto | `assuntoId` |
| `subsubject_accessed` | entrada explícita em subassunto | `subassuntoId` |
| `interaction_time` | interação mensurável | `interactionDurationMs` |
| `app_error` | erro recuperável | somente código técnico não sensível em `metadata` |
| `app_crash` | crash detectado no próximo início | somente código técnico não sensível em `metadata` |

`eventName` usa `snake_case` minúsculo. Metadados têm limite de 4 KiB e chaves que indiquem senha, token, e-mail, CPF, telefone, endereço ou IP são rejeitadas. O endpoint possui limite de 120 eventos por minuto por IP.

## Dashboard administrativo

`GET /api/v1/admin/analytics/dashboard`

Exige JWT com role `ADMIN`. Parâmetros opcionais:

| Parâmetro | Tipo | Regra |
|---|---|---|
| `from` | ISO-8601 com offset | início inclusivo; padrão: 7 dias antes de `to` |
| `to` | ISO-8601 com offset | fim exclusivo; padrão: agora |
| `disciplinaId` | long | filtra todas as métricas do período |
| `assuntoId` | long | deriva e valida a disciplina pai |
| `subassuntoId` | long | deriva e valida assunto/disciplina pais |

O período máximo é de 366 dias. A resposta contém total histórico de dispositivos, ativos hoje/no período, online nos últimos cinco minutos, questões respondidas hoje/no período, interação média e rankings de telas, filtros, disciplinas, assuntos e subassuntos.

Exemplo:

```http
GET /api/v1/admin/analytics/dashboard?from=2026-06-01T00:00:00-03:00&to=2026-06-20T00:00:00-03:00&disciplinaId=1
Authorization: Bearer <token-admin>
```

## Teste local

1. Inicie PostgreSQL e configure `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` e `JWT_SECRET`.
2. Execute `./mvnw spring-boot:run`. O Liquibase cria `app_events` automaticamente.
3. Envie um evento com `curl` para `/api/v1/analytics/events`.
4. Entre no painel como `ADMIN`, abra **Analytics** e escolha o período.
5. Valide a autorização: uma conta sem role `ADMIN` deve receber `403` no endpoint do dashboard.

## Próximas evoluções

Retenção/coortes, funis, usuários engajados e inativos pseudonimizados, exportação CSV, agregações diárias, política automática de retenção, consentimento/opt-out e integração dos códigos de erro com Crashlytics/Sentry.
