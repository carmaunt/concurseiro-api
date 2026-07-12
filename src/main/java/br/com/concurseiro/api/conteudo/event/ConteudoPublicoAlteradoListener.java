package br.com.concurseiro.api.conteudo.event;

import br.com.concurseiro.api.infra.portal.PortalRevalidationClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ConteudoPublicoAlteradoListener {
    private final PortalRevalidationClient client;
    public ConteudoPublicoAlteradoListener(PortalRevalidationClient client) { this.client = client; }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(ConteudoPublicoAlteradoEvent event) { client.revalidar(); }
}
