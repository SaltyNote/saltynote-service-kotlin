package com.saltynote.service.controller

import com.google.common.base.Splitter
import com.saltynote.service.domain.transfer.JwtUser
import com.saltynote.service.domain.transfer.NoteDto
import com.saltynote.service.domain.transfer.NoteQuery
import com.saltynote.service.domain.transfer.ServiceResponse
import com.saltynote.service.entity.Note
import com.saltynote.service.exception.WebAppRuntimeException
import com.saltynote.service.service.NoteService
import com.saltynote.service.utils.BaseUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

private val logger = KotlinLogging.logger {}

@RestController
class NoteController(val noteService: NoteService) {


    @GetMapping("/note/{id}")
    fun getNoteById(@PathVariable("id") id: String): ResponseEntity<Note> {
        val note = noteService.getById(id)
        checkNoteOwner(note, auth)
        return note.map { ResponseEntity.ok(it) }.orElseGet { ResponseEntity.notFound().build() }
    }

    @RequestMapping(value = ["/note/{id}"], method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateNoteById(
        @PathVariable("id") id: String, @RequestBody noteDto: NoteDto
    ): ResponseEntity<Note> {
        val queryNote = noteService.getById(id)
        checkNoteOwner(queryNote, auth)
        var noteTobeUpdate = queryNote.get()
        if (StringUtils.isNotBlank(noteDto.note)) {
            noteTobeUpdate.note = noteDto.note
        }
        noteTobeUpdate.tags = noteDto.tags
        if (StringUtils.isNotBlank(noteDto.highlightColor)) {
            noteTobeUpdate.highlightColor = noteDto.highlightColor
        }
        noteTobeUpdate = noteService.update(noteTobeUpdate)
        return ResponseEntity.ok(noteTobeUpdate)
    }

    @DeleteMapping("/note/{id}")
    fun deleteNoteById(@PathVariable("id") id: String): ResponseEntity<ServiceResponse> {
        val note = noteService.getById(id)
        checkNoteOwner(note, auth)
        noteService.delete(note.get())
        return ResponseEntity.ok(ServiceResponse.ok("Delete Successfully!"))
    }

    // TODO: this POST is required for chrome extension, as I find the PUT or DELETE
    // requests will be
    // blocked by Chrome. Further investigation is required from me for this issue.
    @PostMapping("/note/{id}/delete")
    fun postDeleteNoteById(@PathVariable("id") id: String): ResponseEntity<ServiceResponse> {
        return deleteNoteById(id, auth)
    }

    @GetMapping("/notes")
    fun getNotes(auth: Authentication, @RequestParam(required = false) keyword: String?): List<Note>? {
        val user: JwtUser = auth.principal as JwtUser
        val allNotes = noteService.getAllByUserId(user.getId())
        if (allNotes.isEmpty() || StringUtils.isBlank(keyword)) {
            return allNotes
        }
        val queries = Splitter.on(" ").trimResults().omitEmptyStrings().split(keyword!!)
        return allNotes.stream()
            .filter { n: Note ->
                (StringUtils.isNotBlank(n.note) && BaseUtils.containsAllIgnoreCase(n.note, queries)
                        || StringUtils.isNotBlank(n.text) && BaseUtils.containsAllIgnoreCase(n.text, queries))
            }
            .toList()
    }

    @PostMapping("/notes")
    fun getNotesByUrl(auth: Authentication, @RequestBody noteQuery: @Valid NoteQuery): List<Note> {
        val user: JwtUser = auth.principal as JwtUser
        return noteService.getAllByUserIdAndUrl(user.getId(), noteQuery.url)
    }

    @PostMapping("/note")
    fun createNote(@RequestBody noteDto: @Valid NoteDto): ResponseEntity<Note> {
        val user = auth.principal as JwtUser
        noteDto.userId = user.getId()
        var note = Note.from(noteDto)
        note = noteService.create(note)
        if (note.getId() != null) {
            return ResponseEntity.ok(note)
        }
        throw WebAppRuntimeException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Failed to save note into database: $note"
        )
    }

    private fun checkNoteOwner(note: Optional<Note>) {
        val user = auth.principal as JwtUser
        if (note.isPresent && user.getId() == note.get().userId) {
            return
        }
        throw WebAppRuntimeException(HttpStatus.FORBIDDEN, "Permission Error: You are not the owner of the note.")
    }
}
