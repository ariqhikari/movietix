package org.ariqhikari.movietix.data.movie.mapper

import org.ariqhikari.movietix.data.movie.remote.dto.CastDto
import org.ariqhikari.movietix.data.movie.remote.dto.CreditsDto
import org.ariqhikari.movietix.data.movie.remote.dto.GenreDto
import org.ariqhikari.movietix.data.movie.remote.dto.MovieDetailDto
import org.ariqhikari.movietix.data.movie.remote.dto.MovieDto
import org.ariqhikari.movietix.data.movie.remote.dto.VideoDto
import org.ariqhikari.movietix.data.movie.remote.dto.VideosDto
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

    @Test
    fun toDetailDomain_mapsAllFieldsCorrectly() {
        val dto = MovieDetailDto(
            id = 693134,
            title = "Dune: Part Two",
            tagline = "Long live the fighters.",
            overview = "Paul Atreides unites with Chani...",
            genres = listOf(GenreDto(878, "Science Fiction"), GenreDto(12, "Adventure")),
            runtime = 166,
            voteAverage = 8.169,
            voteCount = 4900,
            releaseDate = "2024-02-27",
            posterPath = "/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
            backdropPath = "/xOMo8BRK7PfcJv9JCnx7s5200bm.jpg"
        )

        val detail = dto.toDomain()

        assertEquals(693134, detail.id)
        assertEquals("Dune: Part Two", detail.title)
        assertEquals("Long live the fighters.", detail.tagline)
        assertEquals("Paul Atreides unites with Chani...", detail.overview)
        assertEquals(listOf("Science Fiction", "Adventure"), detail.genres)
        assertEquals(166, detail.runtime)
        assertEquals(8.169, detail.voteAverage)
        assertEquals(4900, detail.voteCount)
        assertEquals("2024-02-27", detail.releaseDate)
        assertEquals("https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg", detail.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w1280/xOMo8BRK7PfcJv9JCnx7s5200bm.jpg", detail.backdropUrl)
    }

    @Test
    fun toDetailDomain_handlesNullFieldsGracefully() {
        val dto = MovieDetailDto(
            id = 10,
            title = null,
            tagline = null,
            overview = null,
            genres = null,
            runtime = null,
            voteAverage = null,
            voteCount = null,
            releaseDate = null,
            posterPath = null,
            backdropPath = null
        )

        val detail = dto.toDomain()

        assertEquals(10, detail.id)
        assertEquals("Tanpa Judul", detail.title)
        assertEquals("", detail.tagline)
        assertEquals("", detail.overview)
        assertTrue(detail.genres.isEmpty())
        assertEquals(0, detail.runtime)
        assertEquals(0.0, detail.voteAverage)
        assertEquals(0, detail.voteCount)
        assertEquals("", detail.releaseDate)
        assertNull(detail.posterUrl)
        assertNull(detail.backdropUrl)
    }

    @Test
    fun toCastMembers_mapsAndSortsByOrder() {
        val dto = CreditsDto(
            id = 1,
            cast = listOf(
                CastDto(id = 2, name = "Zendaya", character = "Chani", profilePath = "/profile2.jpg", order = 1),
                CastDto(id = 1, name = "Timothee Chalamet", character = "Paul Atreides", profilePath = "/profile1.jpg", order = 0),
                CastDto(id = 3, name = "Rebecca Ferguson", character = "Lady Jessica", profilePath = null, order = 2)
            )
        )

        val cast = dto.toCastMembers()

        assertEquals(3, cast.size)
        assertEquals("Timothee Chalamet", cast[0].name)
        assertEquals(0, cast[0].order)
        assertEquals("https://image.tmdb.org/t/p/w185/profile1.jpg", cast[0].profileUrl)
        assertEquals("Zendaya", cast[1].name)
        assertEquals(1, cast[1].order)
        assertEquals("Rebecca Ferguson", cast[2].name)
        assertNull(cast[2].profileUrl)
    }

    @Test
    fun toTrailers_filtersYouTubeAndSortsByPriority() {
        val dto = VideosDto(
            id = 1,
            results = listOf(
                VideoDto(id = "v1", key = "vimeo1", name = "Vimeo Video", site = "Vimeo", type = "Trailer"),
                VideoDto(id = "v2", key = "teaserKey", name = "Teaser Video", site = "YouTube", type = "Teaser"),
                VideoDto(id = "v3", key = "clipKey", name = "Clip Video", site = "YouTube", type = "Clip"),
                VideoDto(id = "v4", key = "trailerKey", name = "Official Trailer", site = "YouTube", type = "Trailer"),
                VideoDto(id = "v5", key = "", name = "Empty Key", site = "YouTube", type = "Trailer")
            )
        )

        val trailers = dto.toTrailers()

        assertEquals(3, trailers.size)
        // Trailer comes first, then Teaser, then Clip
        assertEquals("trailerKey", trailers[0].key)
        assertEquals("Trailer", trailers[0].type)
        assertEquals("https://www.youtube.com/watch?v=trailerKey", trailers[0].youtubeWatchUrl)

        assertEquals("teaserKey", trailers[1].key)
        assertEquals("Teaser", trailers[1].type)

        assertEquals("clipKey", trailers[2].key)
        assertEquals("Clip", trailers[2].type)
    }
}
