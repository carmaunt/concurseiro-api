package br.com.concurseiro.api.analytics.repository;

import br.com.concurseiro.api.analytics.model.AppEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface AppEventRepository extends JpaRepository<AppEvent, Long> {

    @Query(value = """
            SELECT COUNT(DISTINCT device_id)
            FROM app_events
            WHERE device_id IS NOT NULL AND device_id <> ''
            """, nativeQuery = true)
    long countDistinctDevices();

    @Query(value = """
            SELECT COUNT(DISTINCT COALESCE(CAST(user_id AS TEXT), device_id))
            FROM app_events
            WHERE created_at >= :fromDate
              AND created_at < :toDate
              AND COALESCE(CAST(user_id AS TEXT), device_id) IS NOT NULL
            """, nativeQuery = true)
    long countActiveIdentitiesBetween(@Param("fromDate") OffsetDateTime from, @Param("toDate") OffsetDateTime to);

    @Query(value = """
            SELECT COUNT(*)
            FROM app_events
            WHERE event_name = :eventName
              AND created_at >= :fromDate
              AND created_at < :toDate
            """, nativeQuery = true)
    long countEventBetween(@Param("eventName") String eventName, @Param("fromDate") OffsetDateTime from, @Param("toDate") OffsetDateTime to);

    @Query(value = """
            SELECT COUNT(DISTINCT COALESCE(CAST(user_id AS TEXT), device_id))
            FROM app_events
            WHERE created_at >= :since
              AND COALESCE(CAST(user_id AS TEXT), device_id) IS NOT NULL
            """, nativeQuery = true)
    long countOnlineSince(@Param("since") OffsetDateTime since);

    @Query(value = """
            SELECT COALESCE(AVG(interaction_duration_seconds), 0)
            FROM app_events
            WHERE interaction_duration_seconds IS NOT NULL
              AND created_at >= :fromDate
              AND created_at < :toDate
            """, nativeQuery = true)
    double averageInteractionSecondsBetween(@Param("fromDate") OffsetDateTime from, @Param("toDate") OffsetDateTime to);

    @Query(value = """
            SELECT screen_name AS label, COUNT(*) AS total
            FROM app_events
            WHERE screen_name IS NOT NULL AND screen_name <> ''
              AND created_at >= :fromDate
              AND created_at < :toDate
            GROUP BY screen_name
            ORDER BY total DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<AnalyticsRankingProjection> topScreens(@Param("fromDate") OffsetDateTime from, @Param("toDate") OffsetDateTime to, @Param("limit") int limit);

    @Query(value = """
            SELECT filter_name AS label, COUNT(*) AS total
            FROM app_events
            WHERE event_name = 'filter_applied'
              AND filter_name IS NOT NULL AND filter_name <> ''
              AND created_at >= :fromDate
              AND created_at < :toDate
            GROUP BY filter_name
            ORDER BY total DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<AnalyticsRankingProjection> topFilters(@Param("fromDate") OffsetDateTime from, @Param("toDate") OffsetDateTime to, @Param("limit") int limit);

    @Query(value = """
            SELECT disciplina_nome AS label, COUNT(*) AS total
            FROM app_events
            WHERE disciplina_nome IS NOT NULL AND disciplina_nome <> ''
              AND created_at >= :fromDate
              AND created_at < :toDate
            GROUP BY disciplina_nome
            ORDER BY total DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<AnalyticsRankingProjection> topDisciplinas(@Param("fromDate") OffsetDateTime from, @Param("toDate") OffsetDateTime to, @Param("limit") int limit);

    @Query(value = """
            SELECT assunto_nome AS label, COUNT(*) AS total
            FROM app_events
            WHERE assunto_nome IS NOT NULL AND assunto_nome <> ''
              AND created_at >= :fromDate
              AND created_at < :toDate
            GROUP BY assunto_nome
            ORDER BY total DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<AnalyticsRankingProjection> topAssuntos(@Param("fromDate") OffsetDateTime from, @Param("toDate") OffsetDateTime to, @Param("limit") int limit);

    @Query(value = """
            SELECT subassunto_nome AS label, COUNT(*) AS total
            FROM app_events
            WHERE subassunto_nome IS NOT NULL AND subassunto_nome <> ''
              AND created_at >= :fromDate
              AND created_at < :toDate
            GROUP BY subassunto_nome
            ORDER BY total DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<AnalyticsRankingProjection> topSubassuntos(@Param("fromDate") OffsetDateTime from, @Param("toDate") OffsetDateTime to, @Param("limit") int limit);
}
