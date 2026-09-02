package ru.gohasoft.wanderingtable.data.main.remote.dto

import kotlinx.serialization.Serializable

/** A club news post — `GET /notes`. */
@Serializable
internal data class NoteDto(
    val id: String,
    val title: String,
    val content: String = "",
    val createdAt: String,
    val ownerId: String,
)
