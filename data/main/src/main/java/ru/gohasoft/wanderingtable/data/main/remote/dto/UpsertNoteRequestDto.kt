package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

/** `POST /notes` is a single upsert: a non-null [id] edits that post, null creates one. */
@Serializable
internal data class UpsertNoteRequestDto(
    val id: String?,
    val title: String,
    val content: String,
)
