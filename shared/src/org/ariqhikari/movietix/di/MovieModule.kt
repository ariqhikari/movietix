package org.ariqhikari.movietix.di

import org.ariqhikari.movietix.data.movie.remote.TmdbApiService
import org.ariqhikari.movietix.data.movie.repository.MovieRepositoryImpl
import org.ariqhikari.movietix.domain.movie.repository.MovieRepository
import org.ariqhikari.movietix.domain.movie.usecase.GetNowPlayingMoviesUseCase
import org.ariqhikari.movietix.domain.movie.usecase.GetPopularMoviesUseCase
import org.ariqhikari.movietix.domain.movie.usecase.GetUpcomingMoviesUseCase
import org.ariqhikari.movietix.presentation.movie.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val movieModule = module {
    single { TmdbApiService(get()) }
    single<MovieRepository> { MovieRepositoryImpl(get()) }

    factory { GetNowPlayingMoviesUseCase(get()) }
    factory { GetPopularMoviesUseCase(get()) }
    factory { GetUpcomingMoviesUseCase(get()) }

    viewModel { HomeViewModel(get(), get(), get()) }
}
