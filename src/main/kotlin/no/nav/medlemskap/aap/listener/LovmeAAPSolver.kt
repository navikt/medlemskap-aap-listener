package no.nav.medlemskap.aap.listener

import no.nav.aap.medlem.model.MedlemResponse
import no.nav.aap.medlem.model.Svar
import no.nav.helse.rapids_rivers.*
import no.nav.medlemskap.aap.listener.service.LovMeService
import org.slf4j.LoggerFactory
internal class LovmeAAPSolver(
    rapidsConnection: RapidsConnection,
    private val lovMeService: LovMeService
) : River.PacketListener {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    companion object {
        const val behov = "medlemskap-vurdering"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("MedlemResponse") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("folkeregisteridentifikator") }
            //validate { it.requireKey("ytelse") }
            validate { it.requireKey("medlemRequest") }
            //validate { it.require("$behov.institusjonsoppholdTom", JsonNode::asLocalDate) }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke $behov med melding\n${problems.toExtendedReport()}")
    }
    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        val response = MedlemResponse(Svar.JA,null)
        packet["MedlemResponse"] = response
        context.publish(packet.toJson())

    }
}