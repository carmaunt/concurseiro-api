package br.com.concurseiro.api.analytics.model;

import br.com.concurseiro.api.usuarios.model.Usuario;
import jakarta.persistence.*;

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

    @Column(name = "acertou")
    private Boolean acertou;

    @Column(name = "disciplina_id")
    private Long disciplinaId;

    @Column(name = "disciplina_nome", length = 160)
    private String disciplinaNome;

    @Column(name = "assunto_id")
    private Long assuntoId;

    @Column(name = "assunto_nome", length = 160)
    private String assuntoNome;

    @Column(name = "subassunto_id")
    private Long subassuntoId;

    @Column(name = "subassunto_nome", length = 160)
    private String subassuntoNome;

    @Column(name = "interaction_duration_seconds")
    private Integer interactionDurationSeconds;

    @Column(name = "app_version", length = 40)
    private String appVersion;

    @Column(name = "platform", length = 40)
    private String platform;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

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

    public Boolean getAcertou() { return acertou; }
    public void setAcertou(Boolean acertou) { this.acertou = acertou; }

    public Long getDisciplinaId() { return disciplinaId; }
    public void setDisciplinaId(Long disciplinaId) { this.disciplinaId = disciplinaId; }

    public String getDisciplinaNome() { return disciplinaNome; }
    public void setDisciplinaNome(String disciplinaNome) { this.disciplinaNome = disciplinaNome; }

    public Long getAssuntoId() { return assuntoId; }
    public void setAssuntoId(Long assuntoId) { this.assuntoId = assuntoId; }

    public String getAssuntoNome() { return assuntoNome; }
    public void setAssuntoNome(String assuntoNome) { this.assuntoNome = assuntoNome; }

    public Long getSubassuntoId() { return subassuntoId; }
    public void setSubassuntoId(Long subassuntoId) { this.subassuntoId = subassuntoId; }

    public String getSubassuntoNome() { return subassuntoNome; }
    public void setSubassuntoNome(String subassuntoNome) { this.subassuntoNome = subassuntoNome; }

    public Integer getInteractionDurationSeconds() { return interactionDurationSeconds; }
    public void setInteractionDurationSeconds(Integer interactionDurationSeconds) { this.interactionDurationSeconds = interactionDurationSeconds; }

    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
