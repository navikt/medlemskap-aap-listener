package no.nav.medlemskap.aap.listener.domain

import java.time.LocalDate
import java.util.*

data class AapRecord(val partition:Int, val offset:Long, val key:String?, val topic:String, val aapRequest:MedlemKafkaDto) {
}

data class MedlemKafkaDto(
    val personident: String,
    val id: UUID,
    val request: Request?,
    var response: Response?
) {
    data class Request(
        val mottattDato: LocalDate,
        val ytelse: String = "AAP",
        val arbeidetUtenlands: Boolean,
    )

    data class Response(
        val erMedlem: ErMedlem,
        val begrunnelse: String?
    )

    enum class ErMedlem {
        JA,
        NEI,
        UAVKLART
    }
}
