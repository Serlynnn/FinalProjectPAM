package com.example.finalprojectpam.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {

	private const val SUPABASE_URL = "https://bgflrtvbwcexlyilgaay.supabase.co"
	private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJnZmxydHZid2NleGx5aWxnYWF5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUxNzYzMzUsImV4cCI6MjA4MDc1MjMzNX0.PwzZ1LYa_QqOciHuGw55lqsQidV-jmCUtJqqINlOxko"

	val client: SupabaseClient = createSupabaseClient(
		supabaseUrl = SUPABASE_URL,
		supabaseKey = SUPABASE_KEY
	) {
		install(Auth)
		install(Postgrest)
		install(Storage)
	}
}
