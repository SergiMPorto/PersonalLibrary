package com.example.milibrary

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyLibraryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var savedBookAdapter: SavedBookAdapter
    private lateinit var editTextSearch: EditText
    private lateinit var buttonSearch: Button
    private lateinit var buttonShowAll: Button
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewEmpty: TextView
    private lateinit var textViewStats: TextView

    private val savedBooksList = mutableListOf<SavedBook>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_library)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        setupSwipeRefresh()

        // Cargar libros al iniciar
        loadSavedBooks()
        loadLibraryStats()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewMyLibrary)
        editTextSearch = findViewById(R.id.editTextSearchMyLibrary)
        buttonSearch = findViewById(R.id.buttonSearchMyLibrary)
        buttonShowAll = findViewById(R.id.buttonShowAll)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        progressBar = findViewById(R.id.progressBarMyLibrary)
        textViewEmpty = findViewById(R.id.textViewEmpty)
        textViewStats = findViewById(R.id.textViewStats)
    }

    private fun setupRecyclerView() {
        savedBookAdapter = SavedBookAdapter(savedBooksList) { selectedBook ->
            // Convertir SavedBook a Book para usar con DetailActivity existente
            val displayBook = selectedBook.toDisplayBook()
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("book", displayBook)
            intent.putExtra("saved_book_id", selectedBook.id) // Para futuras funciones de edición
            startActivity(intent)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MyLibraryActivity)
            adapter = savedBookAdapter
        }
    }

    private fun setupClickListeners() {
        buttonSearch.setOnClickListener {
            val searchTerm = editTextSearch.text.toString().trim()
            if (searchTerm.isNotEmpty()) {
                searchBooks(searchTerm)
            } else {
                showToast("Por favor, ingresa un término de búsqueda")
            }
        }

        buttonShowAll.setOnClickListener {
            editTextSearch.text.clear()
            loadSavedBooks()
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            loadSavedBooks()
            loadLibraryStats()
        }
    }

    private fun loadSavedBooks() {
        showLoading(true)

        RetrofitClient.myLibraryService.getSavedBooks().enqueue(object : Callback<List<SavedBook>> {
            override fun onResponse(call: Call<List<SavedBook>>, response: Response<List<SavedBook>>) {
                showLoading(false)
                swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    val books = response.body() ?: emptyList()
                    updateBooksList(books)
                    showToast("${books.size} libros cargados")
                } else {
                    handleError("Error cargando libros: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<SavedBook>>, t: Throwable) {
                showLoading(false)
                swipeRefreshLayout.isRefreshing = false
                handleError("Error de conexión: ${t.message}")
            }
        })
    }

    private fun searchBooks(searchTerm: String) {
        showLoading(true)

        RetrofitClient.myLibraryService.getSavedBooks(search = searchTerm).enqueue(object : Callback<List<SavedBook>> {
            override fun onResponse(call: Call<List<SavedBook>>, response: Response<List<SavedBook>>) {
                showLoading(false)

                if (response.isSuccessful) {
                    val books = response.body() ?: emptyList()
                    updateBooksList(books)
                    showToast("${books.size} resultados encontrados")
                } else {
                    handleError("Error en la búsqueda: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<SavedBook>>, t: Throwable) {
                showLoading(false)
                handleError("Error de conexión: ${t.message}")
            }
        })
    }

    private fun loadLibraryStats() {
        RetrofitClient.myLibraryService.getLibraryStats().enqueue(object : Callback<LibraryStats> {
            override fun onResponse(call: Call<LibraryStats>, response: Response<LibraryStats>) {
                if (response.isSuccessful) {
                    val stats = response.body()
                    stats?.let { updateStatsDisplay(it) }
                }
            }

            override fun onFailure(call: Call<LibraryStats>, t: Throwable) {
                // Silently fail for stats - not critical
            }
        })
    }

    private fun updateBooksList(books: List<SavedBook>) {
        savedBooksList.clear()
        savedBooksList.addAll(books)
        savedBookAdapter.notifyDataSetChanged()

        // Mostrar/ocultar mensaje de lista vacía
        if (books.isEmpty()) {
            textViewEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            textViewEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateStatsDisplay(stats: LibraryStats) {
        textViewStats.text = "📚 ${stats.totalBooks} libros | ✍️ ${stats.totalAuthors} autores | 🌍 ${stats.totalLanguages} idiomas"
        textViewStats.visibility = View.VISIBLE
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun handleError(message: String) {
        showToast(message)
        // Si no hay libros cargados, mostrar mensaje de error
        if (savedBooksList.isEmpty()) {
            textViewEmpty.text = "Error cargando la biblioteca.\nVerifica tu conexión e intenta de nuevo."
            textViewEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Recargar libros cuando se regrese a esta actividad
        // por si se agregó un nuevo libro
        loadSavedBooks()
    }
}