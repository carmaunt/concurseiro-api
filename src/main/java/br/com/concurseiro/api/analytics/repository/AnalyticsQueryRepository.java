package br.com.concurseiro.api.analytics.repository;

import br.com.concurseiro.api.analytics.dto.AnalyticsDashboardResponse.DailyTrend;
import br.com.concurseiro.api.analytics.dto.AnalyticsRankingItemResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class AnalyticsQueryRepository {
    private static final String IDENTITY = "COALESCE('u:' || e.user_id::text, 'a:' || NULLIF(e.anonymous_id,''), 'd:' || NULLIF(e.device_id,''))";
    private final NamedParameterJdbcTemplate jdbc;
    public AnalyticsQueryRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public long countDevices() { return scalarLong("SELECT COUNT(DISTINCT device_id) FROM app_events WHERE NULLIF(device_id,'') IS NOT NULL", new MapSqlParameterSource()); }
    public long countActive(AnalyticsFilter f) { return count("COUNT(DISTINCT " + IDENTITY + ")", f, null); }
    public long countRealActive(AnalyticsFilter f) { return count("COUNT(DISTINCT " + IDENTITY + ")", f, "question_answered"); }
    public long countActiveEvent(String event, AnalyticsFilter f) { return count("COUNT(DISTINCT " + IDENTITY + ")", f, event); }
    public long countNewIdentities(AnalyticsFilter f) {
        QueryParts p = where(f, null, IDENTITY + " IS NOT NULL");
        return scalarLong("SELECT COUNT(*) FROM (SELECT " + IDENTITY + " identity FROM app_events e GROUP BY identity HAVING MIN(e.created_at)>=:fromDate AND MIN(e.created_at)<:toDate) x", p.params);
    }
    public long countIdentified(AnalyticsFilter f) { return count("COUNT(DISTINCT e.user_id)", f, null); }
    public long countEvent(String event, AnalyticsFilter f) { return count("COUNT(*)", f, event); }
    public long countSessions(AnalyticsFilter f) { return count("COUNT(DISTINCT e.session_id)", f, null); }
    public double averageAccuracy(AnalyticsFilter f) { return scalar("AVG(CASE WHEN e.answer_correct THEN 1.0 ELSE 0.0 END)", f, "question_answered") * 100; }
    public double averageSessionSeconds(AnalyticsFilter f) {
        QueryParts p = where(f, "session_ended", "jsonb_exists(e.metadata, 'duration_seconds')");
        double explicit = scalarSql("SELECT AVG((e.metadata->>'duration_seconds')::double precision) FROM app_events e " + p.sql, p.params);
        if (explicit > 0) return explicit;
        p = where(f, null, "e.session_id IS NOT NULL");
        return scalarSql("SELECT AVG(seconds) FROM (SELECT EXTRACT(EPOCH FROM MAX(e.created_at)-MIN(e.created_at)) seconds FROM app_events e " + p.sql + " GROUP BY e.session_id) s", p.params);
    }
    /** Compatibilidade com o contrato do MVP anterior. */
    public double averageInteractionSeconds(AnalyticsFilter f) {
        return scalar("AVG(e.interaction_duration_ms) / 1000.0", f, null);
    }
    public double averageMinutesToFirstAnswer(AnalyticsFilter f) {
        QueryParts p = where(f, null, IDENTITY + " IS NOT NULL");
        return scalarSql("SELECT AVG(EXTRACT(EPOCH FROM (first_answer-first_event))/60.0) FROM (SELECT " + IDENTITY + " identity, MIN(e.created_at) first_event, MIN(e.created_at) FILTER (WHERE e.event_name='question_answered') first_answer FROM app_events e " + p.sql + " GROUP BY identity) x WHERE first_answer IS NOT NULL", p.params);
    }
    public long identitiesWithAtLeastQuestions(AnalyticsFilter f, int minimum) {
        QueryParts p = where(f, "question_answered", IDENTITY + " IS NOT NULL"); p.params.addValue("minimum", minimum);
        return scalarLong("SELECT COUNT(*) FROM (SELECT " + IDENTITY + " FROM app_events e " + p.sql + " GROUP BY " + IDENTITY + " HAVING COUNT(*) >= :minimum) x", p.params);
    }
    public long inactiveUsers(OffsetDateTime now) {
        var p = new MapSqlParameterSource().addValue("before", now.minusDays(7));
        return scalarLong("SELECT COUNT(*) FROM (SELECT user_id FROM app_events WHERE user_id IS NOT NULL AND event_name='question_answered' GROUP BY user_id HAVING MAX(created_at) < :before) x", p);
    }
    public double retention(int day, AnalyticsFilter f) {
        QueryParts p = where(f, null, IDENTITY + " IS NOT NULL"); p.params.addValue("day", day);
        return scalarSql("WITH first_seen AS (SELECT " + IDENTITY + " identity, MIN(e.created_at)::date cohort FROM app_events e " + p.sql + " GROUP BY identity), retained AS (SELECT DISTINCT f.identity FROM first_seen f JOIN app_events e ON " + IDENTITY + "=f.identity AND e.created_at::date=f.cohort+:day) SELECT COALESCE(100.0*COUNT(r.identity)/NULLIF(COUNT(f.identity),0),0) FROM first_seen f LEFT JOIN retained r USING(identity)", p.params);
    }
    public List<DailyTrend> dailyTrend(AnalyticsFilter f) {
        QueryParts p = where(f, null);
        return jdbc.query("SELECT e.created_at::date event_day, COUNT(DISTINCT " + IDENTITY + ") active, COUNT(DISTINCT e.session_id) sessions, COUNT(*) FILTER (WHERE e.event_name='question_answered') questions, COALESCE(100.0*AVG(CASE WHEN e.answer_correct THEN 1.0 WHEN e.answer_correct=false THEN 0.0 END),0) accuracy FROM app_events e " + p.sql + " GROUP BY event_day ORDER BY event_day", p.params, (rs,n) -> new DailyTrend(rs.getDate("event_day").toLocalDate(), rs.getLong("active"), rs.getLong("sessions"), rs.getLong("questions"), rs.getDouble("accuracy")));
    }
    public List<AnalyticsRankingItemResponse> topScreens(AnalyticsFilter f,int l) { return ranking("e.screen_name", null, f,l,"e.screen_name IS NOT NULL", "e.event_name IN ('screen_viewed','screen_view')"); }
    public List<AnalyticsRankingItemResponse> topFilters(AnalyticsFilter f,int l) { return ranking("COALESCE(e.filter_name,e.metadata->>'filter_name','Filtro')", "filter_applied", f,l,"true"); }
    public List<AnalyticsRankingItemResponse> dimension(String table,String column,String event,AnalyticsFilter f,int l) {
        QueryParts p=where(f,event,"e."+column+" IS NOT NULL"); p.params.addValue("limit",l);
        return jdbc.query("SELECT d.id,d.nome label,COUNT(*) total FROM app_events e JOIN "+table+" d ON d.id=e."+column+" "+p.sql+" GROUP BY d.id,d.nome ORDER BY total DESC,label LIMIT :limit",p.params,(rs,n)->new AnalyticsRankingItemResponse(rs.getLong("id"),rs.getString("label"),rs.getLong("total")));
    }
    public List<AnalyticsRankingItemResponse> questionRanking(AnalyticsFilter f,boolean correct,int l) {
        QueryParts p=where(f,"question_answered","e.question_id IS NOT NULL","e.answer_correct = "+correct);p.params.addValue("limit",l);
        return jdbc.query("SELECT NULL id,e.question_id label,COUNT(*) total FROM app_events e "+p.sql+" GROUP BY e.question_id ORDER BY total DESC LIMIT :limit",p.params,(rs,n)->new AnalyticsRankingItemResponse(null,rs.getString("label"),rs.getLong("total")));
    }
    public long countMetadataBoolean(String event,String key,AnalyticsFilter f,boolean value) { QueryParts p=where(f,event,"COALESCE((e.metadata->>'"+key+"')::boolean,false)="+value); return scalarLong("SELECT COUNT(*) FROM app_events e "+p.sql,p.params); }
    public OffsetDateTime lastEventAt(){ return jdbc.queryForObject("SELECT MAX(created_at) FROM app_events",new MapSqlParameterSource(),OffsetDateTime.class); }
    public Map<String,Long> eventsByVersion(AnalyticsFilter f){ QueryParts p=where(f,null);Map<String,Long> out=new LinkedHashMap<>();jdbc.query("SELECT COALESCE(app_version,'desconhecida') v,COUNT(*) n FROM app_events e "+p.sql+" GROUP BY v ORDER BY n DESC",p.params,rs->{out.put(rs.getString("v"),rs.getLong("n"));});return out; }
    public long countUnknown(AnalyticsFilter f,Set<String> official){ QueryParts p=where(f,null);p.params.addValue("official",official);return scalarLong("SELECT COUNT(*) FROM app_events e "+p.sql+" AND e.event_name NOT IN (:official)",p.params); }
    public double missingPercent(AnalyticsFilter f,String expression){ QueryParts p=where(f,null);return scalarSql("SELECT COALESCE(100.0*COUNT(*) FILTER (WHERE "+expression+")/NULLIF(COUNT(*),0),0) FROM app_events e "+p.sql,p.params); }
    public List<AnalyticsRankingItemResponse> recentErrors(AnalyticsFilter f,int l){return ranking("COALESCE(e.metadata->>'error_type',e.metadata->>'message','Erro')","error_occurred",f,l,"true");}

    private List<AnalyticsRankingItemResponse> ranking(String label,String event,AnalyticsFilter f,int limit,String... required){QueryParts p=where(f,event,required);p.params.addValue("limit",limit);return jdbc.query("SELECT NULL id,"+label+" label,COUNT(*) total FROM app_events e "+p.sql+" GROUP BY "+label+" ORDER BY total DESC,label LIMIT :limit",p.params,(rs,n)->new AnalyticsRankingItemResponse(null,rs.getString("label"),rs.getLong("total")));}
    private long count(String expr,AnalyticsFilter f,String event){QueryParts p=where(f,event);return scalarLong("SELECT "+expr+" FROM app_events e "+p.sql,p.params);}
    private double scalar(String expr,AnalyticsFilter f,String event){QueryParts p=where(f,event);return scalarSql("SELECT COALESCE("+expr+",0) FROM app_events e "+p.sql,p.params);}
    private long scalarLong(String sql,MapSqlParameterSource p){Long v=jdbc.queryForObject(sql,p,Long.class);return v==null?0:v;}
    private double scalarSql(String sql,MapSqlParameterSource p){Double v=jdbc.queryForObject(sql,p,Double.class);return v==null?0:v;}
    private QueryParts where(AnalyticsFilter f,String event,String... extra){StringBuilder s=new StringBuilder("WHERE e.created_at>=:fromDate AND e.created_at<:toDate");MapSqlParameterSource p=new MapSqlParameterSource().addValue("fromDate",f.from).addValue("toDate",f.to);if(event!=null){s.append(" AND e.event_name=:event");p.addValue("event",event);} add(s,p,"disciplina_id",f.disciplinaId);add(s,p,"assunto_id",f.assuntoId);add(s,p,"subassunto_id",f.subassuntoId);add(s,p,"banca_id",f.bancaId);add(s,p,"instituicao_id",f.instituicaoId);add(s,p,"prova_id",f.provaId);for(String x:extra)s.append(" AND ").append(x);return new QueryParts(s.toString(),p);}
    private void add(StringBuilder s,MapSqlParameterSource p,String column,Long value){if(value!=null){String key=column.replace("_","");s.append(" AND e.").append(column).append("=:").append(key);p.addValue(key,value);}}
    public record AnalyticsFilter(OffsetDateTime from,OffsetDateTime to,Long disciplinaId,Long assuntoId,Long subassuntoId,Long bancaId,Long instituicaoId,Long provaId){
        public AnalyticsFilter(OffsetDateTime from, OffsetDateTime to, Long disciplinaId, Long assuntoId, Long subassuntoId) {
            this(from, to, disciplinaId, assuntoId, subassuntoId, null, null, null);
        }
    }
    private record QueryParts(String sql,MapSqlParameterSource params){}
}
