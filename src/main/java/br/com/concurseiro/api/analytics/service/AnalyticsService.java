package br.com.concurseiro.api.analytics.service;

import br.com.concurseiro.api.analytics.dto.*;
import br.com.concurseiro.api.analytics.model.AppEvent;
import br.com.concurseiro.api.analytics.repository.AnalyticsRankingProjection;
import br.com.concurseiro.api.analytics.repository.AppEventRepository;
import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class AnalyticsService {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Bahia");
    private static final int DEFAULT_RANKING_LIMIT = 8;
    private static final int ONLINE_WINDOW_MINUTES = 5;

    private final AppEventRepository appEventRepository;
    private final UsuarioRepository usuarioRepository;

    public AnalyticsService(AppEventRepository appEventRepository, UsuarioRepository usuarioRepository) {
        this.appEventRepository = appEventRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public AnalyticsEventResponse registrarEvento(AnalyticsEventRequest request, Authentication authentication) {
        Usuario usuario = usuarioAutenticado(authentication);

        if (usuario == null && isBlank(request.deviceId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deviceId é obrigatório para eventos sem usuário autenticado");
        }

        AppEvent event = new AppEvent();
        event.setUsuario(usuario);
        event.setDeviceId(clean(request.deviceId()));
        event.setSessionId(clean(request.sessionId()));
        event.setEventName(cleanRequired(request.eventName()).toLowerCase());
        event.setScreenName(clean(request.screenName()));
        event.setFilterName(clean(request.filterName()));
        event.setQuestionId(clean(request.questionId()));
        event.setAcertou(request.acertou());
        event.setDisciplinaId(request.disciplinaId());
        event.setDisciplinaNome(clean(request.disciplinaNome()));
        event.setAssuntoId(request.assuntoId());
        event.setAssuntoNome(clean(request.assuntoNome()));
        event.setSubassuntoId(request.subassuntoId());
        event.setSubassuntoNome(clean(request.subassuntoNome()));
        event.setInteractionDurationSeconds(request.interactionDurationSeconds());
        event.setAppVersion(clean(request.appVersion()));
        event.setPlatform(clean(request.platform()));
        event.setMetadataJson(clean(request.metadataJson()));

        AppEvent salvo = appEventRepository.save(event);
        return new AnalyticsEventResponse(salvo.getId());
    }

    @Transactional(readOnly = true)
    public AnalyticsDashboardResponse dashboard(OffsetDateTime from, OffsetDateTime to) {
        OffsetDateTime resolvedTo = to != null ? to : OffsetDateTime.now(APP_ZONE);
        OffsetDateTime resolvedFrom = from != null ? from : resolvedTo.minusDays(7);

        if (!resolvedFrom.isBefore(resolvedTo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from deve ser anterior a to");
        }

        OffsetDateTime todayStart = LocalDate.now(APP_ZONE).atStartOfDay(APP_ZONE).toOffsetDateTime();
        OffsetDateTime tomorrowStart = todayStart.plusDays(1);
        OffsetDateTime onlineSince = OffsetDateTime.now(APP_ZONE).minusMinutes(ONLINE_WINDOW_MINUTES);

        AnalyticsSummaryResponse summary = new AnalyticsSummaryResponse(
                appEventRepository.countDistinctDevices(),
                appEventRepository.countActiveIdentitiesBetween(todayStart, tomorrowStart),
                appEventRepository.countActiveIdentitiesBetween(resolvedFrom, resolvedTo),
                appEventRepository.countOnlineSince(onlineSince),
                appEventRepository.countEventBetween("question_answered", todayStart, tomorrowStart),
                appEventRepository.countEventBetween("question_answered", resolvedFrom, resolvedTo),
                appEventRepository.averageInteractionSecondsBetween(resolvedFrom, resolvedTo)
        );

        return new AnalyticsDashboardResponse(
                resolvedFrom,
                resolvedTo,
                summary,
                mapRanking(appEventRepository.topScreens(resolvedFrom, resolvedTo, DEFAULT_RANKING_LIMIT)),
                mapRanking(appEventRepository.topFilters(resolvedFrom, resolvedTo, DEFAULT_RANKING_LIMIT)),
                mapRanking(appEventRepository.topDisciplinas(resolvedFrom, resolvedTo, DEFAULT_RANKING_LIMIT)),
                mapRanking(appEventRepository.topAssuntos(resolvedFrom, resolvedTo, DEFAULT_RANKING_LIMIT)),
                mapRanking(appEventRepository.topSubassuntos(resolvedFrom, resolvedTo, DEFAULT_RANKING_LIMIT))
        );
    }

    private Usuario usuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof String email) || email.isBlank() || "anonymousUser".equals(email)) return null;
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    private List<AnalyticsRankingItemResponse> mapRanking(List<AnalyticsRankingProjection> projections) {
        return projections.stream()
                .map(item -> new AnalyticsRankingItemResponse(item.getLabel(), item.getTotal() == null ? 0 : item.getTotal()))
                .toList();
    }

    private String clean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String cleanRequired(String value) {
        String cleaned = clean(value);
        if (cleaned == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventName é obrigatório");
        }
        return cleaned;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
