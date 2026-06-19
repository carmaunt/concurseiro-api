package br.com.concurseiro.api.analytics.repository;

import br.com.concurseiro.api.analytics.dto.AnalyticsRankingItemResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class AnalyticsQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AnalyticsQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long countDevices() {
        Long value = jdbc.queryForObject("""
                SELECT COUNT(DISTINCT device_id)
                FROM app_events
                WHERE device_id IS NOT NULL AND device_id <> ''
                """, new MapSqlParameterSource(), Long.class);
        return value == null ? 0 : value;
    }

    public long countActive(AnalyticsFilter filter) {
        return count("COUNT(DISTINCT COALESCE('u:' || CAST(user_id AS TEXT), 'd:' || device_id))", filter, null);
    }

    public long countEvent(String eventName, AnalyticsFilter filter) {
        return count("COUNT(*)", filter, eventName);
    }

    public double averageInteractionSeconds(AnalyticsFilter filter) {
        QueryParts parts = where(filter, null);
        Double value = jdbc.queryForObject("""
                SELECT COALESCE(AVG(interaction_duration_ms) / 1000.0, 0)
                FROM app_events e
                """ + parts.sql(), parts.params(), Double.class);
        return value == null ? 0 : value;
    }

    public List<AnalyticsRankingItemResponse> topScreens(AnalyticsFilter filter, int limit) {
        return ranking("e.screen_name", "e.screen_name", "screen_view", filter, limit, "e.screen_name IS NOT NULL");
    }

    public List<AnalyticsRankingItemResponse> topFilters(AnalyticsFilter filter, int limit) {
        return ranking("e.filter_name", "e.filter_name", "filter_applied", filter, limit, "e.filter_name IS NOT NULL");
    }

    public List<AnalyticsRankingItemResponse> topDisciplinas(AnalyticsFilter filter, int limit) {
        return dimensionRanking("disciplinas", "disciplina_id", filter, limit);
    }

    public List<AnalyticsRankingItemResponse> topAssuntos(AnalyticsFilter filter, int limit) {
        return dimensionRanking("assuntos", "assunto_id", filter, limit);
    }

    public List<AnalyticsRankingItemResponse> topSubassuntos(AnalyticsFilter filter, int limit) {
        return dimensionRanking("subassuntos", "subassunto_id", filter, limit);
    }

    private long count(String expression, AnalyticsFilter filter, String eventName) {
        QueryParts parts = where(filter, eventName);
        Long value = jdbc.queryForObject("SELECT " + expression + " FROM app_events e " + parts.sql(), parts.params(), Long.class);
        return value == null ? 0 : value;
    }

    private List<AnalyticsRankingItemResponse> ranking(
            String idExpression,
            String labelExpression,
            String eventName,
            AnalyticsFilter filter,
            int limit,
            String required
    ) {
        QueryParts parts = where(filter, eventName, required);
        parts.params().addValue("limit", limit);
        String sql = "SELECT NULL AS id, " + labelExpression + " AS label, COUNT(*) AS total "
                + "FROM app_events e " + parts.sql()
                + " GROUP BY " + idExpression + ", " + labelExpression
                + " ORDER BY total DESC, label ASC LIMIT :limit";
        return jdbc.query(sql, parts.params(), (rs, row) ->
                new AnalyticsRankingItemResponse(null, rs.getString("label"), rs.getLong("total")));
    }

    private List<AnalyticsRankingItemResponse> dimensionRanking(
            String table,
            String column,
            AnalyticsFilter filter,
            int limit
    ) {
        QueryParts parts = where(filter, null, "e." + column + " IS NOT NULL");
        parts.params().addValue("limit", limit);
        String sql = "SELECT d.id, d.nome AS label, COUNT(*) AS total "
                + "FROM app_events e JOIN " + table + " d ON d.id = e." + column + " "
                + parts.sql() + " GROUP BY d.id, d.nome ORDER BY total DESC, label ASC LIMIT :limit";
        return jdbc.query(sql, parts.params(), (rs, row) ->
                new AnalyticsRankingItemResponse(rs.getLong("id"), rs.getString("label"), rs.getLong("total")));
    }

    private QueryParts where(AnalyticsFilter filter, String eventName, String... additionalConditions) {
        StringBuilder sql = new StringBuilder("WHERE e.created_at >= :fromDate AND e.created_at < :toDate");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fromDate", filter.from())
                .addValue("toDate", filter.to());

        if (eventName != null) {
            sql.append(" AND e.event_name = :eventName");
            params.addValue("eventName", eventName);
        }
        if (filter.disciplinaId() != null) {
            sql.append(" AND e.disciplina_id = :disciplinaId");
            params.addValue("disciplinaId", filter.disciplinaId());
        }
        if (filter.assuntoId() != null) {
            sql.append(" AND e.assunto_id = :assuntoId");
            params.addValue("assuntoId", filter.assuntoId());
        }
        if (filter.subassuntoId() != null) {
            sql.append(" AND e.subassunto_id = :subassuntoId");
            params.addValue("subassuntoId", filter.subassuntoId());
        }
        for (String condition : additionalConditions) {
            sql.append(" AND ").append(condition);
        }
        return new QueryParts(sql.toString(), params);
    }

    public record AnalyticsFilter(
            OffsetDateTime from,
            OffsetDateTime to,
            Long disciplinaId,
            Long assuntoId,
            Long subassuntoId
    ) {}

    private record QueryParts(String sql, MapSqlParameterSource params) {}
}
