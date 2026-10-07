package com.senla.errorfreetext.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI errorFreeTextOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Error Free Text API")
                        .version("1.0")
                        .description("Asynchronous text correction using Yandex Speller"));
    }
}
