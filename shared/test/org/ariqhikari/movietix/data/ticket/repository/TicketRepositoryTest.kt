package org.ariqhikari.movietix.data.ticket.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult
import org.ariqhikari.movietix.data.ticket.local.TicketEntity
import org.ariqhikari.movietix.data.ticket.mapper.toDomain
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.ticket.model.Ticket
import org.ariqhikari.movietix.domain.ticket.model.TicketStatus
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class TicketRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private fun createTestDataStore(): androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
        val randomSuffix = Random.nextLong()
        val tempPath = "${okio.FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/test_tickets_$randomSuffix.preferences_pb".toPath()
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = { tempPath }
        )
    }

    private fun createSampleTicket(
        bookingCode: String,
        timestampEpochMs: Long = 1731330000000L
    ) = Ticket(
        bookingCode = bookingCode,
        movieId = 100,
        movieTitle = "Sample Movie",
        posterUrl = "https://image.tmdb.org/t/p/w500/sample.jpg",
        backdropUrl = null,
        cinemaName = "CGV Grand Indonesia",
        studioName = "Studio 1",
        studioType = "IMAX",
        showtimeIso = "2026-11-12 14:30",
        seats = listOf("C5", "C6"),
        pricePerSeat = 75000L,
        serviceFee = 5000L,
        totalPrice = 155000L,
        paymentMethod = PaymentMethod.BCA_VA,
        transactionTimeEpochMs = timestampEpochMs,
        status = TicketStatus.ACTIVE,
        qrPayload = "MOVIETIX:v1:$bookingCode:sch_123:C5,C6"
    )

    @Test
    fun getTickets_initiallyEmpty() = runTest(testDispatcher) {
        val dataStore = createTestDataStore()
        val repository = TicketRepositoryImpl(dataStore, testDispatcher)

        repository.getTickets().test {
            val item = awaitItem()
            assertIs<AppResult.Success<*>>(item)
            assertEquals(emptyList<Ticket>(), item.data)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveTicket_persistsTicketAndGetTicketsEmitsDescending() = runTest(testDispatcher) {
        val dataStore = createTestDataStore()
        val repository = TicketRepositoryImpl(dataStore, testDispatcher)

        val ticket1 = createSampleTicket("MTX-111111", timestampEpochMs = 1000L)
        val ticket2 = createSampleTicket("MTX-222222", timestampEpochMs = 2000L)

        val save1 = repository.saveTicket(ticket1)
        assertIs<AppResult.Success<*>>(save1)

        val save2 = repository.saveTicket(ticket2)
        assertIs<AppResult.Success<*>>(save2)

        repository.getTickets().test {
            val item = awaitItem()
            assertIs<AppResult.Success<List<Ticket>>>(item)
            assertEquals(2, item.data.size)
            // Descending order by timestamp: ticket2 first, then ticket1
            assertEquals("MTX-222222", item.data[0].bookingCode)
            assertEquals("MTX-111111", item.data[1].bookingCode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveTicket_rejectsDuplicateBookingCode() = runTest(testDispatcher) {
        val dataStore = createTestDataStore()
        val repository = TicketRepositoryImpl(dataStore, testDispatcher)

        val ticket = createSampleTicket("MTX-DUP001")
        val save1 = repository.saveTicket(ticket)
        assertIs<AppResult.Success<*>>(save1)

        val save2 = repository.saveTicket(ticket)
        assertIs<AppResult.Error>(save2)
        assertIs<AppError.Storage.WriteFailed>(save2.error)
    }

    @Test
    fun saveTicket_enforcesMax50RecentTicketsCap() = runTest(testDispatcher) {
        val dataStore = createTestDataStore()
        val repository = TicketRepositoryImpl(dataStore, testDispatcher)

        // Save 55 tickets
        for (i in 1..55) {
            val ticket = createSampleTicket("MTX-${i.toString().padStart(6, '0')}", timestampEpochMs = i.toLong())
            repository.saveTicket(ticket)
        }

        repository.getTickets().test {
            val item = awaitItem()
            assertIs<AppResult.Success<List<Ticket>>>(item)
            assertEquals(50, item.data.size, "History must cap at maximum 50 tickets")
            // Most recent is MTX-000055
            assertEquals("MTX-000055", item.data.first().bookingCode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTicketByBookingCode_findsCorrectTicket() = runTest(testDispatcher) {
        val dataStore = createTestDataStore()
        val repository = TicketRepositoryImpl(dataStore, testDispatcher)

        val ticket = createSampleTicket("MTX-FINDME")
        repository.saveTicket(ticket)

        val found = repository.getTicketByBookingCode("MTX-FINDME")
        assertIs<AppResult.Success<Ticket?>>(found)
        assertEquals("MTX-FINDME", found.data?.bookingCode)

        val notFound = repository.getTicketByBookingCode("MTX-NONEXIST")
        assertIs<AppResult.Success<Ticket?>>(notFound)
        assertNull(notFound.data)
    }

    @Test
    fun getTickets_corruptJsonFallbackToEmptyList() = runTest(testDispatcher) {
        val dataStore = createTestDataStore()
        val repository = TicketRepositoryImpl(dataStore, testDispatcher)

        val key = stringPreferencesKey("ticket_history_v1")
        dataStore.edit { preferences ->
            preferences[key] = "{ invalid json corrupt data ]"
        }

        repository.getTickets().test {
            val item = awaitItem()
            assertIs<AppResult.Success<List<Ticket>>>(item)
            assertEquals(emptyList(), item.data)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun mapper_toDomain_mapsUnknownStatusOrPaymentMethodSafely() {
        val entityWithUnknowns = TicketEntity(
            bookingCode = "MTX-UNKNOWN",
            movieId = 10,
            movieTitle = "Test",
            posterUrl = null,
            backdropUrl = null,
            cinemaName = "Cinema",
            studioName = "Studio",
            studioType = "IMAX",
            showtimeIso = "2026-11-12",
            seats = listOf("A1"),
            pricePerSeat = 50000L,
            serviceFee = 5000L,
            totalPrice = 55000L,
            paymentMethod = "CRYPTO_WALLET_UNSUPPORTED",
            transactionTimeEpochMs = 1000L,
            status = "REFUNDED_NEW_STATUS",
            qrPayload = "PAYLOAD"
        )

        val domain = entityWithUnknowns.toDomain()
        assertEquals(PaymentMethod.BCA_VA, domain.paymentMethod, "Unknown payment method must safely fallback to BCA_VA")
        assertEquals(TicketStatus.ACTIVE, domain.status, "Unknown status must safely fallback to ACTIVE")
    }
}
