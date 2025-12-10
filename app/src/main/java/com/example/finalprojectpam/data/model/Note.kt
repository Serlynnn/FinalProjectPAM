package com.example.finalprojectpam.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Note(
	val id: String? = null,

	@SerialName("user_id")
	val userId: String,

	val title: String,
	val content: String?,

	@SerialName("image_url")
	val imageUrl: String? = null, // Fitur Anggota 2

	@SerialName("category_id")
	val categoryId: String? = null, // Fitur Anggota 3

	@SerialName("date_created")
	val dateCreated: String? = null
)
