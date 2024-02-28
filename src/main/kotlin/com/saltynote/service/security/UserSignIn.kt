package com.saltyee.golink.security

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UserSignIn(val required: Boolean = true)
