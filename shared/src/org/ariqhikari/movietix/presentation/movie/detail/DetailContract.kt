package org.ariqhikari.movietix.presentation.movie.detail

import org.ariqhikari.movietix.domain.movie.model.CastMember
import org.ariqhikari.movietix.domain.movie.model.MovieDetail
import org.ariqhikari.movietix.domain.movie.model.TrailerVideo

data class DetailState(
    val movieId: Int = 0,
    val isLoadingDetail: Boolean = true,
    val movieDetail: MovieDetail? = null,
    val detailError: String? = null,
    val isLoadingCredits: Boolean = true,
    val castMembers: List<CastMember> = emptyList(),
    val isLoadingTrailers: Boolean = true,
    val trailers: List<TrailerVideo> = emptyList(),
    val isSynopsisExpanded: Boolean = false
)

sealed interface DetailAction {
    data object RetryAll : DetailAction
    data object RetryDetail : DetailAction
    data object ToggleSynopsis : DetailAction
    data class PlayTrailer(val trailer: TrailerVideo) : DetailAction
    data object SelectScheduleClicked : DetailAction
    data object BackClicked : DetailAction
}

sealed interface DetailEffect {
    data object NavigateBack : DetailEffect
    data class NavigateToSchedule(
        val movieId: Int,
        val movieTitle: String,
        val posterUrl: String?
    ) : DetailEffect
    data class OpenUrl(val url: String) : DetailEffect
    data class ShowSnackbar(val message: String) : DetailEffect
}

object DetailContract {
    typealias State = DetailState
    typealias Action = DetailAction
    typealias Effect = DetailEffect
}
