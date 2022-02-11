package no.nav.medlemskap.aap.listener.service

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.aap.avro.medlem.v1.ErMedlem
import no.nav.aap.avro.medlem.v1.Medlem
import no.nav.aap.avro.medlem.v1.Request
import no.nav.aap.avro.medlem.v1.Response
import no.nav.medlemskap.aap.listener.domain.AapRecord
import no.nav.medlemskap.aap.listener.clients.RestClients
import no.nav.medlemskap.aap.listener.clients.azuread.AzureAdClient
import no.nav.medlemskap.aap.listener.clients.medloppslag.*
import no.nav.medlemskap.aap.listener.config.Configuration
import no.nav.medlemskap.aap.listener.jakson.JaksonParser

class LovMeService(
    private val configuration: Configuration,
    private val medlOppslagClient: LovmeAPI
)
{
    companion object {
        private val log = KotlinLogging.logger { }

    }
    suspend fun callLovMe(request: Medlem): String {
        val lovMeRequest = MedlOppslagRequest(
            fnr = request.personident,
            førsteDagForYtelse = request.request.mottattDato.toString(),
            periode = Periode(request.request.mottattDato.toString(), request.request.mottattDato.toString()),
            brukerinput = Brukerinput(request.request.arbeidetUtenlands)
        )
        return medlOppslagClient.vurderMedlemskap(lovMeRequest, request.id)
    }
    suspend fun handle(aapRecord: AapRecord)
    {
        if (validerSoknad(aapRecord.aapRequest)) {
            try {
                var response = vurderAAPMeldemskap(aapRecord.aapRequest)
                //TODO: Publish response to kafka
                println("temporarily end of line. Response will be implemented shotly")
            }
            catch (t:Throwable){
                aapRecord.logTekiskFeil(t)
            }
        } else {
            aapRecord.logIkkeSendt()
        }
    }

    fun validerSoknad(aapRecord: Medlem): Boolean {
        return true
    }

    suspend fun vurderAAPMeldemskap (request:Medlem):Medlem{
        var aapResponse:Medlem = request
        val lovmeResponseAsTekst = callLovMe(request)

        val lovmeResponse:JsonNode = JaksonParser().parse(lovmeResponseAsTekst)
        val response:Response = mapToAAPResponseObject(lovmeResponse)
        aapResponse.response=response
        return aapResponse
    }

    private fun mapToAAPResponseObject(lovmeResponse: JsonNode): Response {
        val svar = lovmeResponse.get("resultat").get("svar").asText()
        val begrunnelse = lovmeResponse.get("resultat").get("begrunnelse").asText()
        val response = Response(ErMedlem.valueOf(svar),begrunnelse)
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
}
