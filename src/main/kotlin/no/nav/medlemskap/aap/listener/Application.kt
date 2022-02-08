package no.nav.medlemskap.aap.listener

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.medlemskap.aap.listener.config.Configuration
import no.nav.medlemskap.aap.listener.service.LovMeService

fun main() {
    val config = Configuration()

    //val azureAdClient = AzureAdClient(config.azureAd)
    //val oidcTokenProvider = { azureAdClient.token(config.medlemskapOppslag.clientId).access_token }

    //val oppslagClient = MedlemskapOppslagClient(config.medlemskapOppslag.url, oidcTokenProvider)

    RapidApplication.create(config.kafkaConfig.rapidApplication).apply {
        LovmeAAPSolver(this, lovMeService = LovMeService(config))
    }.start()
}