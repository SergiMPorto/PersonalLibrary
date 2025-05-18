package com.example.milibrary

data class BookResponse(
    val items: List<Book>
)

data class Book(
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String,
    val authors: List<String>,
    val imageLinks: ImageLinks?,
    val description: String,
    val categories: List<String>,
    val publisher: String,
    val publishedDate: String,
    val pageCount: Int,
    val language: String,
    val isbn: String,
)

data class ImageLinks(
    val thumbnail: String
)
