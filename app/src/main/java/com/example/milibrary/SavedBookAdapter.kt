package com.example.milibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SavedBookAdapter(
    private val bookList: List<SavedBook>,
    private val onItemClick: (SavedBook) -> Unit
) : RecyclerView.Adapter<SavedBookAdapter.SavedBookViewHolder>() {

    inner class SavedBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textViewTitle)
        val author: TextView = itemView.findViewById(R.id.textViewAuthor)
        val thumbnail: ImageView = itemView.findViewById(R.id.imageViewCover)
        val dateAdded: TextView = itemView.findViewById(R.id.textViewDateAdded)
        val publisher: TextView = itemView.findViewById(R.id.textViewPublisher)
        val pageCount: TextView = itemView.findViewById(R.id.textViewPageCount)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(bookList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedBookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_book, parent, false)
        return SavedBookViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedBookViewHolder, position: Int) {
        val book = bookList[position]

        holder.title.text = book.title
        holder.author.text = book.authors ?: "Autor desconocido"
        holder.dateAdded.text = "Agregado: ${book.getFormattedDateAdded()}"

        // Mostrar editorial si está disponible
        if (!book.publisher.isNullOrBlank()) {
            holder.publisher.text = book.publisher
            holder.publisher.visibility = View.VISIBLE
        } else {
            holder.publisher.visibility = View.GONE
        }

        // Mostrar número de páginas si está disponible
        if (book.pageCount != null && book.pageCount > 0) {
            holder.pageCount.text = "${book.pageCount} páginas"
            holder.pageCount.visibility = View.VISIBLE
        } else {
            holder.pageCount.visibility = View.GONE
        }

        // Cargar imagen de portada
        val imageUrl = book.getSecureImageUrl()
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_book)
            .error(R.drawable.error_image)
            .into(holder.thumbnail)
    }

    override fun getItemCount(): Int = bookList.size
}