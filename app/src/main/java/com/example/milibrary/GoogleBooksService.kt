package com.example.milibrary
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksService {



        @GET("volumes")
        fun getBookByTitle(@Query("q") title: String): Call<BookResponse>

        @GET("volumes")
        fun getBookByISBN(@Query("q") isbn: String): Call<BookResponse>
    }


