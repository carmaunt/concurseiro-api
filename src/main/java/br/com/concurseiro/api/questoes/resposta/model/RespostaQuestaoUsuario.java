package br.com.concurseiro.api.questoes.resposta.model;

import br.com.concurseiro.api.questoes.model.Questao;
import br.com.concurseiro.api.usuarios.model.Usuario;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "respostas_questoes_usuario",
        indexes = {
                @Index(name = "idx_respostas_usuario_respondida", columnList = "usuario_id,respondida_em"),
                @Index(name = "idx_respostas_questao_usuario", columnList = "questao_id,usuario_id"),
                @Index(name = "idx_respostas_disciplina", columnList = "disciplina")
        }
)
public class RespostaQuestaoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @Column(name = "id_question", nullable = false, length = 16)
    private String idQuestion;

    @Column(nullable = false, length = 160)
    private String disciplina;

    @Column(name = "resposta_selecionada", nullable = false, length = 20)
    private String respostaSelecionada;

    @Column(nullable = false, length = 8)
    private String gabarito;

    @Column(nullable = false)
    private boolean acertou;

    @Column(name = "respondida_em", nullable = false)
    private OffsetDateTime respondidaEm = OffsetDateTime.now();

    public Long getId() { return id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Questao getQuestao() { return questao; }
    public void setQuestao(Questao questao) { this.questao = questao; }

    public String getIdQuestion() { return idQuestion; }
    public void setIdQuestion(String idQuestion) { this.idQuestion = idQuestion; }

    public String getDisciplina() { return disciplina; }
    public void setDisciplina(String disciplina) { this.disciplina = disciplina; }

    public String getRespostaSelecionada() { return respostaSelecionada; }
    public void setRespostaSelecionada(String respostaSelecionada) { this.respostaSelecionada = respostaSelecionada; }

    public String getGabarito() { return gabarito; }
    public void setGabarito(String gabarito) { this.gabarito = gabarito; }

    public boolean isAcertou() { return acertou; }
    public void setAcertou(boolean acertou) { this.acertou = acertou; }

    public OffsetDateTime getRespondidaEm() { return respondidaEm; }
    public void setRespondidaEm(OffsetDateTime respondidaEm) { this.respondidaEm = respondidaEm; }
}
