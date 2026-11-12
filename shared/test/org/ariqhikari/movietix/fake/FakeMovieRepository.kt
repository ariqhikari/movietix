package org.ariqhikari.movietix.fake

import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.movie.model.CastMember
import org.ariqhikari.movietix.domain.movie.model.Movie
import org.ariqhikari.movietix.domain.movie.model.MovieDetail
import org.ariqhikari.movietix.domain.movie.model.TrailerVideo
import org.ariqhikari.movietix.domain.movie.repository.MovieRepository

class FakeMovieRepository : MovieRepository {

    var nowPlayingResult: AppResult<List<Movie>> = AppResult.Success(emptyList())
    var popularResult: AppResult<List<Movie>> = AppResult.Success(emptyList())
    var upcomingResult: AppResult<List<Movie>> = AppResult.Success(emptyList())

    var movieDetailResult: AppResult<MovieDetail>? = null
    var movieCreditsResult: AppResult<List<CastMember>> = AppResult.Success(emptyList())
    var movieTrailersResult: AppResult<List<TrailerVideo>> = AppResult.Success(emptyList())

    var lastRequestedNowPlayingPage: Int? = null
    var lastRequestedPopularPage: Int? = null
    var lastRequestedUpcomingPage: Int? = null
    var lastRequestedMovieDetailId: Int? = null
    var lastRequestedCreditsMovieId: Int? = null
    var lastRequestedTrailersMovieId: Int? = null

    override suspend fun getNowPlayingMovies(page: Int): AppResult<List<Movie>> {
        lastRequestedNowPlayingPage = page
        return nowPlayingResult
    }

    override suspend fun getPopularMovies(page: Int): AppResult<List<Movie>> {
        lastRequestedPopularPage = page
        return popularResult
    }

    override suspend fun getUpcomingMovies(page: Int): AppResult<List<Movie>> {
        lastRequestedUpcomingPage = page
        return upcomingResult
    }

    override suspend fun getMovieDetail(movieId: Int): AppResult<MovieDetail> {
        lastRequestedMovieDetailId = movieId
        return movieDetailResult ?: AppResult.Success(createDummyMovieDetail(id = movieId))
    }

    override suspend fun getMovieCredits(movieId: Int): AppResult<List<CastMember>> {
        lastRequestedCreditsMovieId = movieId
        return movieCreditsResult
    }

    override suspend fun getMovieTrailers(movieId: Int): AppResult<List<TrailerVideo>> {
        lastRequestedTrailersMovieId = movieId
        return movieTrailersResult
    }

    companion object {
        fun createDummyMovie(
            id: Int = 1,
            title: String = "Test Movie $id",
            voteAverage: Double = 8.5,
            genres: List<String> = listOf("Action", "Drama")
        ): Movie {
            return Movie(
                id = id,
                title = title,
                overview = "Overview of $title",
                posterUrl = "https://image.tmdb.org/t/p/w500/poster_$id.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/w780/backdrop_$id.jpg",
                releaseDate = "2026-05-15",
                voteAverage = voteAverage,
                voteCount = 100,
                genres = genres
            )
        }

        fun createDummyMovieDetail(
            id: Int = 1,
            movieId: Int = id,
            title: String = "Test Movie Detail $movieId",
            runtime: Int = 166,
            voteAverage: Double = 8.6,
            genres: List<String> = listOf("Sci-Fi", "Action")
        ): MovieDetail {
            val resolvedId = if (movieId != 1) movieId else id
            return MovieDetail(
                id = resolvedId,
                title = title,
                tagline = "The epic journey continues",
                overview = "A detailed overview for $title",
                genres = genres,
                runtime = runtime,
                voteAverage = voteAverage,
                voteCount = 12400,
                releaseDate = "2024-03-01",
                posterUrl = "https://image.tmdb.org/t/p/w500/poster_$resolvedId.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/w1280/backdrop_$resolvedId.jpg"
            )
        }

        fun createDummyCastMember(
            id: Int = 1,
            name: String = "Actor $id",
            character: String = "Character $id",
            order: Int = 0
        ): CastMember {
            return CastMember(
                id = id,
                name = name,
                character = character,
                profileUrl = "https://image.tmdb.org/t/p/w185/profile_$id.jpg",
                order = order
            )
        }

        fun createDummyTrailerVideo(
            id: String = "trailer_1",
            key: String = "dQw4w9WgXcQ",
            name: String = "Official Trailer",
            type: String = "Trailer"
        ): TrailerVideo {
            return TrailerVideo(
                id = id,
                key = key,
                name = name,
                site = "YouTube",
                type = type,
                youtubeWatchUrl = "https://www.youtube.com/watch?v=$key"
            )
        }
    }
}
