package com.caelum.chronos.shared.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

        @Bean
        OpenAPI chronosOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Chronos API")
                                                .description("Documentação interativa da API Chronos")
                                                .version("v1.0.0"))
                                .addSecurityItem(new SecurityRequirement()
                                                .addList("cookieAuth"))
                                .components(new Components()
                                                .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                                                .name("access_token")
                                                                .type(SecurityScheme.Type.APIKEY)
                                                                .in(SecurityScheme.In.COOKIE)
                                                                .description("Autenticação JWT através de cookie.")));
        }
}
