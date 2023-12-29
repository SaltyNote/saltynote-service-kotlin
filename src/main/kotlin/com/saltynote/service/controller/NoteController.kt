package com.saltynote.service.controller

import cn.dev33.satoken.stp.StpUtil
import com.google.common.base.Splitter
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
import org.springframework.web.bind.annotation.*
import java.util.*

private val logger = KotlinLogging.logger {}

@RestController
class NoteController(val noteService: NoteService) {


    @GetMapping("/note/{id}")
    fun getNoteById(@PathVariable("id") id: Long): ResponseEntity<Note> {
        val note = noteService.getById(id)
        checkNoteOwner(note)
        return note.map { ResponseEntity.ok(it) }.orElseGet { ResponseEntity.notFound().build() }
    }

    @RequestMapping(value = ["/note/{id}"], method = [RequestMethod.POST, RequestMethod.PUT])
    fun updateNoteById(
        @PathVariable("id") id: Long, @RequestBody noteDto: NoteDto
    ): ResponseEntity<Note> {
        val queryNote = noteService.getById(id)
        checkNoteOwner(queryNote)
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
    fun deleteNoteById(@PathVariable("id") id: Long): ResponseEntity<ServiceResponse> {
        val note = noteService.getById(id)
        checkNoteOwner(note)
        noteService.delete(note.get())
        return ResponseEntity.ok(ServiceResponse.ok("Delete Successfully!"))
    }

    // TODO: this POST is required for chrome extension, as I find the PUT or DELETE
    // requests will be
    // blocked by Chrome. Further investigation is required from me for this issue.
    @PostMapping("/note/{id}/delete")
    fun postDeleteNoteById(@PathVariable("id") id: Long): ResponseEntity<ServiceResponse> {
        return deleteNoteById(id)
    }

    @GetMapping("/notes")
    fun getNotes(@RequestParam(required = false) keyword: String?): List<Note>? {
        val userId = StpUtil.getLoginIdAsLong()
        val allNotes = noteService.getAllByUserId(userId)
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
    fun getNotesByUrl(@RequestBody noteQuery: @Valid NoteQuery): List<Note> {
        val userId = StpUtil.getLoginIdAsLong()
        return noteService.getAllByUserIdAndUrl(userId, noteQuery.url)
    }

    @PostMapping("/note")
    fun createNote(@RequestBody noteDto: @Valid NoteDto): ResponseEntity<Note> {
        val userId = StpUtil.getLoginIdAsLong()
        noteDto.userId = userId
        var note = Note.from(noteDto)
        note = noteService.create(note)
        return ResponseEntity.ok(note)
    }

    private fun checkNoteOwner(note: Optional<Note>) {
        val userId = StpUtil.getLoginIdAsLong()
        if (note.isPresent && userId == note.get().userId) {
            return
        }
        throw WebAppRuntimeException(HttpStatus.FORBIDDEN, "Permission Error: You are not the owner of the note.")
    }
}
