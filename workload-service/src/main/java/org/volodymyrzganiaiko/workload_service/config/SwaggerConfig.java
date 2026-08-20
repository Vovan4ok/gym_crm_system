package org.volodymyrzganiaiko.workload_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(apiDetails())
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    private Info apiDetails() {
        return new Info()
                .title("Trainer Workload API")
                .description("Sample REST API Documentation using springdoc-openapi")
                .version("1.0.0")
                .termsOfService("Terms of Service URL")
                .contact(new Contact()
                        .name("Volodymyr Zganiaiko")
                        .url("some.url.com")
                        .email("vladimirzganiaiko@gmail.com"))
                .license(new License()
                        .name("API License")
                        .url("LICENSE URL"));
    }
}
