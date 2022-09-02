package no.nav.medlemskap.aap.listener

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.time.delay
import mu.KotlinLogging
import no.nav.medlemskap.aap.listener.kafka.AapKafkaProducer
import no.nav.medlemskap.aap.listener.clients.RestClients
import no.nav.medlemskap.aap.listener.clients.azuread.AzureAdClient
import no.nav.medlemskap.aap.listener.clients.medloppslag.LovmeAPI
import no.nav.medlemskap.aap.listener.config.Configuration
import no.nav.medlemskap.aap.listener.config.Environment
import no.nav.medlemskap.aap.listener.config.KafkaConfig
import no.nav.medlemskap.aap.listener.domain.AapRecord
import no.nav.medlemskap.aap.listener.jackson.JacksonParser
import no.nav.medlemskap.aap.listener.service.LovMeService
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.util.Objects.isNull

class JsonConsumer(
    environment: Environment,
    private val config: KafkaConfig = KafkaConfig(environment),
    private val service: LovMeService = LovMeService(
        Configuration(),
        SimulatedLovMeResponseClient(),
        AapKafkaProducer(Configuration())
    ),
    private val consumer: KafkaConsumer<String, String> = config.createAvroConsumer(),
)
{
    val azureAdClient = AzureAdClient(Configuration())
    val restClients = RestClients(
        azureAdClient = azureAdClient,
        configuration = Configuration()
    )
    val medlOppslagClient: LovmeAPI
    private val secureLogger = KotlinLogging.logger("tjenestekall")
    private val logger = KotlinLogging.logger { }
    init {
        consumer.subscribe(listOf(config.topic,config.test_topic))
        medlOppslagClient = SimulatedLovMeResponseClient()
    }

    fun pollMessages(): List<AapRecord> =

            consumer.poll(Duration.ofSeconds(4))
                .map {
                    AapRecord(
                        it.partition(),
                        it.offset(),
                        it.key(),
                        it.topic(),
                        JacksonParser().parse(it.value()))
                }
                .filter { isNull(it.aapRequest.response)}

    fun flow(): Flow<List<AapRecord>> =
        kotlinx.coroutines.flow.flow {
            while (true) {

                if (config.enabled != "Ja") {
                    logger.debug("Kafka is disabled. Does not fetch messages from topic")
                    emit(emptyList<AapRecord>())
                } else {
                    emit(pollMessages())
                }

                delay(Duration.ofSeconds(1))
            }
        }.onEach {

            logger.debug { "receiced :"+ it.size + "on topic "+config.topic }
            it.forEach { record -> service.handle(record) }
        }.onEach {
            consumer.commitAsync()
        }.onEach {
            Metrics.incProcessedTotal(it.count())
        }

}