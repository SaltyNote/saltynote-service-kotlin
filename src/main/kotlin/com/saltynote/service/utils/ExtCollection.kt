package com.saltynote.service.utils

import com.auth0.jwt.interfaces.DecodedJWT


fun DecodedJWT.getUserId(): Long = this.subject.toLong()