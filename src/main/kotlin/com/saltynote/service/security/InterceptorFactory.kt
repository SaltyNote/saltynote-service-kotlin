package com.saltyee.golink.security

import com.saltyee.golink.interceptor.SignInCheckInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class InterceptorFactory(val signInCheckInterceptor: SignInCheckInterceptor) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(signInCheckInterceptor)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
    }
}
