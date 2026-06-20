package br.com.concurseiro.api.analytics.service;

import br.com.concurseiro.api.analytics.dto.*;
import br.com.concurseiro.api.analytics.model.AppEvent;
import br.com.concurseiro.api.analytics.model.AnalyticsEventName;
import br.com.concurseiro.api.analytics.repository.AnalyticsQueryRepository;
import br.com.concurseiro.api.analytics.repository.AnalyticsQueryRepository.AnalyticsFilter;
import br.com.concurseiro.api.analytics.repository.AppEventRepository;
import br.com.concurseiro.api.catalogo.assunto.model.Assunto;
import br.com.concurseiro.api.catalogo.assunto.repository.AssuntoRepository;
import br.com.concurseiro.api.catalogo.disciplina.repository.DisciplinaRepository;
import br.com.concurseiro.api.catalogo.subassunto.model.SubAssunto;
import br.com.concurseiro.api.catalogo.subassunto.repository.SubAssuntoRepository;
import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class AnalyticsService {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final int ONLINE_WINDOW_MINUTES = 5;
    private static final int RANKING_LIMIT = 10;
    private static final int MAX_RANGE_DAYS = 366;
    private static final int MAX_METADATA_BYTES = 4096;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Set<String> SENSITIVE_KEY_PARTS = Set.of(
            "email", "senha", "password", "token", "secret", "cpf", "phone", "telefone", "address", "endereco",
            "username", "user_name", "full_name", "nome_usuario"
    );

    private final AppEventRepository events;
    private final AnalyticsQueryRepository queries;
    private final UsuarioRepository usuarios;
    private final DisciplinaRepository disciplinas;
    private final AssuntoRepository assuntos;
    private final SubAssuntoRepository subassuntos;

    public AnalyticsService(
            AppEventRepository events,
            AnalyticsQueryRepository queries,
            UsuarioRepository usuarios,
            DisciplinaRepository disciplinas,
            AssuntoRepository assuntos,
            SubAssuntoRepository subassuntos
    ) {
        this.events = events;
        this.queries = queries;
        this.usuarios = usuarios;
        this.disciplinas = disciplinas;
        this.assuntos = assuntos;
        this.subassuntos = subassuntos;
    }

    @Transactional
    public AnalyticsEventResponse register(AnalyticsEventRequest request, Authentication authentication) {
        Usuario usuario = authenticatedUser(authentication);
        String anonymousId = clean(request.anonymousId());
        String deviceId = clean(request.deviceId());
        if (anonymousId == null) anonymousId = deviceId;
        if (usuario == null && anonymousId == null) {
            throw badRequest("anonymousId é obrigatório quando não há usuário autenticado");
        }
        String eventName = AnalyticsEventName.normalize(request.eventName());
        String sessionId = clean(request.sessionId());
        if (AnalyticsEventName.requiresSession(eventName) && sessionId == null) {
            throw badRequest("sessionId é obrigatório para eventos de uso");
        }

        Dimensions dimensions = resolveDimensions(request.disciplinaId(), request.assuntoId(), request.subassuntoId());
        JsonNode metadata = safeMetadata(request.metadata());

        AppEvent event = new AppEvent();
        event.setUsuario(usuario);
        event.setDeviceId(deviceId);
        event.setAnonymousId(anonymousId);
        event.setSessionId(sessionId);
        event.setEventName(eventName);
        event.setScreenName(clean(request.screenName()));
        event.setFilterName(clean(request.filterName()));
        event.setQuestionId(clean(request.questionId()));
        event.setAnswerCorrect(request.answerCorrect());
        event.setDisciplinaId(dimensions.disciplinaId());
        event.setAssuntoId(dimensions.assuntoId());
        event.setSubassuntoId(dimensions.subassuntoId());
        event.setInteractionDurationMs(request.interactionDurationMs());
        event.setAppVersion(clean(request.appVersion()));
        event.setPlatform(clean(request.platform()));
        event.setOsVersion(clean(request.osVersion()));
        event.setEventSchemaVersion(request.eventSchemaVersion() == null ? 1 : request.eventSchemaVersion());
        event.setBancaId(request.bancaId());
        event.setInstituicaoId(request.instituicaoId());
        event.setProvaId(request.provaId());
        event.setMetadata(metadata);

        AppEvent saved = events.save(event);
        return new AnalyticsEventResponse(saved.getId(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public AnalyticsDashboardResponse dashboard(
            String period,
            LocalDate startDate,
            LocalDate endDate,
            Long disciplinaId,
            Long assuntoId,
            Long subassuntoId,
            Long bancaId,
            Long instituicaoId,
            Long provaId
    ) {
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);
        DateRange range = resolveRange(period, startDate, endDate, now);
        OffsetDateTime resolvedFrom = range.from();
        OffsetDateTime resolvedTo = range.to();
        validateRange(resolvedFrom, resolvedTo);
        Dimensions dimensions = resolveDimensions(disciplinaId, assuntoId, subassuntoId);

        AnalyticsFilter selected = filter(resolvedFrom, resolvedTo, dimensions, bancaId, instituicaoId, provaId);
        OffsetDateTime todayStart = LocalDate.now(APP_ZONE).atStartOfDay(APP_ZONE).toOffsetDateTime();
        AnalyticsFilter today = filter(todayStart, now.plusNanos(1), dimensions, bancaId, instituicaoId, provaId);
        AnalyticsFilter online = filter(now.minusMinutes(ONLINE_WINDOW_MINUTES), now.plusNanos(1), dimensions, bancaId, instituicaoId, provaId);
        AnalyticsFilter last7 = filter(now.minusDays(7), now.plusNanos(1), dimensions, bancaId, instituicaoId, provaId);
        AnalyticsFilter last24 = filter(now.minusHours(24), now.plusNanos(1), dimensions, bancaId, instituicaoId, provaId);
        long active = queries.countActive(selected);
        long opened = queries.countActiveEvent("app_opened", selected);
        long answered = queries.countActiveEvent("question_answered", selected);
        long sessions = queries.countSessions(selected);

        return new AnalyticsDashboardResponse(
                resolvedFrom,
                resolvedTo,
                ONLINE_WINDOW_MINUTES,
                new AnalyticsDashboardResponse.Overview(queries.countActive(today), active, queries.countRealActive(selected),
                        queries.countActive(online), sessions, queries.averageSessionSeconds(selected),
                        queries.countEvent("question_answered", today), queries.countEvent("question_answered", selected),
                        queries.averageAccuracy(selected), queries.countDevices(), queries.countIdentified(selected)),
                new AnalyticsDashboardResponse.Activation(queries.countNewIdentities(selected), opened,
                        queries.countActiveEvent("question_viewed", selected), answered,
                        percent(answered, opened), queries.averageMinutesToFirstAnswer(selected)),
                new AnalyticsDashboardResponse.Engagement(ratio(queries.countEvent("question_answered", selected), active),
                        ratio(sessions, active), queries.identitiesWithAtLeastQuestions(selected, 10),
                        queries.identitiesWithAtLeastQuestions(selected, 50), queries.countRealActive(last7), queries.inactiveUsers(now)),
                new AnalyticsDashboardResponse.Retention(queries.retention(1, selected), queries.retention(7, selected),
                        queries.retention(30, selected), "coorte pela primeira atividade; retorno exato no dia D+N"),
                new AnalyticsDashboardResponse.Content(queries.topScreens(selected, RANKING_LIMIT), queries.topFilters(selected, RANKING_LIMIT),
                        queries.dimension("disciplinas", "disciplina_id", "discipline_opened", selected, RANKING_LIMIT), queries.dimension("disciplinas", "disciplina_id", "question_answered", selected, RANKING_LIMIT),
                        queries.dimension("assuntos", "assunto_id", "subject_opened", selected, RANKING_LIMIT), queries.dimension("assuntos", "assunto_id", "question_answered", selected, RANKING_LIMIT),
                        queries.dimension("subassuntos", "subassunto_id", "subsubject_opened", selected, RANKING_LIMIT), queries.dimension("subassuntos", "subassunto_id", "question_answered", selected, RANKING_LIMIT),
                        queries.questionRanking(selected, false, RANKING_LIMIT), queries.questionRanking(selected, true, RANKING_LIMIT),
                        queries.countMetadataBoolean("filter_applied", "has_results", selected, false), queries.countMetadataBoolean("search_performed", "has_results", selected, false)),
                new AnalyticsDashboardResponse.DataQuality(queries.countEvent(null, last24), queries.lastEventAt(), queries.eventsByVersion(selected),
                        queries.countUnknown(selected, AnalyticsEventName.OFFICIAL), queries.missingPercent(selected, "e.session_id IS NULL"),
                        queries.missingPercent(selected, "e.user_id IS NULL AND NULLIF(e.anonymous_id,'') IS NULL AND NULLIF(e.device_id,'') IS NULL"), queries.recentErrors(selected, RANKING_LIMIT)),
                queries.dailyTrend(selected)
        );
    }

    private AnalyticsFilter filter(OffsetDateTime from, OffsetDateTime to, Dimensions dimensions, Long bancaId, Long instituicaoId, Long provaId) {
        return new AnalyticsFilter(from, to, dimensions.disciplinaId(), dimensions.assuntoId(), dimensions.subassuntoId(), bancaId, instituicaoId, provaId);
    }

    private DateRange resolveRange(String period, LocalDate start, LocalDate end, OffsetDateTime now) {
        String value = period == null ? "7d" : period;
        LocalDate today = now.toLocalDate();
        if ("custom".equals(value)) {
            if (start == null || end == null || end.isBefore(start)) throw badRequest("startDate e endDate válidos são obrigatórios");
            return new DateRange(start.atStartOfDay(APP_ZONE).toOffsetDateTime(), end.plusDays(1).atStartOfDay(APP_ZONE).toOffsetDateTime());
        }
        OffsetDateTime from = switch (value) {
            case "today" -> today.atStartOfDay(APP_ZONE).toOffsetDateTime();
            case "7d" -> today.minusDays(6).atStartOfDay(APP_ZONE).toOffsetDateTime();
            case "30d" -> today.minusDays(29).atStartOfDay(APP_ZONE).toOffsetDateTime();
            case "current_month" -> today.withDayOfMonth(1).atStartOfDay(APP_ZONE).toOffsetDateTime();
            default -> throw badRequest("period deve ser today, 7d, 30d, current_month ou custom");
        };
        return new DateRange(from, now.plusNanos(1));
    }

    private double ratio(long numerator, long denominator) { return denominator == 0 ? 0 : (double) numerator / denominator; }
    private double percent(long numerator, long denominator) { return ratio(numerator, denominator) * 100; }

    private void validateRange(OffsetDateTime from, OffsetDateTime to) {
        if (!from.isBefore(to)) throw badRequest("from deve ser anterior a to");
        if (Duration.between(from, to).compareTo(Duration.ofDays(MAX_RANGE_DAYS)) > 0) {
            throw badRequest("o período máximo é de 366 dias");
        }
        if (to.isAfter(OffsetDateTime.now(APP_ZONE).plusMinutes(5))) {
            throw badRequest("to não pode estar no futuro");
        }
    }

    private Dimensions resolveDimensions(Long disciplinaId, Long assuntoId, Long subassuntoId) {
        if (subassuntoId != null) {
            SubAssunto subassunto = subassuntos.findById(subassuntoId)
                    .orElseThrow(() -> badRequest("subassuntoId inválido"));
            Assunto assunto = subassunto.getAssunto();
            Long derivedAssunto = assunto.getId();
            Long derivedDisciplina = assunto.getDisciplina().getId();
            requireCompatible("assuntoId", assuntoId, derivedAssunto);
            requireCompatible("disciplinaId", disciplinaId, derivedDisciplina);
            return new Dimensions(derivedDisciplina, derivedAssunto, subassuntoId);
        }
        if (assuntoId != null) {
            Assunto assunto = assuntos.findById(assuntoId)
                    .orElseThrow(() -> badRequest("assuntoId inválido"));
            Long derivedDisciplina = assunto.getDisciplina().getId();
            requireCompatible("disciplinaId", disciplinaId, derivedDisciplina);
            return new Dimensions(derivedDisciplina, assuntoId, null);
        }
        if (disciplinaId != null && !disciplinas.existsById(disciplinaId)) {
            throw badRequest("disciplinaId inválido");
        }
        return new Dimensions(disciplinaId, null, null);
    }

    private void requireCompatible(String field, Long supplied, Long expected) {
        if (supplied != null && !supplied.equals(expected)) {
            throw badRequest(field + " não pertence à hierarquia informada");
        }
    }

    private JsonNode safeMetadata(Map<String, Object> metadata) {
        JsonNode node = OBJECT_MAPPER.valueToTree(metadata == null ? Map.of() : metadata);
        validateMetadataKeys(node);
        try {
            if (OBJECT_MAPPER.writeValueAsBytes(node).length > MAX_METADATA_BYTES) {
                throw badRequest("metadata deve ter no máximo 4096 bytes");
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw badRequest("metadata inválido");
        }
        return node;
    }

    private void validateMetadataKeys(JsonNode node) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey().toLowerCase(Locale.ROOT);
                boolean ipAddress = key.equals("ip") || key.equals("ip_address") || key.equals("ipaddress") || key.endsWith("_ip");
                if (ipAddress || SENSITIVE_KEY_PARTS.stream().anyMatch(key::contains)) {
                    throw badRequest("metadata contém campo sensível não permitido: " + entry.getKey());
                }
                validateMetadataKeys(entry.getValue());
            });
        } else if (node.isArray()) {
            node.forEach(this::validateMetadataKeys);
        }
    }

    private Usuario authenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        if (!(authentication.getPrincipal() instanceof String email) || "anonymousUser".equals(email)) return null;
        return usuarios.findByEmail(email).orElse(null);
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private record Dimensions(Long disciplinaId, Long assuntoId, Long subassuntoId) {}
    private record DateRange(OffsetDateTime from, OffsetDateTime to) {}
}
