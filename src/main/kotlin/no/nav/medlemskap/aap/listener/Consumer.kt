package no.nav.medlemskap.aap.listener

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.time.delay
import mu.KotlinLogging
import no.nav.medlemskap.aap.listener.config.Configuration
import no.nav.medlemskap.aap.listener.config.KafkaConfig
import no.nav.medlemskap.aap.listener.config.Environment
import no.nav.medlemskap.aap.listener.domain.SoknadRecord
import no.nav.medlemskap.aap.listener.jakson.JaksonParser
import no.nav.medlemskap.aap.listener.service.LovMeService
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

class Consumer(
    environment: Environment,
    private val config: KafkaConfig = KafkaConfig(environment),
    private val service: LovMeService = LovMeService(Configuration()),
    private val consumer: KafkaConsumer<String, String> = config.createConsumer(),

)
{
    private val secureLogger = KotlinLogging.logger("tjenestekall")
    private val logger = KotlinLogging.logger { }
    init {
        consumer.subscribe(listOf(config.topic))
    }

    fun pollMessages(): List<SoknadRecord> =

        consumer.poll(Duration.ofSeconds(4))
            .map { SoknadRecord(it.partition(),
                it.offset(),
                it.value(),
                it.key(),
                it.topic(),
                JaksonParser().parse(it.value())
            )}
            .also {
                Metrics.incReceivedTotal(it.count())
            }

    fun flow(): Flow<List<SoknadRecord>> =
        flow {
            while (true) {

                if(config.enabled!="Ja"){
                    logger.debug("Kafka is disabled. Does not fetch messages from topic")
                    emit(emptyList<SoknadRecord>())
                }
                else{
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