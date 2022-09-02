package no.nav.medlemskap.aap.listener.service

import no.nav.medlemskap.aap.listener.clients.medloppslag.LovmeAPI
import no.nav.medlemskap.aap.listener.clients.medloppslag.MedlOppslagRequest
import java.util.*


class SimulatedLovMeResponseClient(): LovmeAPI {
    override suspend fun vurderMedlemskap(medlOppslagRequest: MedlOppslagRequest, callId: String): String {
        if (callId== UuidValues.JA.value) {
            return mockJaResponse()
        }
        return if (callId==UuidValues.UAVLART.value) {
            mockUavklartResponse()
        } else{
            mockRandomResponse()
        }
    }
}

fun mockUavklartResponse():String{
     val fileContent = SimulatedLovMeResponseClient::class.java.classLoader.getResource("LovMeUavklartResponse.json").readText(Charsets.UTF_8)
    return fileContent
}
fun mockJaResponse():String{
    val fileContent = SimulatedLovMeResponseClient::class.java.classLoader.getResource("LovMeJaResponse.json").readText(Charsets.UTF_8)
    return fileContent
}
fun mockRandomResponse():String{

    if (Random().nextBoolean()){
        return mockJaResponse()
    }
    else{
        return mockUavklartResponse()
    }
}