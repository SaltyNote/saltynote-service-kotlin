package com.saltynote.service.domain

import java.io.Serializable

fun interface Identifiable : Serializable {
    fun getId(): Long
}
