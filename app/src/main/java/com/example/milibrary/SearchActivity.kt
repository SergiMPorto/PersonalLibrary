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

        // Initialize views
        editTextBookTitle = findViewById(R.id.editTextBookTitle)
        buttonSearchByTitle = findViewById(R.id.buttonSearchByTitle)
        buttonScanISBN = findViewById(R.id.buttonScanISBN)

        // Search by title
        buttonSearchByTitle.setOnClickListener {
            val bookTitle = editTextBookTitle.text.toString()
            searchBookByTitle(bookTitle)
        }

        // Search by ISBN
        buttonScanISBN.setOnClickListener {
            val isbn = editTextBookTitle.text.toString()
            searchBooksByISBN(isbn)
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        setupRecyclerView()

    }


    // Function to search for books by ISBN
    private fun searchBooksByISBN(isbn: String) {
        RetrofitClient.googleBooksService.getBookByISBN(isbn).enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    val bookList = response.body()?.items
                    if (bookList != null && bookList.isNotEmpty()) {

                    } else {
                        Toast.makeText(this@SearchActivity, "No books found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SearchActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                Toast.makeText(this@SearchActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to search for books by title
    private fun searchBookByTitle(title: String) {
        RetrofitClient.googleBooksService.getBookByTitle(title).enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    val bookList = response.body()?.items
                    if (bookList != null && bookList.isNotEmpty()) {
                        booksList.clear()
                        booksList.addAll(bookList)
                        bookAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@SearchActivity, "No books found", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this@SearchActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                Toast.makeText(this@SearchActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(booksList) { selectedBook ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("book", selectedBook)
            startActivity(intent)
        }
        recyclerView.adapter = bookAdapter
    }

}
