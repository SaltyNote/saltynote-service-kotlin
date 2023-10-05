package com.saltynote.service.entity

import com.saltynote.service.domain.Identifiable
import com.saltynote.service.domain.transfer.NoteDto
import com.saltynote.service.generator.SnowflakeIdGenerator
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable

@Document
data class Note(
    @Id
    private  val id: Long = SnowflakeIdGenerator.DEFAULT.nextId(),
    val userId: Long,
    val text: String,
    val url: String,
    var note: String = "",
    val isPageOnly: Boolean = false,
    var highlightColor: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    var tags: Set<String> = emptySet()
) : Serializable, Identifiable {


    companion object {
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
