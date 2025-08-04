package com.example.milibrary

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConnectionTestActivity : AppCompatActivity() {

    private lateinit var textViewResults: TextView
    private lateinit var buttonTestHealth: Button
    private lateinit var buttonTestTable: Button
    private lateinit var buttonTestBooks: Button
    private lateinit var buttonTestStats: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection_test)

        initializeViews()
        setupClickListeners()

        // Test automático al abrir
        testHealth()
    }

    private fun initializeViews() {
        textViewResults = findViewById(R.id.textViewResults)
        buttonTestHealth = findViewById(R.id.buttonTestHealth)
        buttonTestTable = findViewById(R.id.buttonTestTable)
        buttonTestBooks = findViewById(R.id.buttonTestBooks)
        buttonTestStats = findViewById(R.id.buttonTestStats)
    }

    private fun setupClickListeners() {
        buttonTestHealth.setOnClickListener { testHealth() }
        buttonTestTable.setOnClickListener { testTable() }
        buttonTestBooks.setOnClickListener { testBooks() }
        buttonTestStats.setOnClickListener { testStats() }
    }

    private fun testHealth() {
        appendResult("🏥 Probando Health Check...")
        buttonTestHealth.isEnabled = false

        RetrofitClient.myLibraryService.getHealth().enqueue(object : Callback<HealthResponse> {
            override fun onResponse(call: Call<HealthResponse>, response: Response<HealthResponse>) {
                runOnUiThread {
                    buttonTestHealth.isEnabled = true
                    if (response.isSuccessful) {
                        val health = response.body()
                        appendResult("Health Check exitoso!")
                        appendResult("   Status: ${health?.status}")
                        appendResult("   Database: ${health?.databaseStatus}")
                        appendResult("   URL: ${RetrofitClient.getCurrentBaseUrl()}")
                    } else {
                        appendResult(" Health Check falló: ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<HealthResponse>, t: Throwable) {
                runOnUiThread {
                    buttonTestHealth.isEnabled = true
                    appendResult(" Error de conexión: ${t.message}")
                    appendResult("   URL usada: ${RetrofitClient.getCurrentBaseUrl()}")
                }
            }
        })
    }

    private fun testTable() {
        appendResult("\n🧪 Probando Test Table...")
        buttonTestTable.isEnabled = false

        // Llamada directa usando OkHttp para test
        val client = okhttp3.OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("${RetrofitClient.getCurrentBaseUrl()}api/test/table")
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                runOnUiThread {
                    buttonTestTable.isEnabled = true
                    if (response.isSuccessful) {
                        appendResult(" Test Table exitoso!")
                        appendResult("   Response: ${response.body?.string()?.take(100)}...")
                    } else {
                        appendResult(" Test Table falló: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    buttonTestTable.isEnabled = true
                    appendResult(" Error en Test Table: ${e.message}")
                }
            }
        }.start()
    }

    private fun testBooks() {
        appendResult("\n📚 Probando API Books...")
        buttonTestBooks.isEnabled = false

        RetrofitClient.myLibraryService.getSavedBooks().enqueue(object : Callback<List<SavedBook>> {
            override fun onResponse(call: Call<List<SavedBook>>, response: Response<List<SavedBook>>) {
                runOnUiThread {
                    buttonTestBooks.isEnabled = true
                    if (response.isSuccessful) {
                        val books = response.body() ?: emptyList()
                        appendResult(" API Books exitoso!")
                        appendResult("   Libros encontrados: ${books.size}")
                    } else {
                        appendResult(" API Books falló: ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<List<SavedBook>>, t: Throwable) {
                runOnUiThread {
                    buttonTestBooks.isEnabled = true
                    appendResult(" Error en API Books: ${t.message}")
                }
            }
        })
    }

    private fun testStats() {
        appendResult("\n📊 Probando Stats...")
        buttonTestStats.isEnabled = false

        RetrofitClient.myLibraryService.getLibraryStats().enqueue(object : Callback<LibraryStats> {
            override fun onResponse(call: Call<LibraryStats>, response: Response<LibraryStats>) {
                runOnUiThread {
                    buttonTestStats.isEnabled = true
                    if (response.isSuccessful) {
                        val stats = response.body()
                        appendResult(" Stats exitoso!")
                        appendResult("   Total libros: ${stats?.totalBooks}")
                        appendResult("   Total autores: ${stats?.totalAuthors}")
                    } else {
                        appendResult(" Stats falló: ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<LibraryStats>, t: Throwable) {
                runOnUiThread {
                    buttonTestStats.isEnabled = true
                    appendResult(" Error en Stats: ${t.message}")
                }
            }
        })
    }

    private fun appendResult(text: String) {
        textViewResults.append("$text\n")
    }
}