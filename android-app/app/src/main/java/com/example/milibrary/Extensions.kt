package com.example.milibrary



// Extensión para convertir Book (Google Books) a BookCreateRequest (tu API)
fun Book.toBookCreateRequest(): BookCreateRequest {
    val isbn10 = volumeInfo.industryIdentifiers?.find { it.type == "ISBN_10" }?.identifier
    val isbn13 = volumeInfo.industryIdentifiers?.find { it.type == "ISBN_13" }?.identifier

    return BookCreateRequest(
        googleBooksId = null, // Ajustar según tu modelo Book
        title = volumeInfo.title ?: "",
        authors = volumeInfo.authors?.joinToString(", "),
        description = volumeInfo.description,
        thumbnailUrl = volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"),
        publisher = volumeInfo.publisher,
        publishedDate = volumeInfo.publishedDate,
        pageCount = volumeInfo.pageCount,
        language = volumeInfo.language,
        isbn10 = isbn10,
        isbn13 = isbn13,
        categories = volumeInfo.categories?.joinToString(", ")
    )
}

//Function for build a unique id for book
private fun Book.generateGoogleBooksId(): String {
    val title = volumeInfo.title?.replace(" ", "_")?.lowercase() ?: "unknown"
    val author = volumeInfo.authors?.firstOrNull()?.replace(" ", "_")?.lowercase() ?: "unknown"
    val isbn = volumeInfo.industryIdentifiers?.firstOrNull()?.identifier ?: System.currentTimeMillis().toString()

    return "${title}_${author}_${isbn}".take(100) // Limitar a 100 caracteres
}


// Extensión para convertir SavedBook (tu API) a Book (para mostrar en UI existente)
fun SavedBook.toDisplayBook(): Book {
    val industryIdentifiers = mutableListOf<IndustryIdentifier>()

    isbn10?.let { industryIdentifiers.add(IndustryIdentifier("ISBN_10", it)) }
    isbn13?.let { industryIdentifiers.add(IndustryIdentifier("ISBN_13", it)) }

    return Book(
        volumeInfo = VolumeInfo(
            title = this.title,
            authors = this.authors?.split(", ")?.filter { it.isNotBlank() },
            description = this.description,
            imageLinks = this.thumbnailUrl?.let { ImageLinks(it) },
            publisher = this.publisher,
            publishedDate = this.publishedDate,
            pageCount = this.pageCount,
            language = this.language,
            industryIdentifiers = industryIdentifiers.takeIf { it.isNotEmpty() },
            categories = this.categories?.split(", ")?.filter { it.isNotBlank() }
        )
    )
}

// Funtion for format date
fun SavedBook.getFormattedDateAdded(): String {
    return try {
        val isoDate = dateAdded.split("T")[0]
        val parts = isoDate.split("-")
        "${parts[2]}/${parts[1]}/${parts[0]}" // dd/mm/yyyy
    } catch (e: Exception) {
        dateAdded
    }
}

// Función for format language
fun SavedBook.getLanguageName(): String {
    return when (language) {
        "en" -> "Inglés"
        "es" -> "Español"
        "fr" -> "Francés"
        "de" -> "Alemán"
        "it" -> "Italiano"
        "pt" -> "Portugués"
        null -> "No especificado"
        else -> language ?: "No especificado"
    }
}
// Función para obtener el estado de lectura en español
fun SavedBook.getReadingStatusText(): String {
    // Nota: Esta función asume que tienes acceso a los datos de book_reviews
    // Si necesitas implementar esto, necesitarás agregar estos campos al modelo SavedBook
    return "Por leer" // Valor por defecto
}

// Función para truncar descripción si es muy larga
fun SavedBook.getTruncatedDescription(maxLength: Int = 200): String {
    return if (description != null && description.length > maxLength) {
        "${description.take(maxLength)}..."
    } else {
        description ?: "Sin descripción disponible"
    }
}

// Función para obtener el año de publicación
fun SavedBook.getPublicationYear(): String {
    return try {
        publishedDate?.take(4) ?: "Año desconocido"
    } catch (e: Exception) {
        "Año desconocido"
    }
}

// Función para verificar si un libro tiene imagen
fun SavedBook.hasImage(): Boolean {
    return !thumbnailUrl.isNullOrBlank()
}

// Función para obtener la URL de imagen con protocolo seguro
fun SavedBook.getSecureImageUrl(): String? {
    return thumbnailUrl?.replace("http://", "https://")}
