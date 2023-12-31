package com.saltynote.service.domain.transfer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class NoteDto(
    var userId: Long?,
    val text: String,
    val url: String,
    var note: String = "",

    @JsonProperty("is_page_only")
    val pageOnly: Boolean = false,
    val highlightColor: String = "",
    val tags: Set<String> = emptySet(),
)