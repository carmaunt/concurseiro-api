package br.com.concurseiro.api;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Context test depende de DB externo; unit tests cobrem a lógica")
@SpringBootTest
class ConcurseiroApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
