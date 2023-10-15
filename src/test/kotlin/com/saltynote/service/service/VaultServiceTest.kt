package com.saltynote.service.service

import com.saltynote.service.domain.VaultEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.IOException
import java.util.*

@SpringBootTest
internal class VaultServiceTest {
    @Autowired
    lateinit var vaultService: VaultService

    @Test
    @Throws(IOException::class)
    fun encodeAndDecodeTest() {
        val ve = VaultEntity(secret = "secret", userId = 8888)
        val encoded = vaultService.encode(ve)
        val decoded = vaultService.decode(encoded)
        Assertions.assertNotNull(decoded)
        Assertions.assertEquals(ve, decoded)
    }
}
