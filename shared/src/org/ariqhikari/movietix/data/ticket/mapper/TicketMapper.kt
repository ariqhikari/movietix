package org.ariqhikari.movietix.data.ticket.mapper

import org.ariqhikari.movietix.data.ticket.local.TicketEntity
import org.ariqhikari.movietix.domain.booking.model.PaymentMethod
import org.ariqhikari.movietix.domain.ticket.model.Ticket
import org.ariqhikari.movietix.domain.ticket.model.TicketStatus

fun TicketEntity.toDomain(): Ticket = Ticket(
    bookingCode = bookingCode,
    movieId = movieId,
    movieTitle = movieTitle,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    cinemaName = cinemaName,
    studioName = studioName,
    studioType = studioType,
    showtimeIso = showtimeIso,
    seats = seats,
    pricePerSeat = pricePerSeat,
    serviceFee = serviceFee,
    totalPrice = totalPrice,
    paymentMethod = PaymentMethod.fromCode(paymentMethod),
    transactionTimeEpochMs = transactionTimeEpochMs,
    status = try {
        TicketStatus.valueOf(status)
    } catch (_: Exception) {
        TicketStatus.ACTIVE
    },
    qrPayload = qrPayload
)

fun Ticket.toEntity(): TicketEntity = TicketEntity(
    bookingCode = bookingCode,
    movieId = movieId,
    movieTitle = movieTitle,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    cinemaName = cinemaName,
    studioName = studioName,
    studioType = studioType,
    showtimeIso = showtimeIso,
    seats = seats,
    pricePerSeat = pricePerSeat,
    serviceFee = serviceFee,
    totalPrice = totalPrice,
    paymentMethod = paymentMethod.name,
    transactionTimeEpochMs = transactionTimeEpochMs,
    status = status.name,
    qrPayload = qrPayload
)
