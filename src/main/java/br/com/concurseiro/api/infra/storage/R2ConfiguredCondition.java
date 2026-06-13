package br.com.concurseiro.api.infra.storage;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import java.util.List;

public class R2ConfiguredCondition implements Condition {

    private static final List<String> PROPRIEDADES_OBRIGATORIAS = List.of(
            "r2.endpoint",
            "r2.access-key-id",
            "r2.secret-access-key",
            "r2.bucket",
            "r2.public-base-url"
    );

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return PROPRIEDADES_OBRIGATORIAS.stream()
                .map(context.getEnvironment()::getProperty)
                .allMatch(StringUtils::hasText);
    }
}
