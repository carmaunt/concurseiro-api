# Diagnóstico automático de Analytics

`GET /api/v1/admin/analytics/insights` exige perfil `ADMIN` e aceita os mesmos parâmetros do dashboard: `period`, `startDate`, `endDate`, `disciplinaId`, `assuntoId`, `subassuntoId`, `bancaId`, `instituicaoId` e `provaId`.

## Períodos comparados

- `today`: hoje até agora versus ontem até o mesmo horário.
- `7d` e `30d`: janela atual versus janela imediatamente anterior com a mesma duração.
- `current_month`: mês atual até agora versus mês anterior até o dia/horário equivalente.
- `custom`: intervalo escolhido versus intervalo imediatamente anterior de mesma duração.

## Score

O cálculo é determinístico: ativação 30%, engajamento 30%, retenção 20%, conteúdo 10% e qualidade dos dados 10%. Os thresholds principais são:

- Ativação: 20% a 75% é escalado de 0 a 100.
- Questões/sessão: 0,5 a 10.
- Questões/ativo real: 1 a 20.
- Sessão produtiva: 60 a 600 segundos; duração sem respostas não pontua.
- Retenção: D1 (5% a 25%), D7 (2% a 10%) e D30 (1% a 8%).
- Qualidade perde pontos por sessão/identidade ausente, evento desconhecido e erro.

O status é `INSUFFICIENT_DATA` se houver menos de 50 eventos, 5 ativos reais ou 10 respostas. Fora disso: `GOOD` a partir de 70 sem alerta crítico, `BAD` abaixo de 45 ou com qualidade crítica e `STABLE` nos demais casos.

A confiança é alta com 50 ativos reais, 200 respostas e 1.000 eventos; média com 10, 50 e 200; caso contrário é baixa. Sinais de muitas sessões/telas e poucas respostas também reduzem a confiança e marcam possível tráfego automatizado.

## Limitações

Os pesos estão em código e ainda não possuem histórico diário. Tráfego de teste é apenas sinalizado, não removido. Para exclusão futura, o desenho admite dimensões como `environment` e `installSource` quando esses campos forem adicionados aos eventos.
