package br.com.concurseiro.api.estudante.service;

import br.com.concurseiro.api.estudante.dto.DashboardEstudanteResponse;
import br.com.concurseiro.api.estudante.dto.UltimaRespostaResponse;
import br.com.concurseiro.api.questoes.resposta.repository.RespostaQuestaoUsuarioRepository;
import br.com.concurseiro.api.usuarios.model.Usuario;
import br.com.concurseiro.api.usuarios.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DashboardEstudanteService {

    private final UsuarioRepository usuarioRepository;
    private final RespostaQuestaoUsuarioRepository respostaRepository;

    public DashboardEstudanteService(UsuarioRepository usuarioRepository, RespostaQuestaoUsuarioRepository respostaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.respostaRepository = respostaRepository;
    }

    @Transactional(readOnly = true)
    public DashboardEstudanteResponse carregar(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .filter(u -> u.getStatus() == Usuario.Status.ATIVO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado"));

        long resolvidas = respostaRepository.countByUsuario(usuario);
        long acertos = respostaRepository.countByUsuarioAndAcertou(usuario, true);
        long erros = respostaRepository.countByUsuarioAndAcertou(usuario, false);
        double aproveitamento = resolvidas == 0 ? 0 : Math.round((acertos * 1000.0 / resolvidas)) / 10.0;

        var ultimas = respostaRepository.findByUsuarioOrderByRespondidaEmDesc(usuario, PageRequest.of(0, 8))
                .stream()
                .map(UltimaRespostaResponse::fromEntity)
                .toList();

        var disciplinas = respostaRepository.desempenhoPorDisciplina(usuario, PageRequest.of(0, 6));

        return new DashboardEstudanteResponse(resolvidas, acertos, erros, aproveitamento, ultimas, disciplinas);
    }
}
