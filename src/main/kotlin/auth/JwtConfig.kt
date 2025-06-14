package com.studentify.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.studentify.model.Role
import io.ktor.server.application.*
import java.util.*

object JwtConfig {
    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var audience: String
    private const val VALIDITYINMS = 36_000_00 * 24 * 7 // 7 дней

    fun init(environment: ApplicationEnvironment) {
        secret = environment.config.property("jwt.secret").getString()
        issuer = environment.config.property("jwt.issuer").getString()
        audience = environment.config.property("jwt.audience").getString()
    }

    fun generateToken(userId: Int, role: Role): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("role", role.name)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITYINMS))
            .sign(Algorithm.HMAC256(secret))
    }
}