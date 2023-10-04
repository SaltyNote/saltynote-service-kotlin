package com.saltynote.service.configure

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringDocConfig {
    @Bean
    fun saltyNoteOpenAPI(): OpenAPI {
        return OpenAPI().info(
            Info().title("SaltyNote API")
                .description("SpringDoc for SaltyNote API")
                .version("v0.3.0")
                .license(
                    License().name("MIT")
                        .url("https://github.com/SaltyNote/saltynote-service/blob/master/LICENSE")
                )
        )
    }

    @Bean
    fun homeApi(): GroupedOpenApi {
        return GroupedOpenApi.builder().group("HomeController").pathsToMatch("/").build()
    }

    @Bean
    fun userApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("UserController")
            .pathsToMatch(
                "/password/**", "/password", "/signup", "/login", "/email/verification", "/refresh_token",
                "/account/**"
            )
            .build()
    }

    @Bean
    fun noteApi(): GroupedOpenApi {
        return GroupedOpenApi.builder().group("NoteController").pathsToMatch("/note/**", "/notes", "/note").build()
    }
}
