package com.example.milibrary

import android.util.Log
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // --- URLs Base para diferentes entornos ---
    // URL para Google Books API
    private const val GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/"

    // URLs para tu backend Mi Biblioteca:
    // 1. K3s con MetalLB (producción local - Raspberry Pi):
    private const val K3S_METALLB_URL = "http://192.168.1.200:8000/"

    // 2. Port Forward (emulador Android Studio):
    private const val PORT_FORWARD_URL = "http://10.0.2.2:8000/"

    // URL actualmente seleccionada para el backend de Mi Biblioteca.
    private const val CURRENT_MY_LIBRARY_BASE_URL = K3S_METALLB_URL

    // --- Interceptor para Logging HTTP ---
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("HTTP_LOG", message)
    }.apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    // --- Interceptor para añadir cabeceras y loguear peticiones/respuestas ---
    private val headerAndErrorInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .method(originalRequest.method, originalRequest.body)

        val request = requestBuilder.build()

        Log.d("HTTP_REQUEST", "🌐 URL: ${request.url}")
        Log.d("HTTP_REQUEST", "📤 Method: ${request.method}")

        try {
            val response = chain.proceed(request)

            Log.d("HTTP_RESPONSE", "Response Code: ${response.code}")
            if (!response.isSuccessful) {
                Log.e("HTTP_ERROR", "HTTP Error: ${response.code} - ${response.message} for URL: ${response.request.url}")
            }
            response
        } catch (e: Exception) {
            Log.e("HTTP_ERROR", "🔌 Network error for URL: ${request.url} - ${e.message}", e)
            throw e
        }
    }

    // --- Configuración de OkHttpClient ---
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(headerAndErrorInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // --- Instancia de Retrofit para Google Books ---
    val googleBooksRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(GOOGLE_BOOKS_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    // --- Instancia de Retrofit para Mi Biblioteca API ---
    val myLibraryRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(CURRENT_MY_LIBRARY_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    // --- Servicios de API ---
    val googleBooksService: GoogleBooksService by lazy { googleBooksRetrofit.create(GoogleBooksService::class.java) }
    val myLibraryService: MyLibraryService by lazy { myLibraryRetrofit.create(MyLibraryService::class.java) }

    // --- Funciones de Utilidad ---
    fun testConnection(onResult: (String, Boolean) -> Unit) {
        val urls = listOf(
            "K3s MetalLB (Raspberry Pi)" to K3S_METALLB_URL,
            "Port Forward (Emulator)" to PORT_FORWARD_URL
        )

        Log.d("CONNECTION_TEST", "🧪 Probando conexiones configuradas...")
        urls.forEach { (name, url) ->
            Log.d("CONNECTION_TEST", "🔄 URL configurada para '$name': $url")
        }
        onResult("Current active URL", true)
    }

    fun getCurrentBaseUrl(): String = CURRENT_MY_LIBRARY_BASE_URL

    fun getAllBackendUrls(): Map<String, String> = mapOf(
        "K3s MetalLB (Raspberry Pi)" to K3S_METALLB_URL,
        "Port Forward (Emulator)" to PORT_FORWARD_URL,
        "Current Active" to CURRENT_MY_LIBRARY_BASE_URL
    )
}