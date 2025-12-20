package com.example.finalprojectpam.data.repository

import com.example.finalprojectpam.data.model.Category
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

class CategoryRepository(private val supabase: SupabaseClient) {

	// 1. READ: Ambil semua kategori milik user
	suspend fun getCategories(): List<Category> {
		return supabase.postgrest["categories"]
			.select()
			.decodeList<Category>()
	}


	// 2. CREATE: Tambah kategori baru
	suspend fun addCategory(name: String, imageUrl: String?) { // Tambahkan imageUrl
		val currentUser = supabase.auth.currentUserOrNull() ?: throw Exception("No user logged in")

		val newCategory = Category(
			userId = currentUser.id,
			name = name,
			imageUrl = imageUrl // Simpan URL
		)

		supabase.postgrest["categories"].insert(newCategory)
	}


	// 3. UPDATE: Ubah nama kategori dan URL gambar
	suspend fun updateCategory(
		id: String,
		newName: String,
		newImageUrl: String? //  Tambahkan newImageUrl
	) {
		supabase.postgrest["categories"].update(
			{
				set("name", newName)
				set("image_url", newImageUrl) //  Update URL gambar
			}
		) {
			filter {
				eq("id", id)
			}
		}
	}

	// 4. DELETE: Hapus kategori
	suspend fun deleteCategory(id: String) {
		supabase.postgrest["categories"].delete {
			filter {
				eq("id", id)
			}
		}
	}
}
