package org.ariqhikari.movietix.domain.movie.model

data class TrailerVideo(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val youtubeWatchUrl: String
)
