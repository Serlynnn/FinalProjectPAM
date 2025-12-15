package com.example.finalprojectpam.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

class StorageRepository(private val supabase: SupabaseClient) {

	private val BUCKET_NAME = "materials"
	private val storage: Storage = supabase.storage

	suspend fun uploadImage(data: ByteArray, path: String): String {
		val response = storage[BUCKET_NAME].upload(path = path, data = data)
		// Mengembalikan key dari server, atau fallback ke path lokal jika null
		return response.key ?: path
	}

	fun getPublicUrl(path: String): String {

		val baseUrl = supabase.supabaseUrl


		// Path: /storage/v1/object/public/{bucket_name}/{path_file}
		return "https://$baseUrl/storage/v1/object/public/$path"
	}

	suspend fun deleteImage(path: String) {
		storage[BUCKET_NAME].delete(listOf(path))
	}
}
