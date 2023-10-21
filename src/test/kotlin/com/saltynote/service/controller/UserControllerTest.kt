package com.saltynote.service.controller

import cn.dev33.satoken.secure.BCrypt
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.javafaker.Faker
import com.saltynote.service.domain.VaultType
import com.saltynote.service.domain.transfer.*
import com.saltynote.service.entity.Note
import com.saltynote.service.entity.User
import com.saltynote.service.entity.Vault
import com.saltynote.service.security.SecurityConstants
import com.saltynote.service.service.*
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.concurrent.TimeUnit

// Overwrite refresh token ttl to 8 seconds
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = [SecurityAutoConfiguration::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles(value = ["test"])
internal class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var noteService: NoteService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var vaultService: VaultService

    @Autowired
    lateinit var jwtService: JwtService

    @MockBean
    private var emailService: EmailService? = null

    private val faker = Faker()

    @BeforeEach
    fun setup() {
        doNothing().`when`<EmailService>(emailService).sendAsHtml(any(), any(), any())
        doNothing().`when`<EmailService>(emailService).send(any(), any(), any())
    }

    @Test
    fun emailVerifyTest() {
        val username: String = faker.name().username()
        val emailStr = getEmail(username)
        val alreadyUsedEmail = "example@exmaple.com"
        var user =
            User(username = faker.name().username(), email = alreadyUsedEmail, password = BCrypt.hashpw(RandomStringUtils.randomAlphanumeric(12)))
        user = userService.create(user)
        assertNotNull(user.getId())
        mockMvc
            .perform(
                post("/email/verification").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Payload(alreadyUsedEmail)))
            )
            .andExpect(status().isBadRequest())
        mockMvc
            .perform(
                post("/email/verification").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Payload(emailStr)))
            )
            .andExpect(status().isOk())
        val vaults: List<Vault> = vaultService.getByEmail(emailStr)
        assertEquals(1, vaults.size)
        vaultService.deleteById(vaults[0].getId())
        userService.cleanupByUserId(user.getId())
    }

    @Test
    fun signupShouldFailIfNoToken() {
        val username: String = faker.name().username()
        val email = getEmail(username)
        val userNewRequest = UserNewRequest(email = email, password = RandomStringUtils.randomAlphanumeric(12), username = username, token = "")
        assertEquals(userNewRequest.token, "")
        mockMvc
            .perform(
                post("/signup").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userNewRequest))
            )
            .andExpect(status().isForbidden())
    }

    @Test
    fun signupShouldReturnSuccess() {
        val username: String = faker.name().username()
        val email = getEmail(username)
        val vault: Vault = vaultService.createVerificationCode(email)
        assertNotNull(vault.getId())
        assertEquals(vault.email, email)
        val userNewRequest =
            UserNewRequest(email = email, password = RandomStringUtils.randomAlphanumeric(12), username = username, token = vault.secret)

        mockMvc
            .perform(
                post("/signup").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userNewRequest))
            )
