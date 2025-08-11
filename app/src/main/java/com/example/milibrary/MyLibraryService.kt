package com.example.milibrary

import retrofit2.Call
import retrofit2.http.*
import retrofit2.Response


interface MyLibraryService {

    @GET("health")
    fun getHealth(): Call<HealthResponse>


    @GET("api/books")
    fun getSavedBooks(
        @Query("search") search: String? = null, // buscando por titulo o autor
        @Query("limit") limit: Int? = 50,        // cantidad de libros a devolver
        @Query("offset") offset: Int? = 0  // indice del primer libro a devolver
    ): Call<List<SavedBook>>

    @POST("api/books")
    fun saveBook(@Body book: BookCreateRequest): Call<SavedBook>

    @GET("api/books/{id}")
    fun getBookById(@Path("id") id: Int): Call<SavedBook>

    @PUT("api/books/{id}")
    fun updateBook(
        @Path("id") id: Int,
        @Body book: BookCreateRequest
    ): Call<SavedBook>

    @DELETE("api/books/{id}")
    fun deleteBook(@Path("id") id: Int): Call<ApiResponse>

    @GET("api/stats")
    fun getLibraryStats(): Call<LibraryStats>

    @GET("api/authors")
    fun getAuthors(): Call<List<String>>

    @GET("api/categories")
    fun getCategories(): Call<List<String>>
}