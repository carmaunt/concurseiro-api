package br.com.concurseiro.api.analytics.repository;

import br.com.concurseiro.api.analytics.model.AppEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppEventRepository extends JpaRepository<AppEvent, Long> {}
