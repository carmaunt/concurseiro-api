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
| `portal_landing_viewed` | primeira página do portal visitada na sessão | `anonymousId`, `sessionId`, origem/campanha e `acquisition_id` opaco |
| `store_cta_clicked` | clique em um CTA que leva ao Google Play | `sessionId`, identificador do CTA e o mesmo `acquisition_id` |
| `app_install_attributed` | primeira leitura bem-sucedida do Install Referrer no Android | `deviceId`, parâmetros permitidos do referrer e `acquisition_id`, quando presente |
| `install_referrer_unavailable` | Install Referrer indisponível ou sem resposta válida | `deviceId` e código técnico não sensível |
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

`eventName` usa `snake_case` minúsculo. Metadados têm limite de 4 KiB e chaves que indiquem senha, token, e-mail, CPF, telefone, endereço ou IP são rejeitadas. O endpoint possui limite de 120 eventos por minuto por IP. `acquisition_id` é um identificador aleatório de sessão: não contém nome, e-mail, telefone ou identificador de hardware.

## Dashboard administrativo

`GET /api/v1/admin/analytics/dashboard`

Exige JWT com role `ADMIN`. Parâmetros opcionais:

| Parâmetro | Tipo | Regra |
|---|---|---|
| `period` | texto | `today`, `7d` (padrão), `30d`, `current_month` ou `custom` |
| `startDate` | data `AAAA-MM-DD` | obrigatória quando `period=custom` |
| `endDate` | data `AAAA-MM-DD` | obrigatória quando `period=custom`; inclusiva para o usuário |
| `disciplinaId` | long | filtra todas as métricas do período |
| `assuntoId` | long | deriva e valida a disciplina pai |
| `subassuntoId` | long | deriva e valida assunto/disciplina pais |
| `bancaId` | long | filtra as métricas de estudo; não altera a coorte de aquisição |
| `instituicaoId` | long | filtra as métricas de estudo; não altera a coorte de aquisição |
| `provaId` | long | filtra as métricas de estudo; não altera a coorte de aquisição |

O período máximo é de 366 dias. A resposta contém total histórico de dispositivos, ativos hoje/no período, online nos últimos cinco minutos, questões respondidas hoje/no período, interação média e rankings de telas, filtros, disciplinas, assuntos e subassuntos.

### Funil de aquisição

O campo `acquisitionFunnel` acompanha uma coorte definida pela primeira visita ao portal dentro do período selecionado. Os estágios são:

1. visitante do portal (`portal_landing_viewed`);
2. clique no Google Play (`store_cta_clicked`);
3. instalação Android atribuída pelo Install Referrer (`app_install_attributed`);
4. ativação pela primeira questão respondida após a instalação;
5. retenção D+7 por atividade Android exatamente no sétimo dia após a ativação, somente entre usuários já elegíveis.

As taxas usam o estágio anterior como denominador, exceto `portalToActivationRate`, que usa visitantes do portal. `attributionCoverageRate` compara instalações com e sem `acquisition_id`. O status é `SEM_DADOS`, `ATRIBUICAO_PARCIAL` (cobertura abaixo de 80%) ou `MEDINDO`. Denominador zero sempre produz taxa zero, sem estimativa artificial. Filtros de conteúdo não são aplicados ao funil de aquisição, porque sua coorte é a entrada no portal.

Exemplo:

```http
GET /api/v1/admin/analytics/dashboard?period=custom&startDate=2026-06-01&endDate=2026-06-19&disciplinaId=1
Authorization: Bearer <token-admin>
```

## Teste local

1. Inicie PostgreSQL e configure `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` e `JWT_SECRET`.
2. Execute `./mvnw spring-boot:run`. O Liquibase cria `app_events` automaticamente.
3. Envie um evento com `curl` para `/api/v1/analytics/events`.
4. Entre no painel como `ADMIN`, abra **Analytics** e escolha o período.
5. Valide a autorização: uma conta sem role `ADMIN` deve receber `403` no endpoint do dashboard.

## Próximas evoluções

Exportação CSV, agregações diárias, política automática de retenção, consentimento/opt-out e integração dos códigos de erro com Crashlytics/Sentry.