//            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
        val queryUser = userService.getByUsername(userNewRequest.username)
        assertThat(queryUser!!.email).isEqualTo(userNewRequest.email)
        userService.cleanupByUserId(queryUser.getId())
    }

    @Test
    fun loginAndRefreshTokenShouldSuccess() {
        val uc = UserCredential(
            username = faker.name().username(),
            email = faker.internet().emailAddress(),
            password = RandomStringUtils.randomAlphanumeric(12)
        )
        var user: User = uc.toUser()
        user = userService.create(user)
        val userRequest = UserCredential(username = uc.username, password = uc.password, email = uc.email)
        var mvcResult = mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            )
            .andExpect(status().isOk())
            .andReturn()
        var res: String = mvcResult.response.contentAsString
        val token: TokenPair = objectMapper.readValue(res, TokenPair::class.java)
        assertNotNull(token)
        assertNotNull(jwtService.parseToken(token.refreshToken))
        assertNotNull(jwtService.parseToken(token.accessToken))

        // Note: have to sleep 1 second to have different expire time for new access token
        TimeUnit.SECONDS.sleep(1)

        // try refresh token
        val tokenRequest = TokenPair("", token.refreshToken)
        mvcResult = mockMvc
            .perform(
                post("/refresh_token").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tokenRequest))
            )
            .andExpect(status().isOk())
            .andReturn()
        res = mvcResult.response.contentAsString
        val newToken: TokenPair = objectMapper.readValue(res, TokenPair::class.java)
        assertNotNull(newToken.accessToken)
        assertNotNull(jwtService.parseToken(newToken.accessToken))
        assertNotEquals(token.accessToken, newToken.accessToken)
        assertEquals(newToken.refreshToken, token.refreshToken)
        userService.cleanupByUserId(user.getId())
    }

    @Test
    fun loginAndRefreshTokenReUsageShouldSuccess() {
        val uc = UserCredential(
            username = faker.name().username(),
            email = faker.internet().emailAddress(),
            password = RandomStringUtils.randomAlphanumeric(12)
        )
        var user: User = uc.toUser()
        user = userService.create(user)
        val userRequest = UserCredential(username = uc.username, password = uc.password, email = uc.email)
        var mvcResult = mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            )
            .andExpect(status().isOk())
            .andReturn()
        var res: String = mvcResult.response.contentAsString
        var token = objectMapper.readValue(res, TokenPair::class.java)

        assertNotNull(token)
        assertNotNull(jwtService.parseToken(token.refreshToken))
        assertNotNull(jwtService.parseToken(token.accessToken))
        val oldRefreshToken: String = token.refreshToken
        mvcResult = mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            )
            .andExpect(status().isOk())
            .andReturn()
        res = mvcResult.response.contentAsString
        token = objectMapper.readValue(res, TokenPair::class.java)
        assertNotNull(token)
        assertNotNull(jwtService.parseToken(token.refreshToken))
        assertNotNull(jwtService.parseToken(token.accessToken))
        // No new refresh token is generated.
        assertEquals(oldRefreshToken, token.refreshToken)

        // Sleep 2 second, so refresh token will age 20%+, then new refresh token should
        // be generated.
        TimeUnit.SECONDS.sleep(2)
        mvcResult = mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            )
            .andExpect(status().isOk())
            .andReturn()
        res = mvcResult.response.contentAsString
        token = objectMapper.readValue(res, TokenPair::class.java)
        assertNotNull(token)
        assertNotNull(jwtService.parseToken(token.refreshToken))
        assertNotNull(jwtService.parseToken(token.accessToken))
        // New refresh token is generated.
        assertNotEquals(oldRefreshToken, token.refreshToken)
        userService.cleanupByUserId(user.getId())
    }

    @Test
    fun loginShouldFail() {
        val uc = UserCredential(
            username = faker.name().username(),
            email = faker.internet().emailAddress(),
            password = RandomStringUtils.randomAlphanumeric(12)
        )
        var user: User = uc.toUser()
        user = userService.create(user)
        assertNotNull(user.getId())
        val userRequest = UserCredential(username = uc.username, password = uc.password + "not valid", email = uc.email)
        mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            )
            .andExpect(status().isForbidden())
        userService.cleanupByUserId(user.getId())
    }

    @Test
    fun passwordResetTest() {
        // Create a new User
        val uc = UserCredential(
            username = faker.name().username(),
            email = faker.internet().emailAddress(),
            password = RandomStringUtils.randomAlphanumeric(12)
        )
        var user: User = uc.toUser()
        user = userService.create(user)

        // request password change
        val payload = Payload(user.email)
        mockMvc
            .perform(
                post("/password/forget").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload))
            )
            .andExpect(status().isOk())
        val vaults: List<Vault> = vaultService.getByUserIdAndType(user.getId(), VaultType.PASSWORD)
        assertEquals(1, vaults.size)
        val vault: Vault = vaults[0]

        // Can log in without problem
        val userRequest = UserCredential(username = uc.username, password = uc.password, email = uc.email)
        mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            )
            .andExpect(status().isOk())
        val newPassword = RandomStringUtils.randomAlphanumeric(10)
        val pr = PasswordReset(token = vaultService.encode(vault), password = newPassword)

        mockMvc
            .perform(
                post("/password/reset").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(pr))
            )
            .andExpect(status().isOk())

        // login with old password should fail
        mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            )
            .andExpect(status().isForbidden())

        // login with new password should success
        val ur = UserCredential(username = uc.username, password = newPassword, email = uc.email)
        mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(ur))
            )
            .andExpect(status().isOk())
        userService.cleanupByUserId(user.getId())
    }

    @Test
    fun passwordUpdateTest() {
        val oldPassword = RandomStringUtils.randomAlphanumeric(12)
        val newPassword = RandomStringUtils.randomAlphanumeric(12)

        // Create a new User
        val username: String = faker.name().username()
        val email = getEmail(username)
        val vault: Vault = vaultService.createVerificationCode(email)
        assertNotNull(vault.getId())
        assertEquals(vault.email, email)
        val userNewRequest = UserNewRequest(email = email, password = oldPassword, username = username, token = vault.secret)

        var mvcResult = mockMvc
            .perform(
                post("/signup").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userNewRequest))
            )
            .andExpect(status().isOk())
            .andReturn()
        var res: String = mvcResult.response.contentAsString
        val jwtUser = objectMapper.readValue(res, UserDto::class.java)
        assertNotNull(jwtUser.getId())

        // Can login without problem
        mvcResult = mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userNewRequest))
            )
            .andExpect(status().isOk())
            .andReturn()
        res = mvcResult.response.contentAsString
        val token: TokenPair = objectMapper.readValue(res, TokenPair::class.java)
        assertNotNull(token)
        assertNotNull(jwtService.parseToken(token.refreshToken))
        assertNotNull(jwtService.parseToken(token.accessToken))
        val pu = PasswordUpdate(oldPassword = oldPassword, password = newPassword)
        mockMvc
            .perform(
                post("/password").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(pu))
                    .header(SecurityConstants.AUTH_HEADER, token.accessToken)
            )
            .andExpect(status().isOk())

        // login with old password should fail
        mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userNewRequest))
            )
            .andExpect(status().isForbidden())

        // login with new password should success
        val ur: UserCredential = UserCredential(username = userNewRequest.username, password = newPassword, email = userNewRequest.email)
        mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(ur))
            )
            .andExpect(status().isOk())
        userService.cleanupByUserId(jwtUser.getId())
    }

    @Test
    fun accountDeletionTest() {
        // Create a new User
        val username: String = faker.name().username()
        val email = getEmail(username)
        val vault: Vault = vaultService.createVerificationCode(email)
        assertNotNull(vault.getId())
        assertEquals(vault.email, email)
        val user = UserNewRequest(
            email = email,
            username = username,
            password = RandomStringUtils.randomAlphanumeric(12),
            token = vault.secret
        )
        var mvcResult = mockMvc
            .perform(
                post("/signup").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user))
            )
            .andExpect(status().isOk())
            .andReturn()
        var res: String = mvcResult.response.contentAsString
        val jwtUser = objectMapper.readValue(res, UserDto::class.java)
        assertNotNull(jwtUser.getId())

        // Can log in without problem
        mvcResult = mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user))
            )
            .andExpect(status().isOk())
            .andReturn()
        res = mvcResult.response.contentAsString
        val token: TokenPair = objectMapper.readValue(res, TokenPair::class.java)
        assertNotNull(token)
        assertNotNull(jwtService.parseToken(token.refreshToken))
        assertNotNull(jwtService.parseToken(token.accessToken))
        val note = NoteControllerTest.createTmpNote(jwtUser.getId())
        mvcResult = mockMvc
            .perform(
                post("/note").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
                    .header(SecurityConstants.AUTH_HEADER, token.accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(Matchers.containsString(note.text)))
            .andReturn()
        res = mvcResult.response.contentAsString
        val note1 = objectMapper.readValue(res, Note::class.java).note
        assertEquals(note.note, note1)

        // deletion should fail due to invalid user id
        mockMvc
            .perform(
                delete("/account/invalid-id").header(
                    SecurityConstants.AUTH_HEADER,
                    token.accessToken
                )
            )
            .andExpect(status().isInternalServerError())
        // deletion should fail due to missing user id
        mockMvc
            .perform(delete("/account").header(SecurityConstants.AUTH_HEADER, token.accessToken))
            .andExpect(status().isNotFound())

        // deletion should fail due to no access token
        mockMvc.perform(delete("/account/" + jwtUser.getId())).andExpect(status().isForbidden())

        // deletion should succeed
        mockMvc.perform(
            delete("/account/" + jwtUser.getId()).header(
                SecurityConstants.AUTH_HEADER,
                token.accessToken
            )
        )
            .andExpect(status().isOk())
        assertFalse(userService.getById(jwtUser.getId()).isPresent)
        assertTrue(noteService.getAllByUserId(jwtUser.getId()).isEmpty())
        assertTrue(vaultService.getByUserId(jwtUser.getId()).isEmpty())
    }

    private fun getEmail(username: String): String {
        return "$username@saltynote.com"
    }
}
