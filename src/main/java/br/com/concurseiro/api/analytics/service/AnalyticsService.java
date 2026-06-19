package br.com.concurseiro.api.analytics.service;

import br.com.concurseiro.api.analytics.dto.*;
import br.com.concurseiro.api.analytics.model.AppEvent;
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
        String deviceId = clean(request.deviceId());
        if (usuario == null && deviceId == null) {
            throw badRequest("deviceId é obrigatório quando não há usuário autenticado");
        }

        Dimensions dimensions = resolveDimensions(request.disciplinaId(), request.assuntoId(), request.subassuntoId());
        JsonNode metadata = safeMetadata(request.metadata());

        AppEvent event = new AppEvent();
        event.setUsuario(usuario);
        event.setDeviceId(deviceId);
        event.setSessionId(clean(request.sessionId()));
        event.setEventName(request.eventName());
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
        event.setMetadata(metadata);

        AppEvent saved = events.save(event);
        return new AnalyticsEventResponse(saved.getId(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public AnalyticsDashboardResponse dashboard(
            OffsetDateTime from,
            OffsetDateTime to,
            Long disciplinaId,
            Long assuntoId,
            Long subassuntoId
    ) {
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);
        OffsetDateTime resolvedTo = to == null ? now : to;
        OffsetDateTime resolvedFrom = from == null ? resolvedTo.minusDays(7) : from;
        validateRange(resolvedFrom, resolvedTo);
        Dimensions dimensions = resolveDimensions(disciplinaId, assuntoId, subassuntoId);

        AnalyticsFilter period = filter(resolvedFrom, resolvedTo, dimensions);
        OffsetDateTime todayStart = LocalDate.now(APP_ZONE).atStartOfDay(APP_ZONE).toOffsetDateTime();
        AnalyticsFilter today = filter(todayStart, now.plusNanos(1), dimensions);
        AnalyticsFilter online = filter(now.minusMinutes(ONLINE_WINDOW_MINUTES), now.plusNanos(1), dimensions);

        AnalyticsSummaryResponse summary = new AnalyticsSummaryResponse(
                queries.countDevices(),
                queries.countActive(today),
                queries.countActive(period),
                queries.countActive(online),
                queries.countEvent("question_answered", today),
                queries.countEvent("question_answered", period),
                queries.averageInteractionSeconds(period)
        );

        return new AnalyticsDashboardResponse(
                resolvedFrom,
                resolvedTo,
                ONLINE_WINDOW_MINUTES,
                summary,
                queries.topScreens(period, RANKING_LIMIT),
                queries.topFilters(period, RANKING_LIMIT),
                queries.topDisciplinas(period, RANKING_LIMIT),
                queries.topAssuntos(period, RANKING_LIMIT),
                queries.topSubassuntos(period, RANKING_LIMIT)
        );
    }

    private AnalyticsFilter filter(OffsetDateTime from, OffsetDateTime to, Dimensions dimensions) {
        return new AnalyticsFilter(from, to, dimensions.disciplinaId(), dimensions.assuntoId(), dimensions.subassuntoId());
    }

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
}
