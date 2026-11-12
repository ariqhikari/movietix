package org.ariqhikari.movietix.data.ticket.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.data.ticket.local.TicketEntity
import org.ariqhikari.movietix.data.ticket.local.TicketHistoryContainer
import org.ariqhikari.movietix.data.ticket.mapper.toDomain
import org.ariqhikari.movietix.data.ticket.mapper.toEntity
import org.ariqhikari.movietix.domain.ticket.model.Ticket
import org.ariqhikari.movietix.domain.ticket.repository.TicketRepository

class TicketRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TicketRepository {

    private val ticketKey = stringPreferencesKey("ticket_history_v1")
    private val maxRecentTickets = 50

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    override fun getTickets(): Flow<AppResult<List<Ticket>>> = dataStore.data
        .map<Preferences, AppResult<List<Ticket>>> { preferences ->
            val rawJson = preferences[ticketKey]
            if (rawJson.isNullOrBlank()) {
                AppResult.Success(emptyList())
            } else {
                val container = try {
                    json.decodeFromString<TicketHistoryContainer>(rawJson)
                } catch (_: Exception) {
                    TicketHistoryContainer(schemaVersion = 1, tickets = emptyList())
                }
                val domainTickets = container.tickets
                    .map(TicketEntity::toDomain)
                    .sortedByDescending { it.transactionTimeEpochMs }
                AppResult.Success(domainTickets)
            }
        }
        .catch { error ->
            emit(AppResult.Error(AppError.Storage.ReadFailed(error.message ?: "Gagal membaca riwayat tiket")))
        }
        .flowOn(dispatcher)

    override suspend fun getTicketByBookingCode(bookingCode: String): AppResult<Ticket?> =
        withContext(dispatcher) {
            try {
                val preferences = dataStore.data.first()
                val rawJson = preferences[ticketKey]
                if (rawJson.isNullOrBlank()) {
                    return@withContext AppResult.Success(null)
                }
                val container = try {
                    json.decodeFromString<TicketHistoryContainer>(rawJson)
                } catch (_: Exception) {
                    return@withContext AppResult.Success(null)
                }
                val entity = container.tickets.firstOrNull { it.bookingCode == bookingCode }
                AppResult.Success(entity?.toDomain())
            } catch (e: Exception) {
                AppResult.Error(AppError.Storage.ReadFailed(e.message ?: "Gagal memuat tiket"))
            }
        }

    override suspend fun saveTicket(ticket: Ticket): AppResult<Unit> =
        withContext(dispatcher) {
            var isDuplicate = false
            try {
                dataStore.edit { preferences ->
                    val rawJson = preferences[ticketKey]
                    val container = if (!rawJson.isNullOrBlank()) {
                        try {
                            json.decodeFromString<TicketHistoryContainer>(rawJson)
                        } catch (_: Exception) {
                            TicketHistoryContainer(schemaVersion = 1, tickets = emptyList())
                        }
                    } else {
                        TicketHistoryContainer(schemaVersion = 1, tickets = emptyList())
                    }

                    if (container.tickets.any { it.bookingCode == ticket.bookingCode }) {
                        isDuplicate = true
                        return@edit
                    }

                    val updatedTickets = (listOf(ticket.toEntity()) + container.tickets)
                        .take(maxRecentTickets)

                    val updatedContainer = container.copy(tickets = updatedTickets)
                    preferences[ticketKey] = json.encodeToString(updatedContainer)
                }

                if (isDuplicate) {
                    AppResult.Error(
                        AppError.Storage.WriteFailed("Tiket dengan kode booking ${ticket.bookingCode} sudah tersimpan.")
                    )
                } else {
                    AppResult.Success(Unit)
                }
            } catch (e: Exception) {
                AppResult.Error(
                    AppError.Storage.WriteFailed(e.message ?: "Gagal menyimpan tiket ke penyimpanan.")
                )
            }
        }
}
