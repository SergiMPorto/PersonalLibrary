package com.example.milibrary

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.milibrary.ui.theme.MiLibraryTheme
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response

class SearchActivity : ComponentActivity() {

    private lateinit var editTextBookTitle: EditText
    private lateinit var buttonSearchByTitle: Button
    private lateinit var buttonScanISBN: Button

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
}
