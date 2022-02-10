package no.nav.medlemskap.aap.listener.service

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.aap.avro.medlem.v1.Medlem
import no.nav.medlemskap.aap.listener.domain.AapRecord
import no.nav.medlemskap.aap.listener.clients.RestClients
import no.nav.medlemskap.aap.listener.clients.azuread.AzureAdClient
import no.nav.medlemskap.aap.listener.clients.medloppslag.*
import no.nav.medlemskap.aap.listener.config.Configuration

class LovMeService(
    private val configuration: Configuration,
)
{
    companion object {
        private val log = KotlinLogging.logger { }

    }
    val azureAdClient = AzureAdClient(configuration)
    val restClients = RestClients(
        azureAdClient = azureAdClient,
        configuration = configuration
    )
    val medlOppslagClient: LovmeAPI


    init {
    //TODO: Endre fra simulert til faktisk Lovme kall
        //merk.. krever endringer i token (eget token for denne tjenesten)
        medlOppslagClient = SimulatedLovMeResponseClient()
    //medlOppslagClient=restClients.medlOppslag(configuration.register.medlemskapOppslagBaseUrl)
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
                val response = callLovMe(aapRecord.aapRequest)
                println("response : $response")
                aapRecord.logSendt()
            }
            catch (t:Throwable){
                aapRecord.logTekiskFeil(t)
            }
        } else {
            aapRecord.logIkkeSendt()
        }
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

    fun validerSoknad(aapRecord: Medlem): Boolean {
        return true
    }
}
