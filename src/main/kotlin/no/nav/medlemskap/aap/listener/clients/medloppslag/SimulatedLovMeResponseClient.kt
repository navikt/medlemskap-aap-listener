package no.nav.medlemskap.aap.listener.clients.medloppslag



class SimulatedLovMeResponseClient():LovmeAPI {
    override suspend fun vurderMedlemskap(medlOppslagRequest: MedlOppslagRequest, callId: String): String {
        return mockResponse()
    }
}

fun mockResponse():String{
    return ""
}