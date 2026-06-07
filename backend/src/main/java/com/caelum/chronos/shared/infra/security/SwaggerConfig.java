package com.caelum.chronos.shared.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

        @Value("${app.keycloak.base-url}")
        private String keycloakBaseUrl;

        @Bean
        OpenAPI chronosOpenAPI() {
                String authUrl = keycloakBaseUrl + "/protocol/openid-connect/auth";
                String tokenUrl = keycloakBaseUrl + "/protocol/openid-connect/token";

                return new OpenAPI()
                                .info(new Info()
                                                .title("Chronos API")
                                                .description("Documentação interativa da API Chronos")
                                                .version("v1.0.0"))
                                .addSecurityItem(new SecurityRequirement()
                                                .addList("cookieAuth")
                                                .addList("keycloakOAuth"))
                                .components(new Components()
                                                .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                                                .name("access_token")
                                                                .type(SecurityScheme.Type.APIKEY)
                                                                .in(SecurityScheme.In.COOKIE)
                                                                .description("Autenticação JWT através de cookie."))
                                                .addSecuritySchemes("keycloakOAuth", new SecurityScheme()
                                                                .type(SecurityScheme.Type.OAUTH2)
                                                                .description("Autenticação via Keycloak")
                                                                .flows(new OAuthFlows()
                                                                                .authorizationCode(new OAuthFlow()
                                                                                                .authorizationUrl(authUrl)
                                                                                                .tokenUrl(tokenUrl)
                                                                                                .scopes(new Scopes()
                                                                                                                .addString("openid", "OpenID login")
                                                                                                                .addString("profile", "User profile info")
                                                                                                                .addString("email", "User email address"))))));
        }
}
