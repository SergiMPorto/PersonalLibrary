package com.example.milibrary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class BookResponse(
    val items: List<Book>
)

@Parcelize
data class Book(
    val volumeInfo: VolumeInfo
) : Parcelable

@Parcelize
data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val imageLinks: ImageLinks?,
    val description: String?,
    val categories: List<String>?,
    val publisher: String?,
    val publishedDate: String?,
    val pageCount: Int?,
    val language: String?,
    val industryIdentifiers: List<IndustryIdentifier>?
) : Parcelable


@Parcelize
data class IndustryIdentifier(
    val type: String,
    val identifier: String
) : Parcelable

@Parcelize
data class ImageLinks(
    val thumbnail: String
) : Parcelable //