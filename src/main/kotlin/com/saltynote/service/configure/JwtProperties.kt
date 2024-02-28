package com.saltynote.service.configure

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "jwt")
class JwtProperties {

    lateinit var accessToken: TokenConf
    lateinit var refreshToken: TokenConf

    class TokenConf {
        var ttl: Long = 0
        lateinit var secret: String
    }
}