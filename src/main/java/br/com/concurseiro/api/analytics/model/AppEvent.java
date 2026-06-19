package br.com.concurseiro.api.analytics.model;

import br.com.concurseiro.api.usuarios.model.Usuario;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "app_events")
public class AppEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Usuario usuario;

    @Column(name = "device_id", length = 160)
    private String deviceId;

    @Column(name = "session_id", length = 160)
    private String sessionId;

    @Column(name = "event_name", nullable = false, length = 80)
    private String eventName;

    @Column(name = "screen_name", length = 120)
    private String screenName;

    @Column(name = "filter_name", length = 120)
    private String filterName;

    @Column(name = "question_id", length = 40)
    private String questionId;

    @Column(name = "answer_correct")
    private Boolean answerCorrect;

    @Column(name = "disciplina_id")
    private Long disciplinaId;

    @Column(name = "assunto_id")
    private Long assuntoId;

    @Column(name = "subassunto_id")
    private Long subassuntoId;

    @Column(name = "interaction_duration_ms")
    private Long interactionDurationMs;

    @Column(name = "app_version", length = 40)
    private String appVersion;

    @Column(name = "platform", length = 40)
    private String platform;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    private JsonNode metadata;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }
    public String getFilterName() { return filterName; }
    public void setFilterName(String filterName) { this.filterName = filterName; }
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public Boolean getAnswerCorrect() { return answerCorrect; }
    public void setAnswerCorrect(Boolean answerCorrect) { this.answerCorrect = answerCorrect; }
    public Long getDisciplinaId() { return disciplinaId; }
    public void setDisciplinaId(Long disciplinaId) { this.disciplinaId = disciplinaId; }
    public Long getAssuntoId() { return assuntoId; }
    public void setAssuntoId(Long assuntoId) { this.assuntoId = assuntoId; }
    public Long getSubassuntoId() { return subassuntoId; }
    public void setSubassuntoId(Long subassuntoId) { this.subassuntoId = subassuntoId; }
    public Long getInteractionDurationMs() { return interactionDurationMs; }
    public void setInteractionDurationMs(Long interactionDurationMs) { this.interactionDurationMs = interactionDurationMs; }
    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public JsonNode getMetadata() { return metadata; }
    public void setMetadata(JsonNode metadata) { this.metadata = metadata; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
