package com.saltynote.service.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.saltynote.service.domain.transfer.NoteDto
import com.saltynote.service.domain.transfer.TokenPair
import com.saltynote.service.domain.transfer.UserCredential
import com.saltynote.service.domain.transfer.UserNewRequest
import com.saltynote.service.entity.Note
import com.saltynote.service.entity.User
import com.saltynote.service.entity.Vault
import com.saltynote.service.security.SecurityConstants
import com.saltynote.service.service.EmailService
import com.saltynote.service.service.NoteService
import com.saltynote.service.service.UserService
import com.saltynote.service.service.VaultService
import net.datafaker.Faker
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.SerializationUtils
import org.apache.commons.lang3.tuple.Pair
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = [SecurityAutoConfiguration::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles(value = ["test"])
class NoteControllerTest {
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

    @MockBean
    private var emailService: EmailService? = null

    private var notesToCleaned = mutableListOf<Note>()
    private var siteUser = User()
    private var accessToken: String = ""
    private var savedNote: Note = Note(userId = 0, note = "", url = "", text = "")


    private fun signupTestUser(): Pair<User, String> {
        Mockito.doNothing().`when`<EmailService>(emailService).sendAsHtml(any(), any(), any())
        Mockito.doNothing().`when`<EmailService>(emailService).send(any(), any(), any())
        val username: String = faker.name().username()
        val email = "$username@saltynote.com"
        val vault: Vault = vaultService.createVerificationCode(email)
        Assertions.assertNotNull(vault.getId())
        Assertions.assertEquals(vault.email, email)
        val userNewRequest = UserNewRequest(
            email = email,
            username = username,
            password = RandomStringUtils.randomAlphanumeric(12),
            token = vault.secret
        )

        mockMvc
            .perform(
                post("/signup").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userNewRequest))
            )
            .andExpect(status().isOk())
        val siteUser = userService.getByUsername(userNewRequest.username)
        assertThat(siteUser!!.email).isEqualTo(userNewRequest.email)
        val user = UserCredential(username = userNewRequest.username, email = userNewRequest.email, password = userNewRequest.password)
        val mvcLoginResult = mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user))
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
        val res: String = mvcLoginResult.response.contentAsString
        val token = objectMapper.readValue(res, TokenPair::class.java)
        Assertions.assertNotNull(token.accessToken)
        return Pair.of(siteUser, token.accessToken)
    }

    @BeforeEach
    fun setUp() {
        val pair: Pair<User, String> = signupTestUser()
        accessToken = pair.right
        siteUser = pair.left
        notesToCleaned = ArrayList()
        // Create a temp note for current user.
        val note = createTmpNote(siteUser.getId())
        savedNote = noteService.create(Note.from(note))
        notesToCleaned.add(savedNote)
    }

    @AfterEach
    fun tearDown() {
        userService.cleanupByUserId(this.siteUser.getId())
        noteService.deleteAll(this.notesToCleaned)
    }

    @Test
    fun getNoteByIdShouldSuccess() {
        // Suppress codacy warning
        Assertions.assertNotNull(savedNote.getId())
        mockMvc
            .perform(
                get("/note/" + savedNote.getId()).header(
                    SecurityConstants.AUTH_HEADER,
                    accessToken
                )
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(savedNote.text)))
    }


    @Test
    fun getNoteByIdNoAccessTokenReturnException() {
        // Suppress codacy warning
        Assertions.assertNotNull(savedNote.getId())
        mockMvc.perform(get("/note/" + savedNote.getId())).andExpect(status().isForbidden())
    }


    @Test
    fun getNoteByIdFromNonOwnerShouldFail() {
        val pair: Pair<User, String> = signupTestUser()
        Assertions.assertNotNull(pair.right)
        mockMvc
            .perform(
                get("/note/" + savedNote.getId()).header(
                    SecurityConstants.AUTH_HEADER,
                    pair.right
                )
            )
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        userService.cleanupByUserId(pair.left.getId())
    }

    @Test
    fun updateNoteByIdShouldSuccess() {
        val newNoteContent = "I am the new note"
        val noteToUpdate = SerializationUtils.clone(savedNote)
        noteToUpdate!!.note = newNoteContent
        mockMvc
            .perform(
                post("/note/" + savedNote.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(noteToUpdate))
                    .header(SecurityConstants.AUTH_HEADER, accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(newNoteContent)))
        val queryNote: Optional<Note> = noteService.getById(savedNote.getId())
        Assertions.assertTrue(queryNote.isPresent)
        Assertions.assertEquals(queryNote.get().note, newNoteContent)
    }

    @Test
    fun updateTagsByIdShouldSuccess() {
        val newTagsContent = setOf("java", "python", "spring-boot")
        val tagToUpdate = SerializationUtils.clone(savedNote)
        tagToUpdate!!.tags = newTagsContent
        mockMvc
            .perform(
                post("/note/" + savedNote.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tagToUpdate))
                    .header(SecurityConstants.AUTH_HEADER, accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString("python")))
        val queryNote: Optional<Note> = noteService.getById(savedNote.getId())
        Assertions.assertTrue(queryNote.isPresent)
        Assertions.assertEquals(queryNote.get().tags, newTagsContent)
    }

    @Test
    fun updateNoteByIdFromNonOwnerShouldFail() {
        val pair: Pair<User, String> = signupTestUser()
        val newNoteContent = "I am the new note"
        val noteToUpdate = SerializationUtils.clone(savedNote)
        noteToUpdate!!.note = newNoteContent
        mockMvc
            .perform(
                post("/note/" + savedNote.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(noteToUpdate))
                    .header(SecurityConstants.AUTH_HEADER, pair.right)
            )
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        val queryNote: Optional<Note> = noteService.getById(savedNote.getId())
        Assertions.assertTrue(queryNote.isPresent)
        Assertions.assertEquals(queryNote.get().note, savedNote.note)
        userService.cleanupByUserId(pair.left.getId())
    }

    @Test
    fun deleteNoteByIdShouldSuccess() {
        val note: NoteDto = createTmpNote(null)
        val mvcResult = mockMvc
            .perform(
                post("/note").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
                    .header(SecurityConstants.AUTH_HEADER, accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.text)))
            .andReturn()
        val res: String = mvcResult.response.contentAsString
        val returnedNote = objectMapper.readValue(res, Note::class.java)
        Assertions.assertTrue(noteService.getById(returnedNote.getId()).isPresent)
        mockMvc
            .perform(
                delete("/note/" + returnedNote.getId()).header(
                    SecurityConstants.AUTH_HEADER,
                    accessToken
                )
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        Assertions.assertFalse(noteService.getById(returnedNote.getId()).isPresent)
    }

    @Test
    fun deleteNoteByIdFromNonOwnerShouldFail() {
        val note: NoteDto = createTmpNote(null)
        val mvcResult = mockMvc
            .perform(
                post("/note").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
                    .header(SecurityConstants.AUTH_HEADER, accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.text)))
            .andReturn()
        val res: String = mvcResult.response.contentAsString
        val returnedNote = objectMapper.readValue(res, Note::class.java)
        Assertions.assertTrue(noteService.getById(returnedNote.getId()).isPresent)
        val pair: Pair<User, String> = signupTestUser()
        mockMvc
            .perform(
                delete("/note/" + returnedNote.getId()).header(
                    SecurityConstants.AUTH_HEADER,
                    pair.right
                )
            )
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        Assertions.assertTrue(noteService.getById(returnedNote.getId()).isPresent)
        userService.cleanupByUserId(pair.left.getId())
    }


    @Test
    fun getNotes() {
        val note: NoteDto = createTmpNote(null)
        val randKeyword = RandomStringUtils.randomAlphanumeric(10)
        note.note = note.note + " " + randKeyword
        val mvcResult = mockMvc
            .perform(
                post("/note").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
                    .header(SecurityConstants.AUTH_HEADER, accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.text)))
            .andReturn()
        val res: String = mvcResult.response.contentAsString
        val returnedNote = objectMapper.readValue(res, Note::class.java)
        Assertions.assertTrue(noteService.getById(returnedNote.getId()).isPresent)
        mockMvc.perform(get("/notes").header(SecurityConstants.AUTH_HEADER, accessToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.text)))
            .andExpect(jsonPath<Collection<*>>("$", Matchers.hasSize<Any>(Matchers.equalTo<Int>(2))))
            .andReturn()

        // search has result
        mockMvc
            .perform(
                get("/notes?keyword=$randKeyword").header(
                    SecurityConstants.AUTH_HEADER,
                    accessToken
                )
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(randKeyword)))
            .andExpect(jsonPath<Collection<*>>("$", Matchers.hasSize<Any>(Matchers.equalTo<Int>(1))))
            .andReturn()

        // search has no result
        mockMvc
            .perform(
                get("/notes?keyword=" + randKeyword + RandomStringUtils.randomAlphanumeric(6))
                    .header(SecurityConstants.AUTH_HEADER, accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath<Collection<*>>("$", Matchers.hasSize<Any>(Matchers.equalTo<Int>(0))))
            .andReturn()
    }

    @Test

    fun createNote() {
        val note: NoteDto = createTmpNote(null)
        val mvcResult = mockMvc
            .perform(
                post("/note").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(note))
                    .header(SecurityConstants.AUTH_HEADER, accessToken)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(note.text)))
            .andReturn()
        val res: String = mvcResult.response.contentAsString
        val returnedNote = objectMapper.readValue(res, Note::class.java)
        Assertions.assertEquals(note.note, returnedNote.note)
        notesToCleaned.add(returnedNote)
    }

    companion object {
        private val faker: Faker = Faker()
        fun createTmpNote(userId: Long?): NoteDto {
            return NoteDto(
                userId = userId,
                note = faker.lorem().characters(50, 100),
                url = faker.internet().url(),
                text = faker.funnyName().name(),
                tags = faker.lorem().words(3).toSet()
            )
        }
    }
}
