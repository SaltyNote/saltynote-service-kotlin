package com.saltynote.service.domain

interface IdentifiableUser : Identifiable {
    fun getUsername(): String?
}
