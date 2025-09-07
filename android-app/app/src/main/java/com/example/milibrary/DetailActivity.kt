package com.example.milibrary

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailActivity : AppCompatActivity() {

    private lateinit var buttonSaveBook: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)


        //iniciar variables
        buttonSaveBook = findViewById(R.id.buttonSaveBook)



        // Recuperar el libro pasado por intent y mostrar detalles si existe, o cerrar la actividad si no
        val book = intent.getParcelableExtra<Book>("book")

        book?.let {
            setupBookDetails(it)  // Show details of the book
            setupSaveButton(it)   // Prepare save button
        } ?: run {
            finish() // Close detail activity if book is null
        }
    }




    private fun setupBookDetails(book: Book) {
        val imageViewCover = findViewById<ImageView>(R.id.imageViewCoverDetail)
        val textTitle = findViewById<TextView>(R.id.textViewTitleDetail)
        val textAuthor = findViewById<TextView>(R.id.textViewAuthorDetail)
        val textDescription = findViewById<TextView>(R.id.textViewDescriptionDetail)
        val textPublisher = findViewById<TextView>(R.id.textViewPublisherDetail)
        val textPublishedDate = findViewById<TextView>(R.id.textViewPublishedDateDetail)
        val textPageCount = findViewById<TextView>(R.id.textViewPageCountDetail)
        val textLanguage = findViewById<TextView>(R.id.textViewLanguageDetail)

        // Set book information with fallbacks
        textTitle.text = book.volumeInfo.title ?: "Título no disponible"
        textAuthor.text = book.volumeInfo.authors?.joinToString(", ") ?: "Autor desconocido"
        textDescription.text = book.volumeInfo.description ?: "Descripción no disponible"
        textPublisher.text = "Editorial: ${book.volumeInfo.publisher ?: "Desconocida"}"
        textPublishedDate.text =
            "Fecha de publicación: ${book.volumeInfo.publishedDate ?: "No disponible"}"
        textPageCount.text = "Páginas: ${book.volumeInfo.pageCount ?: "No especificado"}"
        textLanguage.text = "Idioma: ${book.volumeInfo.language?.let { getLanguageName(it) }}"

        // Load book cover image
        loadBookCover(book, imageViewCover)
    }

    private fun setupSaveButton(book: Book) {
        buttonSaveBook.setOnClickListener {
      saveBookToMyLibrary(book)
        }

    }
    // For save book in api
    private fun saveBookToMyLibrary(book: Book) {
    buttonSaveBook.isEnabled = false
    buttonSaveBook.text = "Guardando..."

    //Convertir Book a BookCreateRequest
    val bookCreateRequest = book.toBookCreateRequest()

//call api for save book
        RetrofitClient.myLibraryService.saveBook(bookCreateRequest).enqueue(object : Callback<SavedBook> {
        override fun onResponse(call: Call<SavedBook>, response: Response<SavedBook>) {
            runOnUiThread {
                if (response.isSuccessful) {
                    val savedBook = response.body()
                    savedBook?.let {
                        showToast("Libro guardado en Mi Biblioteca")
                        buttonSaveBook.text = "✓ Guardado en Mi Biblioteca"
                        buttonSaveBook.setBackgroundColor(getColor(android.R.color.holo_green_dark))

                    } ?: run {
                        handleSaveError("Respuesta del servidor vacía")
                    }
                } else {
                    when (response.code()) {
                        409 -> {
                            // Conflicto - el libro ya existe
                            showToast("Este libro ya está en tu biblioteca")
                            buttonSaveBook.text = "Ya está en Mi Biblioteca"
                            buttonSaveBook.setBackgroundColor(getColor(android.R.color.holo_orange_dark))
                        }
                        404 -> handleSaveError("Servicio no encontrado")
                        500 -> handleSaveError("Error interno del servidor")
                        else -> handleSaveError("Error del servidor: ${response.code()}")

                    }
                }
            }
        }



        override fun onFailure(call: Call<SavedBook>, t: Throwable) {
            runOnUiThread {
                val errorMessage = when {
                    t.message?.contains("timeout") == true -> "Tiempo de espera agotado"
                    t.message?.contains("Unable to resolve host") == true -> "Sin conexión a internet"
                    t.message?.contains("Connection refused") == true -> "Servidor no disponible"
                    else -> "Error de conexión: ${t.message}"
                }
                handleSaveError(errorMessage)
            }
        }
    })
}
private fun handleSaveError(errorMessage: String) {
    showToast(errorMessage)
    buttonSaveBook.isEnabled = true
    buttonSaveBook.text = "Guardar en Mi Biblioteca"
    buttonSaveBook.setBackgroundColor(getColor(R.color.teal_200))
}

    private fun loadBookCover(book: Book, imageViewCover: ImageView) {
            val rawImageUrl = book.volumeInfo.imageLinks?.thumbnail
            val imageUrl = rawImageUrl?.replace("http://", "https://")

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_book_cover_placeholder)
            .error(R.drawable.ic_book_cover_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageViewCover)
        }

    private fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "Inglés"
            "es" -> "Español"
            "fr" -> "Francés"
            "de" -> "Alemán"
            "it" -> "Italiano"
            "pt" -> "Portugués"
            null -> "No disponible"
            else -> languageCode
        }
    }
private fun showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

}


