package org.volodymyrzganiaiko.gym.crm.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(apiDetails());
    }

    private Info apiDetails() {
        return new Info()
                .title("Gym System API")
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