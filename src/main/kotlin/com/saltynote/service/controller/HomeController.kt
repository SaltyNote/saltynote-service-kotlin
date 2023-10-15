package com.saltynote.service.controller

import com.saltynote.service.domain.transfer.ServiceResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController {
    @Value("\${app.welcome.message}")
    lateinit var welcomeMessage: String

    @GetMapping("/")
    fun home(): ResponseEntity<ServiceResponse> {
        return ResponseEntity.ok(ServiceResponse.ok(welcomeMessage))
    }
}
