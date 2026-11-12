package org.ariqhikari.movietix.data.movie.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ariqhikari.movietix.core.data.network.safeApiCall
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.data.movie.mapper.MovieMapper
import org.ariqhikari.movietix.data.movie.remote.TmdbApiService
import org.ariqhikari.movietix.domain.movie.model.CastMember
import org.ariqhikari.movietix.domain.movie.model.Movie
import org.ariqhikari.movietix.domain.movie.model.MovieDetail
import org.ariqhikari.movietix.domain.movie.model.TrailerVideo
import org.ariqhikari.movietix.domain.movie.repository.MovieRepository

class MovieRepositoryImpl(
    private val apiService: TmdbApiService
) : MovieRepository {

    private val genreMutex = Mutex()
    private var genreCache: Map<Int, String>? = null

    private suspend fun getGenreMap(): Map<Int, String> {
        genreCache?.let { return it }
        return genreMutex.withLock {
            genreCache?.let { return it }
            try {
                val response = apiService.getGenres()
                val map = response.genres.associate { it.id to it.name }
                genreCache = map
                map
            } catch (_: Throwable) {
                emptyMap()
            }
        }
    }

    override suspend fun getNowPlayingMovies(page: Int): AppResult<List<Movie>> {
        return safeApiCall {
            val genreMap = getGenreMap()
            val response = apiService.getNowPlayingMovies(page = page)
            MovieMapper.toDomainList(response.results, genreMap)
        }
    }

    override suspend fun getPopularMovies(page: Int): AppResult<List<Movie>> {
        return safeApiCall {
            val genreMap = getGenreMap()
            val response = apiService.getPopularMovies(page = page)
            MovieMapper.toDomainList(response.results, genreMap)
        }
    }

    override suspend fun getUpcomingMovies(page: Int): AppResult<List<Movie>> {
        return safeApiCall {
            val genreMap = getGenreMap()
            val response = apiService.getUpcomingMovies(page = page)
            MovieMapper.toDomainList(response.results, genreMap)
        }
    }

    override suspend fun getMovieDetail(movieId: Int): AppResult<MovieDetail> {
        return safeApiCall {
            val response = apiService.getMovieDetail(movieId = movieId)
            MovieMapper.toDetailDomain(response)
        }
    }

    override suspend fun getMovieCredits(movieId: Int): AppResult<List<CastMember>> {
        return safeApiCall {
            val response = apiService.getMovieCredits(movieId = movieId)
            MovieMapper.toCastListDomain(response)
        }
    }

    override suspend fun getMovieTrailers(movieId: Int): AppResult<List<TrailerVideo>> {
        return safeApiCall {
            val response = apiService.getMovieVideos(movieId = movieId)
            MovieMapper.toTrailerListDomain(response)
        }
    }
}
