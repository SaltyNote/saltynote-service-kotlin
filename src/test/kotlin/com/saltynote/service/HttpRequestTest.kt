package com.saltynote.service

import com.saltynote.service.domain.transfer.ServiceResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class HttpRequestTest {
    @LocalServerPort
    private val port = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Value("\${app.welcome.message}")
    private val welcomeMessage: String? = null

    @Test
    fun welcomePageShouldOK() {
        val (status, message) = restTemplate!!.getForObject("http://localhost:$port/", ServiceResponse::class.java)
        Assertions.assertEquals(HttpStatus.OK, status)
        Assertions.assertEquals(welcomeMessage, message)
    }
}
