package com.example.milibrary

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val book = intent.getParcelableExtra<Book>("book")

        book?.let {
            setupBookDetails(it)
        } ?: run {
            finish() // Cierra la actividad si no hay libro
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
         textPublishedDate.text = "Fecha de publicación: ${book.volumeInfo.publishedDate ?: "No disponible"}"
         textPageCount.text = "Páginas: ${book.volumeInfo.pageCount ?: "No especificado"}"
         textLanguage.text = "Idioma: ${book.volumeInfo.language?.let { getLanguageName(it) }}"

         // Load book cover image
         loadBookCover(book, imageViewCover)
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
}

