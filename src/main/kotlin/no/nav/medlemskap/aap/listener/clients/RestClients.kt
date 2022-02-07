package no.nav.medlemskap.aap.listener.clients


import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagClient
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.config.retryRegistry
import no.nav.medlemskap.sykepenger.lytter.http.cioHttpClient

class RestClients(
    private val azureAdClient: AzureAdClient,
    private val configuration: Configuration
) {

    private val medlRetry = retryRegistry.retry("MEDL-OPPSLAG")

    private val httpClient = cioHttpClient
    fun medlOppslag(endpointBaseUrl: String) = MedlOppslagClient(endpointBaseUrl, azureAdClient, httpClient, medlRetry)
}
