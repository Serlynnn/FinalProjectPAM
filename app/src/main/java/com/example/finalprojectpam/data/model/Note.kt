package com.example.finalprojectpam.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Note(
	// ID Catatan (Primary Key)
	// nullable (= null) agar saat INSERT, Supabase otomatis membuatkan UUID unik
	val id: String? = null,

	// ID Pengguna (Foreign Key)
	// @SerialName("user_id"): Di Database namanya "user_id", tapi di Kotlin kita panggil "userId"
	@SerialName("user_id")
	val userId: String,

	// Judul & Isi
	val title: String,
	val content: String? = null,

	// Fitur Anggota 2 (Upload Gambar)
	@SerialName("image_url")
	val imageUrl: String? = null,

	// Fitur Anggota 3 (Kategori)
	@SerialName("category_id")
	val categoryId: String? = null,

	// Tanggal Pembuatan
	@SerialName("date_created")
	val dateCreated: String? = null
)