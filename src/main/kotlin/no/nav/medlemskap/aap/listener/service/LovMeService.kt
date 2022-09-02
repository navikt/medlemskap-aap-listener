package no.nav.medlemskap.aap.listener.service

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv

import no.nav.medlemskap.aap.listener.kafka.KafkaProduser
import no.nav.medlemskap.aap.listener.domain.AapRecord
import no.nav.medlemskap.aap.listener.clients.medloppslag.*
import no.nav.medlemskap.aap.listener.config.Configuration
import no.nav.medlemskap.aap.listener.domain.MedlemKafkaDto
import no.nav.medlemskap.aap.listener.jackson.JacksonParser


class LovMeService(
    private val configuration: Configuration,
    private val medlOppslagClient: LovmeAPI,
    private val kafkaProduser: KafkaProduser
)
{
    companion object {
        private val log = KotlinLogging.logger { }

    }
    suspend fun callLovMe(request: MedlemKafkaDto): String {
        val lovMeRequest = MedlOppslagRequest(
            fnr = request.personident,
            førsteDagForYtelse = request.request!!.mottattDato.toString(),
            periode = Periode(request.request!!.mottattDato.toString(), request.request.mottattDato.toString()),
            brukerinput = Brukerinput(request.request.arbeidetUtenlands)
        )
        return medlOppslagClient.vurderMedlemskap(lovMeRequest, request.id.toString())
    }
    suspend fun handle(aapRecord: AapRecord)
    {
        if (validerSoknad(aapRecord.aapRequest)) {
            try {
                var response = vurderAAPMeldemskap(aapRecord.aapRequest)
                aapRecord.logSendt()
                kafkaProduser.publish(aapRecord.topic,aapRecord.aapRequest.id.toString(),response)
                aapRecord.logSvart()
            }
            catch (t:Throwable){
                aapRecord.logTekiskFeil(t)
            }
        } else {
            aapRecord.logIkkeSendt()
        }
    }

    fun validerSoknad(aapRecord: MedlemKafkaDto): Boolean {
        return true
    }

    suspend fun vurderAAPMeldemskap (request:MedlemKafkaDto):MedlemKafkaDto{
        val lovmeResponseAsTekst = callLovMe(request)
        val lovmeResponse:JsonNode = JacksonParser().parseToJsonNode(lovmeResponseAsTekst)
        val response:MedlemKafkaDto.Response = mapToAAPResponseObject(lovmeResponse)
        return MedlemKafkaDto(
            personident = request.personident,
            id = request.id,
            request = request.request,
            response = response
        )
    }

    private fun mapToAAPResponseObject(lovmeResponse: JsonNode): MedlemKafkaDto.Response {
        val svar = lovmeResponse.get("resultat").get("svar").asText()
        val begrunnelse = lovmeResponse.get("resultat").get("begrunnelse").asText()
        val response = MedlemKafkaDto.Response(MedlemKafkaDto.ErMedlem.valueOf(svar), begrunnelse)
        return response
    }

    private fun AapRecord.logIkkeSendt() =
        LovMeService.log.info(
            "Søknad ikke  sendt til lovme basert på validering - aapID: ${aapRequest.id}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", aapRequest.id),
        )

    private fun AapRecord.logSendt() =
        LovMeService.log.info(
            "Søknad videresendt til Lovme - aapID: ${aapRequest.id}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", aapRequest.id),
        )
    private fun AapRecord.logTekiskFeil(t:Throwable) =
        LovMeService.log.info(
            "Teknisk feil ved kall mot LovMe - aapID: ${aapRequest.id}, melding:"+t.message,
            kv("callId", aapRequest.id),
        )
    private fun AapRecord.logSvart() =
        LovMeService.log.info(
            "Respons gitt til AAP - aapID: ${aapRequest.id}, topic: $topic",
            kv("callId", aapRequest.id),
        )
}
