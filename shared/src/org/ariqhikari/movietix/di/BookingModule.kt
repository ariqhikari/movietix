package org.ariqhikari.movietix.di

import org.ariqhikari.movietix.data.booking.remote.DummyPaymentGateway
import org.ariqhikari.movietix.domain.booking.gateway.PaymentGateway
import org.ariqhikari.movietix.domain.booking.model.BookingOrder
import org.ariqhikari.movietix.domain.booking.usecase.ConfirmBookingUseCase
import org.ariqhikari.movietix.domain.booking.util.BookingCodeGenerator
import org.ariqhikari.movietix.presentation.booking.checkout.CheckoutViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val bookingModule = module {
    single<PaymentGateway> {
        DummyPaymentGateway(dispatcher = get())
    }

    single { BookingCodeGenerator() }

    single {
        ConfirmBookingUseCase(
            cinemaScheduleRepository = get(),
            paymentGateway = get(),
            ticketRepository = get(),
            bookingCodeGenerator = get()
        )
    }

    viewModel { (initialOrder: BookingOrder) ->
        CheckoutViewModel(
            confirmBookingUseCase = get(),
            initialOrder = initialOrder,
            isDebug = get(named("isDebug"))
        )
    }
}
