package com.neobank.backend.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI neoBankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NeoBank Platform API")
                        .description(
                            "REST API for NeoBank — " +
                            "PMIS Internship · Infosys Bhubaneswar DC")
                        .version("Sprint 1 · v1.0")
                        .contact(new Contact()
                                .name("NeoBank Dev Team")
                                .email("dev@neobank.in")))
                .addSecurityItem(
                    new SecurityRequirement().addList("Bearer Auth"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Auth",
                                new SecurityScheme()
                                        .name("Bearer Auth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}