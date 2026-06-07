package com.example.Stq.config;

import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Configuration
public class OpenApiConfig {

    @Bean
    public ParameterCustomizer ocultarPrincipalAutenticado() {
        return (Parameter parameterModel, MethodParameter methodParameter) -> {
            if (methodParameter.hasParameterAnnotation(AuthenticationPrincipal.class)) {
                return null;
            }
            return parameterModel;
        };
    }
}
