package br.com.concurseiro.api.questoes.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;

class QuestaoWebResponseTest {

    @Test
    void contratoWebNaoExpoeGabaritoNemExplicacao() {
        Set<String> campos = Arrays.stream(QuestaoWebResponse.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName)
                .collect(Collectors.toSet());

        assertFalse(campos.contains("gabarito"));
        assertFalse(campos.contains("explicacao"));
    }
}
