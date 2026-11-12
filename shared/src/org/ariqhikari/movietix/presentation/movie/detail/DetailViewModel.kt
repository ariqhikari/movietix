package org.ariqhikari.movietix.presentation.movie.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.domain.movie.usecase.GetMovieCreditsUseCase
import org.ariqhikari.movietix.domain.movie.usecase.GetMovieDetailUseCase
import org.ariqhikari.movietix.domain.movie.usecase.GetMovieTrailersUseCase

class DetailViewModel(
    private val movieId: Int,
    private val getMovieDetailUseCase: GetMovieDetailUseCase,
    private val getMovieCreditsUseCase: GetMovieCreditsUseCase,
    private val getMovieTrailersUseCase: GetMovieTrailersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState(movieId = movieId))
    val state: StateFlow<DetailState> = _state.asStateFlow()

    private val _effect = Channel<DetailEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadAll()
    }

    fun onAction(action: DetailAction) {
        when (action) {
            is DetailAction.RetryAll -> loadAll()
            is DetailAction.RetryDetail -> loadMovieDetail()
            is DetailAction.ToggleSynopsis -> {
                _state.update { it.copy(isSynopsisExpanded = !it.isSynopsisExpanded) }
            }
            is DetailAction.PlayTrailer -> {
                sendEffect(DetailEffect.OpenUrl(action.trailer.youtubeWatchUrl))
            }
            is DetailAction.SelectScheduleClicked -> {
                val detail = _state.value.movieDetail
                sendEffect(
                    DetailEffect.NavigateToSchedule(
                        movieId = movieId,
                        movieTitle = detail?.title ?: "Film",
                        posterUrl = detail?.posterUrl
                    )
                )
            }
            is DetailAction.BackClicked -> {
                sendEffect(DetailEffect.NavigateBack)
            }
        }
    }

    private fun loadAll() {
        loadMovieDetail()
        loadMovieCredits()
        loadMovieTrailers()
    }

    private fun loadMovieDetail() {
        _state.update { it.copy(isLoadingDetail = true, detailError = null) }
        viewModelScope.launch {
            when (val result = getMovieDetailUseCase(movieId)) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoadingDetail = false,
                            movieDetail = result.data,
                            detailError = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoadingDetail = false,
                            detailError = formatErrorMessage(result.error)
                        )
                    }
                }
            }
        }
    }

    private fun loadMovieCredits() {
        _state.update { it.copy(isLoadingCredits = true) }
        viewModelScope.launch {
            when (val result = getMovieCreditsUseCase(movieId)) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoadingCredits = false,
                            castMembers = result.data.take(12)
                        )
                    }
                }
                is AppResult.Error -> {
                    // Supporting data failure does not break the screen
                    _state.update {
                        it.copy(
                            isLoadingCredits = false,
                            castMembers = emptyList()
                        )
                    }
                }
            }
        }
    }

    private fun loadMovieTrailers() {
        _state.update { it.copy(isLoadingTrailers = true) }
        viewModelScope.launch {
            when (val result = getMovieTrailersUseCase(movieId)) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoadingTrailers = false,
                            trailers = result.data
                        )
                    }
                }
                is AppResult.Error -> {
                    // Supporting data failure does not break the screen
                    _state.update {
                        it.copy(
                            isLoadingTrailers = false,
                            trailers = emptyList()
                        )
                    }
                }
            }
        }
    }

    private fun sendEffect(effect: DetailEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun formatErrorMessage(error: AppError): String {
        return when (error) {
            is AppError.Network.NoInternet -> "Periksa koneksi internet Anda dan coba lagi."
            is AppError.Network.Timeout -> "Koneksi terputus. Silakan coba lagi."
            is AppError.Network.ServerError -> "Terjadi kendala pada server (${error.code})."
            is AppError.Network.Unauthorized -> "Akses tidak diizinkan."
            is AppError.Storage.ReadFailed,
            is AppError.Storage.WriteFailed -> "Gagal memuat data lokal."
            is AppError.Booking.SeatAlreadyOccupied,
            is AppError.Booking.PaymentFailedSimulated,
            is AppError.Booking.SaveTicketFailed,
            is AppError.Booking.RollbackFailed,
            is AppError.Booking.InvalidSelection -> "Kendala pada pemesanan."
            is AppError.Unknown -> "Terjadi kesalahan. Silakan coba lagi."
        }
    }
}
