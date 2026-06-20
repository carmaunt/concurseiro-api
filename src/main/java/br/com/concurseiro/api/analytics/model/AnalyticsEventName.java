package br.com.concurseiro.api.analytics.model;

import java.util.Map;
import java.util.Set;

public final class AnalyticsEventName {
    private AnalyticsEventName() {}

    public static final Set<String> OFFICIAL = Set.of(
            "app_opened", "session_started", "session_ended", "screen_viewed",
            "question_viewed", "question_answered", "explanation_viewed", "filter_applied",
            "search_performed", "empty_result_viewed", "discipline_opened", "subject_opened",
            "subsubject_opened", "comment_viewed", "comment_created", "error_occurred"
    );
    private static final Map<String, String> LEGACY = Map.of(
            "app_open", "app_opened", "session_start", "session_started",
            "session_end", "session_ended", "screen_view", "screen_viewed",
            "app_error", "error_occurred"
    );

    public static String normalize(String value) { return LEGACY.getOrDefault(value, value); }
    public static boolean requiresSession(String value) {
        return !Set.of("app_opened", "error_occurred").contains(value);
    }
}
