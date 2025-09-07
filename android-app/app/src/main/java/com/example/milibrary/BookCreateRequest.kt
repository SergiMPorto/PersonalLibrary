// MyLibraryModels.kt - NUEVO ARCHIVO
package com.example.milibrary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

// Modelo para enviar a la API al crear un libro
data class BookCreateRequest(
    @SerializedName("googleBooksId") val googleBooksId: String?,
    @SerializedName("title") val title: String,
    @SerializedName("authors") val authors: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String?,
    @SerializedName("publisher") val publisher: String?,
    @SerializedName("publishedDate") val publishedDate: String?,
    @SerializedName("pageCount") val pageCount: Int?,
    @SerializedName("language") val language: String?,
    @SerializedName("isbn10") val isbn10: String?,
    @SerializedName("isbn13") val isbn13: String?,
    @SerializedName("categories") val categories: String?
)

// Modelo para recibir libros guardados de la API
@Parcelize
data class SavedBook(
    @SerializedName("id") val id: Int,
    @SerializedName("googleBooksId") val googleBooksId: String?,
    @SerializedName("title") val title: String,
    @SerializedName("authors") val authors: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String?,
    @SerializedName("publisher") val publisher: String?,
    @SerializedName("publishedDate") val publishedDate: String?,
    @SerializedName("pageCount") val pageCount: Int?,
    @SerializedName("language") val language: String?,
    @SerializedName("isbn10") val isbn10: String?,
    @SerializedName("isbn13") val isbn13: String?,
    @SerializedName("categories") val categories: String?,
    @SerializedName("date_added") val dateAdded: String,
    @SerializedName("date_updated") val dateUpdated: String
) : Parcelable



// Modelo para estadísticas de la biblioteca
data class LibraryStats(
    @SerializedName("total_books") val totalBooks: Int,
    @SerializedName("total_authors") val totalAuthors: Int,
    @SerializedName("total_languages") val totalLanguages: Int,
    @SerializedName("most_recent_book") val mostRecentBook: String?
)

// Modelo para respuestas genéricas de la API
data class ApiResponse(
    @SerializedName("message") val message: String,
    @SerializedName("success") val success: Boolean? = true
)
