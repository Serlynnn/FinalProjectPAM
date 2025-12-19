package com.example.finalprojectpam.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Favorite(
	val id: String? = null,

	@SerialName("user_id")
	val userId: String,

	@SerialName("note_id")
	val noteId: String,
)
