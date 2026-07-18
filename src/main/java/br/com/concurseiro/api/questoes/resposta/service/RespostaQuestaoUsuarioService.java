package br.com.concurseiro.api.questoes.resposta.service;

import br.com.concurseiro.api.questoes.model.Questao;
import br.com.concurseiro.api.questoes.resposta.dto.RespostaQuestaoRequest;
import br.com.concurseiro.api.questoes.resposta.dto.RespostaQuestaoResponse;
import br.com.concurseiro.api.questoes.resposta.model.RespostaQuestaoUsuario;
import br.com.concurseiro.api.questoes.resposta.repository.RespostaQuestaoUsuarioRepository;
import br.com.concurseiro.api.questoes.service.QuestaoService;
import br.com.concurseiro.api.questoes.service.QuestaoValidationHelper;
import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RespostaQuestaoUsuarioService {

    private final RespostaQuestaoUsuarioRepository repository;
    private final QuestaoService questaoService;
    private final UsuarioRepository usuarioRepository;

    public RespostaQuestaoUsuarioService(
            RespostaQuestaoUsuarioRepository repository,
            QuestaoService questaoService,
            UsuarioRepository usuarioRepository
    ) {
        this.repository = repository;
        this.questaoService = questaoService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public RespostaQuestaoResponse responder(String emailUsuario, String idQuestion, RespostaQuestaoRequest request) {
        Usuario usuario = buscarUsuario(emailUsuario);
        Questao questao = questaoService.buscarPorIdQuestion(idQuestion);
        String respostaNormalizada = QuestaoValidationHelper.normalizarRespostaSelecionada(
                questao.getModalidade(),
                request.respostaSelecionada()
        );
        String gabarito = questao.getGabarito();

        RespostaQuestaoUsuario resposta = new RespostaQuestaoUsuario();
        resposta.setUsuario(usuario);
        resposta.setQuestao(questao);
        resposta.setIdQuestion(questao.getIdQuestion());
        resposta.setDisciplina(questao.getDisciplina() == null ? "" : questao.getDisciplina());
        resposta.setRespostaSelecionada(respostaNormalizada);
        resposta.setGabarito(gabarito);
        resposta.setAcertou(respostaNormalizada.equals(gabarito));

        return RespostaQuestaoResponse.fromEntity(repository.save(resposta), questao.getExplicacao());
    }

    @Transactional(readOnly = true)
    public RespostaQuestaoResponse buscarUltima(String emailUsuario, String idQuestion) {
        Usuario usuario = buscarUsuario(emailUsuario);
        Questao questao = questaoService.buscarPorIdQuestion(idQuestion);

        return repository.findFirstByUsuarioAndQuestaoOrderByRespondidaEmDesc(usuario, questao)
                .map(resposta -> RespostaQuestaoResponse.fromEntity(resposta, questao.getExplicacao()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resposta anterior não encontrada"));
    }

    private Usuario buscarUsuario(String emailUsuario) {
        return usuarioRepository.findByEmail(emailUsuario)
                .filter(usuario -> usuario.getStatus() == Usuario.Status.ATIVO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado"));
    }

}
