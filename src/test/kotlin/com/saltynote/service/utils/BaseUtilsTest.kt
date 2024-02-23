package com.saltynote.service.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BaseUtilsTest {
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
