package com.example.milibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BookAdapter(
    private val bookList: List<Book>,
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textViewTitle)
        val author: TextView = itemView.findViewById(R.id.textViewAuthor)
        val thumbnail: ImageView = itemView.findViewById(R.id.imageViewCover)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(bookList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.activity_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        holder.title.text = book.volumeInfo.title ?: "Título no disponible"
        holder.author.text = book.volumeInfo.authors?.joinToString(", ") ?: "Autor desconocido"

        val rawThumbnailUrl = book.volumeInfo.imageLinks?.thumbnail
        val thumbnailUrl = rawThumbnailUrl?.replace("http://", "https://")

       Glide.with(holder.itemView.context)
            .load(thumbnailUrl)
            .placeholder(R.drawable.placeholder_book)
            .error(R.drawable.error_image)
            .into(holder.thumbnail)
    }


    override fun getItemCount(): Int = bookList.size
}
