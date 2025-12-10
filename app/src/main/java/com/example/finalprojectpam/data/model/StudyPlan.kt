package com.example.finalprojectpam.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudyPlan(
	val id: String? = null,

	@SerialName("user_id")
	val userId: String,

	val title: String,
	val date: String // Format tanggal YYYY-MM-DD
)
