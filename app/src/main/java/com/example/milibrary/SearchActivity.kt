package com.example.milibrary

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response

class SearchActivity : ComponentActivity() {

    private lateinit var editTextBookTitle: EditText
    private lateinit var buttonSearchByTitle: Button
    private lateinit var buttonScanISBN: Button
    private lateinit var bookAdapter: BookAdapter
    private val booksList = mutableListOf<Book>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
    }

      private  fun initializeViews() {
            editTextBookTitle = findViewById(R.id.editTextBookTitle)
            buttonSearchByTitle = findViewById(R.id.buttonSearchByTitle)
            buttonScanISBN = findViewById(R.id.buttonScanISBN)
            recyclerView = findViewById(R.id.recyclerView)
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
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(booksList) { selectedBook ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("book", selectedBook)
            startActivity(intent)
        }
        recyclerView.adapter = bookAdapter
    }








    // Function to search for books by ISBN
    private fun searchBooksByISBN(isbn: String) {
        showToast("Buscando libros con ISBN: $isbn")
        RetrofitClient.googleBooksService.getBookByISBN("isbn:$isbn").enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                handleBookResponse(response)

            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                Toast.makeText(this@SearchActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to search for books by title
    private fun searchBookByTitle(title: String) {
        RetrofitClient.googleBooksService.getBookByTitle(title).enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
               handleBookResponse(response)
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                Toast.makeText(this@SearchActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
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
            showToast("Se encontraron ${bookList?.size} libros")
        }else{
            booksList.clear()
            bookAdapter.notifyDataSetChanged()
            showToast("Error en la respuesta del servidor")
        }
    }else{
        showToast("Error en la respuesta del servidor")
    }
    }

private fun showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}


}
