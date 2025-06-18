package com.it.shop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI swaggerOpenApi(){
        return new OpenAPI()
                .info(new Info().title("Shop Demo APIs")
                        .version("V1.0")
                        .description("API list for Shop Buyer Demo"));
    }

}

