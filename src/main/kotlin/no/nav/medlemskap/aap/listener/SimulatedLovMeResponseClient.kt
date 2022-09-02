package no.nav.medlemskap.aap.listener


import no.nav.medlemskap.aap.listener.clients.medloppslag.LovmeAPI
import no.nav.medlemskap.aap.listener.clients.medloppslag.MedlOppslagRequest
import java.util.*

enum class UuidValues(val value:String)
{
    UAVLART("5fc03087-d265-11e7-b8c6-83e29cd24f4c"),
    JA("5fc03087-d265-11e7-b8c6-83e29cd24f4d"),
}
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