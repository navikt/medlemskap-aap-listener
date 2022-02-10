package no.nav.medlemskap.aap.listener.clients.medloppslag

interface LovmeAPI {
    suspend fun vurderMedlemskap(medlOppslagRequest: MedlOppslagRequest, callId: String): String
}