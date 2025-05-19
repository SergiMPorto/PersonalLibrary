package com.example.milibrary

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val book = intent.getParcelableExtra<Book>("book")

        val imageViewCover = findViewById<ImageView>(R.id.imageViewCoverDetail)
        val textTitle = findViewById<TextView>(R.id.textViewTitleDetail)
        val textAuthor = findViewById<TextView>(R.id.textViewAuthorDetail)
        val textDescription = findViewById<TextView>(R.id.textViewDescriptionDetail)
        val textPublisher = findViewById<TextView>(R.id.textViewPublisherDetail)
        val textPublishedDate = findViewById<TextView>(R.id.textViewPublishedDateDetail)
        val textPageCount = findViewById<TextView>(R.id.textViewPageCountDetail)
        val textLanguage = findViewById<TextView>(R.id.textViewLanguageDetail)

        book?.let {
            textTitle.text = book.volumeInfo.title ?: "Unknown Title"
            textAuthor.text = book.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"
            textDescription.text = book.volumeInfo.description ?: "No description available"
            textPublisher.text = "Editorial: ${book.volumeInfo.publisher ?: "Unknown"}"
            textPublishedDate.text = "Fecha de publicación: ${book.volumeInfo.publishedDate ?: "-"}"
            textPageCount.text = "Páginas: ${book.volumeInfo.pageCount ?: 0}"
            textLanguage.text = "Idioma: ${book.volumeInfo.language ?: "-"}"


            val rawImageUrl = book.volumeInfo.imageLinks?.thumbnail
            val imageUrl = rawImageUrl?.replace("http://", "https://")

            if (imageUrl != null) {
                Glide.with(this).load(imageUrl).into(imageViewCover)
            } else {
                imageViewCover.setImageResource(R.drawable.ic_book_cover_placeholder)
            }


        }
    }
}
