package com.saltynote.service.entity

import com.saltynote.service.domain.Identifiable
import com.saltynote.service.domain.transfer.NoteDto
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serial
import java.io.Serializable

@Document
data class Note(
    @Id val id: String? = null,
    val userId: String? = null,
    val text: String,
    val url: String,
    var note: String = "",
    val isPageOnly: Boolean = false,
    var highlightColor: String = "",
    val createdTime: Long = System.currentTimeMillis(),
    var tags: Set<String> = emptySet()
) : Serializable, Identifiable {


    companion object {
        @Serial
        private val serialVersionUID = 1L

        fun from(note: NoteDto): Note {
            return Note(
                userId = note.userId,
                text = note.text,
                url = note.url,
                note = note.note,
                isPageOnly = note.pageOnly,
                highlightColor = note.highlightColor,
                tags = note.tags,
            )
        }
    }

    override fun getId() = id
}
