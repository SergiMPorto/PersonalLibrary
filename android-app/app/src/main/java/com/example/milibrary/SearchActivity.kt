package com.example.milibrary

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import android.Manifest
import androidx.activity.result.ActivityResultLauncher

class SearchActivity : ComponentActivity() {

    private lateinit var editTextBookTitle: EditText
    private lateinit var buttonSearchByTitle: Button
    private lateinit var buttonScanISBN: Button
    private lateinit var bookAdapter: BookAdapter
    private val booksList = mutableListOf<Book>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var scanButton: Button
    private lateinit var buttonMyLibrary: Button
    private lateinit var buttonConnectionTest: Button

    // Scanner launcher
    private val scanLauncher: ActivityResultLauncher<ScanOptions> = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val isbn = result.contents
            showToast("ISBN escaneado: $isbn")
            searchBooksByISBN(isbn)
        } else {
            showToast("Escaneo cancelado")
        }
    }

    // Permission launcher 
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startScan()
        } else {
            showToast("Permiso de cámara denegado")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun initializeViews() {
        editTextBookTitle = findViewById(R.id.editTextBookTitle)
        buttonSearchByTitle = findViewById(R.id.buttonSearchByTitle)
        buttonScanISBN = findViewById(R.id.buttonScanISBN)
        recyclerView = findViewById(R.id.recyclerView)
        scanButton = findViewById(R.id.scanButton)
        buttonMyLibrary = findViewById(R.id.buttonMyLibrary)
        recyclerView.layoutManager = LinearLayoutManager(this)
        buttonConnectionTest = findViewById(R.id.buttonTestConnection)
    }

    private fun setupClickListeners() {
        buttonSearchByTitle.setOnClickListener {
            val title = editTextBookTitle.text.toString()
            if (title.isNotEmpty()) {
                searchBookByTitle(title)
            } else {
                showToast("Por favor, ingrese un título")
            }
        }

        buttonScanISBN.setOnClickListener {
            val isbn = editTextBookTitle.text.toString()
            if (isbn.isNotEmpty()) {
                searchBooksByISBN(isbn)
            } else {
                showToast("Por favor, ingrese un ISBN")
            }
        }

        scanButton.setOnClickListener {
            checkCameraPermission()
        }
        buttonMyLibrary.setOnClickListener {
            val intent = Intent(this, MyLibraryActivity::class.java)
            startActivity(intent)
        }
         buttonConnectionTest.setOnClickListener {
             val intent = Intent(this, ConnectionTestActivity::class.java)
             startActivity(intent)
         }


    }


    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(booksList) { selectedBook ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("book", selectedBook)
            startActivity(intent)
        }
        recyclerView.adapter = bookAdapter
    }
    private fun testApiConnection() {
        showToast("🔄 Verificando conexión con tu biblioteca...")

        RetrofitClient.myLibraryService.getHealth().enqueue(object : Callback<HealthResponse> {
            override fun onResponse(call: Call<HealthResponse>, response: Response<HealthResponse>) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val health = response.body()
                        if (health?.status == "healthy") {
                            showToast("✅ Conectado a tu biblioteca personal")
                            buttonMyLibrary.setBackgroundColor(getColor(android.R.color.holo_green_dark))
                        } else {
                            showToast("⚠️ Biblioteca con problemas")
                            buttonMyLibrary.setBackgroundColor(getColor(android.R.color.holo_orange_dark))
                        }
                    } else {
                        handleConnectionError("Error del servidor: ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<HealthResponse>, t: Throwable) {
                runOnUiThread {
                    val errorMessage = when {
                        t.message?.contains("timeout") == true -> "Tiempo de espera agotado"
                        t.message?.contains("Unable to resolve host") == true -> "Sin conexión a internet"
                        t.message?.contains("Connection refused") == true -> "Servidor no disponible"
                        else -> "Error de conexión"
                    }
                    handleConnectionError(errorMessage)
                }
            }
        })
    }

    private fun handleConnectionError(message: String) {
        showToast("❌ $message")
        buttonMyLibrary.setBackgroundColor(getColor(android.R.color.holo_red_dark))
        buttonMyLibrary.text = "❌ Mi Biblioteca (Sin conexión)"
    }

    private fun startScan() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.EAN_13, ScanOptions.EAN_8)
        options.setPrompt("Escanea el código ISBN del libro")
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)
        scanLauncher.launch(options)
    }

    // NUEVO: Función de permisos modernizada
    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Ya tenemos el permiso, iniciamos el escáner
                startScan()
            }
            else -> {
                // Solicitar el permiso usando el launcher moderno
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // ELIMINAMOS onRequestPermissionsResult - ya no se necesita

    private fun searchBooksByISBN(isbn: String) {
        showToast("Buscando libros con ISBN: $isbn")
        RetrofitClient.googleBooksService.getBookByISBN("isbn:$isbn").enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                handleBookResponse(response)
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                showToast("Error de conexión: ${t.message}")
            }
        })
    }

    private fun searchBookByTitle(title: String) {
        showToast("Buscando libros por título: $title")
        RetrofitClient.googleBooksService.getBookByTitle(title).enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                handleBookResponse(response)
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                showToast("Error de conexión: ${t.message}")
            }
        })
    }

    private fun handleBookResponse(response: Response<BookResponse>) {
        if (response.isSuccessful) {
            val bookList = response.body()?.items
            if (!bookList.isNullOrEmpty()) {
                booksList.clear()
                booksList.addAll(bookList)
                bookAdapter.notifyDataSetChanged()
                showToast("Se encontraron ${bookList.size} libros")
            } else {
                booksList.clear()
                bookAdapter.notifyDataSetChanged()
                showToast("No se encontraron libros")
            }
        } else {
            showToast("Error en la respuesta del servidor: ${response.code()}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    override fun onResume() {
        super.onResume()

        testApiConnection()
    }

}