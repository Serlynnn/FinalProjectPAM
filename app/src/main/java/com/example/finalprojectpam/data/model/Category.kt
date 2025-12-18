package com.example.finalprojectpam.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
	val id: String? = null,

	@SerialName("user_id")
	val userId: String,

	val name: String,

	@SerialName("image_url")
	val imageUrl: String? = null
)

