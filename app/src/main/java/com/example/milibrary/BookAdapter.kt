package com.example.milibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BookAdapter(private val books: List<Book>) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book)
    }

    override fun getItemCount() = books.size

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        private val authorTextView: TextView = itemView.findViewById(R.id.textViewAuthor)
        private val coverImageView: ImageView = itemView.findViewById(R.id.imageViewCover)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)
        private val publisherTextView: TextView = itemView.findViewById(R.id.textViewPublisher)
        private val publishedDateTextView: TextView = itemView.findViewById(R.id.textViewPublishedDate)
        private val pageCountTextView: TextView = itemView.findViewById(R.id.textViewPageCount)
        private val languageTextView: TextView = itemView.findViewById(R.id.textViewLanguage)
        private val isbnTextView: TextView = itemView.findViewById(R.id.textViewIsbn)



        fun bind(book: Book) {
            titleTextView.text = book.volumeInfo.title
            authorTextView.text = book.volumeInfo.authors?.joinToString(", ") ?: "Unknown"
            descriptionTextView.text = book.volumeInfo.description ?: "No description available"
            publisherTextView.text = book.volumeInfo.publisher ?: "Unknown publisher"
            publishedDateTextView.text = book.volumeInfo.publishedDate ?: "Unknown date"
            pageCountTextView.text = "Pages: ${book.volumeInfo.pageCount ?: "N/A"}"
            languageTextView.text = "Language: ${book.volumeInfo.language ?: "Unknown"}"
            isbnTextView.text = "ISBN: ${book.volumeInfo.isbn ?: "N/A"}"


            Glide.with(itemView.context)
                .load(book.volumeInfo.imageLinks?.thumbnail)
                .into(coverImageView)
        }
    }
}