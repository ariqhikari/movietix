package org.ariqhikari.movietix.data.movie.mapper

import org.ariqhikari.movietix.data.movie.remote.dto.MovieDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MovieMapperTest {

    @Test
    fun toDomain_mapsAllFieldsCorrectlyWithFullUrls() {
        val dto = MovieDto(
            id = 550,
            title = "Fight Club",
            overview = "An insomniac office worker...",
            posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
            backdropPath = "/hZkgoQYus5vegHoetLkCJzb17zJ.jpg",
            releaseDate = "1999-10-15",
            voteAverage = 8.433,
            voteCount = 26280,
            genreIds = listOf(18, 53)
        )

        val genreMap = mapOf(18 to "Drama", 53 to "Thriller")
        val movie = dto.toDomain(genreMap)

        assertEquals(550, movie.id)
        assertEquals("Fight Club", movie.title)
        assertEquals("An insomniac office worker...", movie.overview)
        assertEquals("https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg", movie.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w1280/hZkgoQYus5vegHoetLkCJzb17zJ.jpg", movie.backdropUrl)
        assertEquals("1999-10-15", movie.releaseDate)
        assertEquals(8.433, movie.voteAverage)
        assertEquals(26280, movie.voteCount)
        assertEquals(listOf("Drama", "Thriller"), movie.genres)
    }

    @Test
    fun toDomain_handlesMissingSlashInPosterAndBackdropPaths() {
        val dto = MovieDto(
            id = 1,
            title = "Movie",
            overview = "",
            posterPath = "poster.jpg",
            backdropPath = "backdrop.jpg",
            releaseDate = "",
            voteAverage = 7.0,
            voteCount = 10,
            genreIds = emptyList()
        )

        val movie = dto.toDomain()

        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", movie.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w1280/backdrop.jpg", movie.backdropUrl)
    }

    @Test
    fun toDomain_handlesNullAndBlankFieldsGracefully() {
        val dto = MovieDto(
            id = 2,
            title = null,
            overview = null,
            posterPath = "",
            backdropPath = null,
            releaseDate = null,
            voteAverage = null,
            voteCount = null,
            genreIds = null
        )

        val movie = dto.toDomain()

        assertEquals(2, movie.id)
        assertEquals("Tanpa Judul", movie.title)
        assertEquals("", movie.overview)
        assertNull(movie.posterUrl)
        assertNull(movie.backdropUrl)
        assertEquals("", movie.releaseDate)
        assertEquals(0.0, movie.voteAverage)
        assertEquals(0, movie.voteCount)
        assertTrue(movie.genres.isEmpty())
    }

    @Test
    fun toDomain_filtersUnknownGenreIds() {
        val dto = MovieDto(
            id = 3,
            title = "Genre Test",
            overview = "",
            posterPath = null,
            backdropPath = null,
            releaseDate = "",
            voteAverage = 6.0,
            voteCount = 5,
            genreIds = listOf(999, 18)
        )

        val genreMap = mapOf(18 to "Drama")
        val movie = dto.toDomain(genreMap)

        assertEquals(listOf("Drama"), movie.genres)
    }
}
