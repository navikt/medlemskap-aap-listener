package no.nav.kafkaproduser





import no.nav.medlemskap.aap.listener.config.Configuration
import no.nav.medlemskap.aap.listener.config.Environment
import no.nav.medlemskap.aap.listener.config.KafkaConfig
import no.nav.medlemskap.aap.listener.domain.MedlemKafkaDto
import no.nav.medlemskap.aap.listener.jackson.JacksonParser

import org.apache.kafka.clients.CommonClientConfigs

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.io.File
import java.lang.String.format
import java.time.LocalDate
import java.util.*

import java.util.logging.Level
import java.util.logging.Logger

fun main(args: Array<String>) {
    SimpleProducer(KafkaConfig(System.getenv())).produce(10)
}

class SimpleProducer(brokers: KafkaConfig) {
    private val env: Environment = System.getenv()
    private val logger = Logger.getLogger("SimpleProducer")



    fun inst2Config() = mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to Configuration.KafkaConfig().bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        CommonClientConfigs.CLIENT_ID_CONFIG to "Produser",//Configuration.KafkaConfig().clientId,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.CLIENT_ID_CONFIG to Configuration.KafkaConfig().groupID,
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to SecurityProtocol.SASL_SSL.name,
        SaslConfigs.SASL_MECHANISM to "PLAIN",
        SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to Configuration.KafkaConfig().keystoreLocation,
        SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to Configuration.KafkaConfig().keystorePassword,
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to Configuration.KafkaConfig().securityProtocol,
        SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to Configuration.KafkaConfig().trustStorePath,
        SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to Configuration.KafkaConfig().keystorePassword,
        SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to Configuration.KafkaConfig().keystoreType,
    )
    private val producer = createProducer()
    private fun createProducer(): Producer<String, String> {

        return KafkaProducer<String, String>(inst2Config())
    }

    fun produce(ratePerSecond: Int) {
        val medlem = MedlemKafkaDto(
            "id",
            UUID.randomUUID(),
            MedlemKafkaDto.Request(
                LocalDate.now(),
                "AAP",
                false),
            null)
        //val jsonString: String = File("./src/main/resources/sampleRequest.json").readText(Charsets.UTF_8)
        while(true) {

            val futureResult = producer.send(
                ProducerRecord(
                    "medlemskap.test-medlemskap-oppslag-avro",
                    UUID.randomUUID().toString(), JacksonParser().parseToJsonString(medlem)
                )
            )

            logger.log(Level.INFO, "Sent a record")
            logger.log(Level.INFO,JacksonParser().parseToJsonString(medlem))
            Thread.sleep(1000*ratePerSecond.toLong())

            // wait for the write acknowledgment
            //futureResult.get()
        }
        }
    }