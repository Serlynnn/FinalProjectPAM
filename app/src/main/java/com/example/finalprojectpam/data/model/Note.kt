package com.example.finalprojectpam.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Note(

	// ID Catatan (Primary Key)
	val id: String = UUID.randomUUID().toString(),
	// ID Pengguna (Foreign Key) - Akan diisi saat insert
	val user_id: String,
	// Judul Catatan
	val title: String,
	// Isi Catatan
	val content: String,
	// Tanggal pembuatan (Dibiarkan sebagai String, Supabase akan mengurus timestamp)
	val date_created: String? = null


//	val id: String? = null,
//
//	@SerialName("user_id")
//	val userId: String,
//
//	val title: String,
//	val content: String?,
//
//	@SerialName("image_url")
//	val imageUrl: String? = null, // Fitur Anggota 2
//
//	@SerialName("category_id")
//	val categoryId: String? = null, // Fitur Anggota 3
//
//	@SerialName("date_created")
//	val dateCreated: String? = null

)
