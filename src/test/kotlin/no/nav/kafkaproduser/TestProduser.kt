package no.nav.kafkaproduser


import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.aap.avro.medlem.v1.ErMedlem
import no.nav.aap.avro.medlem.v1.Medlem
import no.nav.aap.avro.medlem.v1.Request
import no.nav.aap.avro.medlem.v1.Response
import no.nav.medlemskap.aap.listener.config.Configuration
import no.nav.medlemskap.aap.listener.config.Environment
import no.nav.medlemskap.aap.listener.config.KafkaConfig
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
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
    SimpleProducer(KafkaConfig(System.getenv())).produce(1)
}

class SimpleProducer(brokers: KafkaConfig) {
    private val env: Environment = System.getenv()
    private val logger = Logger.getLogger("SimpleProducer")



    fun inst2Config() = mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to Configuration.KafkaConfig().bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        CommonClientConfigs.CLIENT_ID_CONFIG to "Produser",//Configuration.KafkaConfig().clientId,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
        ProducerConfig.CLIENT_ID_CONFIG to Configuration.KafkaConfig().groupID,
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to SecurityProtocol.SASL_SSL.name,
        SaslConfigs.SASL_MECHANISM to "PLAIN",
        SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to Configuration.KafkaConfig().keystoreLocation,
        SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to Configuration.KafkaConfig().keystorePassword,
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to Configuration.KafkaConfig().securityProtocol,
        SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to Configuration.KafkaConfig().trustStorePath,
        SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to Configuration.KafkaConfig().keystorePassword,
        SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to Configuration.KafkaConfig().keystoreType,
        "schema.registry.url" to "https://nav-dev-kafka-nav-dev.aivencloud.com:26487",
        SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO" ,
        SchemaRegistryClientConfig.USER_INFO_CONFIG to format("%s:%s", "medlemskap_oppslag_a0ee9eab_NVs", "qyIiyGxjMCLRtr2K")



    )
    private val producer = createProducer()
    private fun createProducer(): Producer<String, Medlem> {

        return KafkaProducer<String, Medlem>(inst2Config())
    }

    fun produce(ratePerSecond: Int) {
        val medlem = Medlem("id",UUID.randomUUID().toString(), Request(LocalDate.now(),"AAP",false), null)
        //val jsonString: String = File("./src/main/resources/sampleRequest.json").readText(Charsets.UTF_8)
        ratePerSecond
        while(true) {

            val futureResult = producer.send(
                ProducerRecord(
                    "medlemskap.test-medlemskap-oppslag-avro",
                    UUID.randomUUID().toString(), medlem
                )
            )

            logger.log(Level.INFO, "Sent a record")

            Thread.sleep(1000*ratePerSecond.toLong())

            // wait for the write acknowledgment
            //futureResult.get()
        }
        }
    }