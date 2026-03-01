package br.com.concurseiro.api.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Concurseiro API")
                        .version("1.0.0")
                        .description("API para gerenciamento e consulta de questões de concursos públicos brasileiros. "
                                + "Permite cadastro, busca avançada com filtros, e gestão de catálogos de disciplinas, assuntos, bancas e instituições.")
                        .contact(new Contact()
                                .name("Concurseiro Dev")
                                .url("https://concurseiro.dev")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtido via POST /api/v1/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
