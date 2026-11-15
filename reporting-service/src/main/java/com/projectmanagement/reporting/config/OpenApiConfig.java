package com.projectmanagement.reporting.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reportingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reporting Service API")
                        .version("1.0.0")
                        .description("REST API сервиса статистической отчётности и аналитики проектов")
                        .contact(new Contact()
                                .name("Project Management System")));
    }
}