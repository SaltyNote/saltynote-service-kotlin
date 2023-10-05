package com.saltynote.service.security

import cn.dev33.satoken.interceptor.SaInterceptor
import cn.dev33.satoken.jwt.StpLogicJwtForStateless
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class SaTokenConfigure : WebMvcConfigurer {
    @Bean
    fun getStpLogicJwt(): StpLogicJwtForStateless {
        return StpLogicJwtForStateless()
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
        registry.addInterceptor(SaInterceptor()).addPathPatterns("/**")
    }
}