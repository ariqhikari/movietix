package org.ariqhikari.movietix.data.cinema.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.data.cinema.local.ProceduralScheduleEngine
import org.ariqhikari.movietix.domain.cinema.model.Cinema
import org.ariqhikari.movietix.domain.cinema.model.SeatLayout
import org.ariqhikari.movietix.domain.cinema.repository.CinemaScheduleRepository

class CinemaScheduleRepositoryImpl(
    private val proceduralEngine: ProceduralScheduleEngine,
    private val dataStore: DataStore<Preferences>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : CinemaScheduleRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun getCinemasWithSchedules(
        movieId: Int,
        date: LocalDate
    ): Flow<AppResult<List<Cinema>>> = flow<AppResult<List<Cinema>>> {
        val cinemas = proceduralEngine.generateCinemas(movieId, date)
        emit(AppResult.Success(cinemas))
    }.catch { e ->
        emit(AppResult.Error(AppError.Storage.ReadFailed(e.message ?: "Failed to generate schedules")))
    }.flowOn(dispatcher)

    override fun getSeatLayout(
        scheduleId: String,
        movieId: Int,
        cinemaId: String,
        studioId: String,
        date: LocalDate,
        time: String
    ): Flow<AppResult<SeatLayout>> {
        val key = stringPreferencesKey("occupied_seats_$scheduleId")
        return dataStore.data.map { preferences ->
            val userBookedJson = preferences[key]
            val userBookedSeats: Set<String> = if (!userBookedJson.isNullOrBlank()) {
                try {
                    json.decodeFromString<List<String>>(userBookedJson).toSet()
                } catch (_: Exception) {
                    emptySet()
                }
            } else {
                emptySet()
            }

            val layout = proceduralEngine.generateSeatLayout(
                scheduleId = scheduleId,
                movieId = movieId,
                cinemaId = cinemaId,
                studioId = studioId,
                date = date.toString(),
                time = time,
                userBookedSeats = userBookedSeats
            )
            AppResult.Success(layout) as AppResult<SeatLayout>
        }.catch { e ->
            emit(AppResult.Error(AppError.Storage.ReadFailed(e.message ?: "Failed to read seat layout")))
        }.flowOn(dispatcher)
    }

    override suspend fun reserveSeats(
        scheduleId: String,
        seatIds: List<String>
    ): AppResult<Unit> = withContext(dispatcher) {
        if (seatIds.isEmpty()) {
            return@withContext AppResult.Success(Unit)
        }

        // Check if any seat is already occupied by the procedural generator
        val parsed = proceduralEngine.parseScheduleId(scheduleId)
        if (parsed != null) {
            val engineOccupied = proceduralEngine.generateOccupiedSeats(
                scheduleId = scheduleId,
                movieId = parsed.movieId,
                cinemaId = parsed.cinemaId,
                studioId = parsed.studioId,
                date = parsed.date,
                time = parsed.time
            )
            if (seatIds.any { it in engineOccupied }) {
                return@withContext AppResult.Error(AppError.Booking.SeatAlreadyOccupied)
            }
        }

        try {
            var conflict = false
            val key = stringPreferencesKey("occupied_seats_$scheduleId")
            dataStore.edit { preferences ->
                val currentJson = preferences[key]
                val currentOccupied: List<String> = if (!currentJson.isNullOrBlank()) {
                    try {
                        json.decodeFromString<List<String>>(currentJson)
                    } catch (_: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                if (seatIds.any { it in currentOccupied }) {
                    conflict = true
                    return@edit
                }

                val updated = (currentOccupied + seatIds).distinct()
                preferences[key] = json.encodeToString(updated)
            }

            if (conflict) {
                AppResult.Error(AppError.Booking.SeatAlreadyOccupied)
            } else {
                AppResult.Success(Unit)
            }
        } catch (e: Exception) {
            AppResult.Error(AppError.Storage.WriteFailed(e.message ?: "Failed to reserve seats"))
        }
    }

    override suspend fun releaseSeats(
        scheduleId: String,
        seatIds: List<String>
    ): AppResult<Unit> = withContext(dispatcher) {
        if (seatIds.isEmpty()) {
            return@withContext AppResult.Success(Unit)
        }

        try {
            val key = stringPreferencesKey("occupied_seats_$scheduleId")
            dataStore.edit { preferences ->
                val currentJson = preferences[key] ?: return@edit
                val currentOccupied: List<String> = try {
                    json.decodeFromString<List<String>>(currentJson)
                } catch (_: Exception) {
                    emptyList()
                }
                val updated = currentOccupied.filterNot { it in seatIds }
                preferences[key] = json.encodeToString(updated)
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.Storage.WriteFailed(e.message ?: "Failed to release seats"))
        }
    }

    override suspend fun getMovieMinimumPrice(
        movieId: Int,
        date: LocalDate
    ): AppResult<Long> = withContext(dispatcher) {
        try {
            val cinemas = proceduralEngine.generateCinemas(movieId, date)
            val minPrice = cinemas.asSequence()
                .flatMap { it.studios.asSequence() }
                .flatMap { it.showtimes.asSequence() }
                .map { it.price }
                .minOrNull() ?: 45_000L
            AppResult.Success(minPrice)
        } catch (e: Exception) {
            AppResult.Error(AppError.Storage.ReadFailed(e.message ?: "Failed to compute min price"))
        }
    }
}
