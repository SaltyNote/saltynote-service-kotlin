package com.saltynote.service.utils

import com.saltynote.service.exception.IllegalInitialException
import com.saltynote.service.utils.BaseUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.InvocationTargetException

internal class BaseUtilsTest {
    @Test
    fun exceptionWhenInit() {
        val exception: Exception = assertThrows(InvocationTargetException::class.java) {
            val pcc = BaseUtils::class.java.getDeclaredConstructor()
            pcc.setAccessible(true)
            val baseUtils = pcc.newInstance()
            println(baseUtils.toString())
        }
        assertTrue(exception.cause is IllegalInitialException)
        assertThat(exception.cause!!.message).isEqualTo("Do not instantiate me.")
    }

    @Test
    fun testSetBaseUrl() {
        BaseUtils.setBaseUrl("http://baseUrl")
        assertTrue(BaseUtils.getPasswordResetUrl("secret").startsWith("http://baseUrl"))
    }

    @Test
    fun testGetPasswordResetUrl() {
        BaseUtils.setBaseUrl("http://baseUrl")
        assertThat(BaseUtils.getPasswordResetUrl("secret")).isEqualTo("http://baseUrl/password/reset?token=secret")
    }

    @Test
    fun testContainsAllIgnoreCase() {
        assertThat(BaseUtils.containsAllIgnoreCase("", listOf("value"))).isFalse()
        assertThat(BaseUtils.containsAllIgnoreCase("src", listOf("value"))).isFalse()
        assertThat(BaseUtils.containsAllIgnoreCase("src is true", listOf("src", "is"))).isTrue()
    }
}
