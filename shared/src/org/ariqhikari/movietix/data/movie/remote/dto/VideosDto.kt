package org.ariqhikari.movietix.data.movie.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideosDto(
    @SerialName("id") val id: Int,
    @SerialName("results") val results: List<VideoDto>? = null
)

@Serializable
data class VideoDto(
    @SerialName("id") val id: String,
    @SerialName("key") val key: String,
    @SerialName("name") val name: String? = null,
    @SerialName("site") val site: String? = null,
    @SerialName("type") val type: String? = null
)
