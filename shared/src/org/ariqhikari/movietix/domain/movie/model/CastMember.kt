package org.ariqhikari.movietix.domain.movie.model

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profileUrl: String?,
    val order: Int
)
