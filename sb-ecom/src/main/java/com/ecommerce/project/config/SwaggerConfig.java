package com.ecommerce.project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT bearer token");

        SecurityScheme cookieScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("SpringBootEcom")
                .description("JWT cookie");

        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot Ecommerce API")
                        .version("1.0")
                        .description("This is a Spring Boot App for ecommerce")
                        .license(new License().name("Apache 2.0")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerScheme)
                        .addSecuritySchemes("cookieAuth", cookieScheme))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"));
    }
}
