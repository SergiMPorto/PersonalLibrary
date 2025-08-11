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

    // URLs para tu backend Mi Biblioteca, ajusta según tu configuración de Minikube:
    // 1. Minikube NodePort:
    private const val MINIKUBE_NODEPORT_URL = "http://192.168.49.2:30800/"

    // 2. Port Forward:
    //    Ajusta el puerto si tu port-forward no es 8080.
    private const val PORT_FORWARD_URL = "http://10.0.2.2:8080/"

    // 3. IP Local de la máquina anfitriona: Si accedes desde un dispositivo físico en la misma red Wi-Fi,
    //    o si Minikube expone el servicio a tu IP local directamente (como con '--address=0.0.0.0' en port-forward).
    //    ¡Importante! Cambia esta IP por la IP real de tu máquina en tu red local (ej. ipconfig/ifconfig).
    private const val MY_LOCAL_NETWORK_IP_URL = "http://192.168.1.96:8080/"

    // URL actualmente seleccionada para el backend de Mi Biblioteca.
    // Puedes cambiarla aquí o hacerla configurable.
    private const val CURRENT_MY_LIBRARY_BASE_URL = MY_LOCAL_NETWORK_IP_URL

    // --- Interceptor para Logging HTTP ---
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("HTTP_LOG", message)
    }.apply {
        // En desarrollo, Level.BODY es genial para ver todo (headers, bodies).
        // En producción, considera Level.HEADERS o Level.BASIC por seguridad y rendimiento.
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    // --- Interceptor para añadir cabeceras y loguear peticiones/respuestas ---
    private val headerAndErrorInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // 1. Añadir cabeceras comunes
        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .method(originalRequest.method, originalRequest.body) // Asegura que el método y el cuerpo se mantienen

        val request = requestBuilder.build()

        // 2. Loguear petición saliente
        Log.d("HTTP_REQUEST", "🌐 URL: ${request.url}")
        Log.d("HTTP_REQUEST", "📤 Method: ${request.method}")
        // No loguear headers aquí si HttpLoggingInterceptor ya está en BODY, para evitar duplicados

        try {
            // 3. Proceder con la petición y obtener la respuesta
            val response = chain.proceed(request)

            // 4. Loguear respuesta (código y posibles errores)
            Log.d("HTTP_RESPONSE", " Response Code: ${response.code}")
            if (!response.isSuccessful) {
                Log.e("HTTP_ERROR", " HTTP Error: ${response.code} - ${response.message} for URL: ${response.request.url}")
                // Opcional: Loguear el body del error si la respuesta no fue exitosa
                // val errorBody = response.body?.string()
                // Log.e("HTTP_ERROR", " Error Body: $errorBody")
            }
            response // Devolver la respuesta para que la cadena de interceptores continúe
        } catch (e: Exception) {
            // 5. Capturar y loguear errores de red
            Log.e("HTTP_ERROR", " 🔌 Network error for URL: ${request.url} - ${e.message}", e)
            throw e // Relanzar la excepción para que Retrofit la maneje (onFailure)
        }
    }

    // --- Configuración de OkHttpClient ---
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(headerAndErrorInterceptor) // Interceptor para cabeceras y manejo de errores básico
        .addInterceptor(loggingInterceptor)       // Interceptor para logging detallado
        .connectTimeout(30, TimeUnit.SECONDS)     // Tiempo máximo para establecer la conexión
        .readTimeout(30, TimeUnit.SECONDS)        // Tiempo máximo para leer datos del servidor
        .writeTimeout(30, TimeUnit.SECONDS)       // Tiempo máximo para escribir datos al servidor
        .retryOnConnectionFailure(true)           // Reintentar la conexión en caso de fallo
        .build()

    // --- Instancia de Retrofit para Google Books ---
    val googleBooksRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(GOOGLE_BOOKS_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient) // Usamos el OkHttpClient configurado
        .build()

    // --- Instancia de Retrofit para Mi Biblioteca API ---
    val myLibraryRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(CURRENT_MY_LIBRARY_BASE_URL) // Usamos la URL seleccionada para nuestro backend
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient) // Usamos el OkHttpClient configurado
        .build()

    // --- Servicios de API ---
    // Interfaces de servicio de Retrofit (deben estar definidas en archivos separados)
    // Por ejemplo: interface GoogleBooksService { @GET("volumes") suspend fun searchBooks(...) }
    // interface MyLibraryService { @GET("health") suspend fun checkHealth(...) }
    val googleBooksService: GoogleBooksService by lazy { googleBooksRetrofit.create(GoogleBooksService::class.java) }
    val myLibraryService: MyLibraryService by lazy { myLibraryRetrofit.create(MyLibraryService::class.java) }


    // --- Funciones de Utilidad ---
    /**
     * Prueba la conexión a las URLs de backend definidas.
     * Nota: Esta función solo loguea, no realiza una petición HTTP real.
     * Para una prueba real, necesitarías una llamada de API simple (ej. /health)
     * a cada URL y manejar el resultado asincrónicamente.
     */
    fun testConnection(onResult: (String, Boolean) -> Unit) {
        val urls = listOf(
            "Minikube NodePort" to MINIKUBE_NODEPORT_URL,
            "Port Forward (Emulator)" to PORT_FORWARD_URL,
            "My Local Network IP" to MY_LOCAL_NETWORK_IP_URL
        )

        Log.d("CONNECTION_TEST", "🧪 Probando conexiones configuradas...")
        urls.forEach { (name, url) ->
            Log.d("CONNECTION_TEST", "🔄 URL configurada para '$name': $url")
            // Para implementar un test real aquí:
            // Tendrías que hacer una llamada API a cada 'url' (ej. a /health)
            // y luego llamar a onResult(name, true) o onResult(name, false)
            // dependiendo del éxito de la llamada. Esto requiere coroutines.
        }
        // Llamada de ejemplo a onResult (solo para mostrar el uso, no es un test real)
        onResult("Current active URL", true)
    }

    /**
     * Retorna la URL base actualmente configurada para la API de Mi Biblioteca.
     */
    fun getCurrentBaseUrl(): String = CURRENT_MY_LIBRARY_BASE_URL

    /**
     * Retorna un mapa con todas las URLs de backend definidas para referencia.
     */
    fun getAllBackendUrls(): Map<String, String> = mapOf(
        "Minikube NodePort" to MINIKUBE_NODEPORT_URL,
        "Port Forward (Emulator)" to PORT_FORWARD_URL,
        "My Local Network IP" to MY_LOCAL_NETWORK_IP_URL,
        "Current Active" to CURRENT_MY_LIBRARY_BASE_URL
    )
}