package no.nav.medlemskap.aap.listener.clients.medloppslag

import java.util.*


class SimulatedLovMeResponseClient():LovmeAPI {
    override suspend fun vurderMedlemskap(medlOppslagRequest: MedlOppslagRequest, callId: String): String {
        if (callId=="1") {
            return mockJaResponse()
        }
        return if (callId=="2") {
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