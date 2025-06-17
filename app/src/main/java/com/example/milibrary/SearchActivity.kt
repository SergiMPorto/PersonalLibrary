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
        recyclerView.layoutManager = LinearLayoutManager(this)
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
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(booksList) { selectedBook ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("book", selectedBook)
            startActivity(intent)
        }
        recyclerView.adapter = bookAdapter
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
}