package no.nav.kafkaproduser

import no.nav.medlemskap.aap.listener.config.Configuration
import no.nav.medlemskap.aap.listener.config.Environment
import no.nav.medlemskap.aap.listener.config.KafkaConfig

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer

import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringDeserializer

import java.time.Duration

import java.util.logging.Logger

fun main(args: Array<String>) {
    TestConsumerAvro(KafkaConfig(System.getenv())).consume(10)
}

class TestConsumerAvro(brokers: KafkaConfig) {
    private val env: Environment = System.getenv()
    private val logger = Logger.getLogger("SimpleProducer")



    fun inst2Config() = mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to Configuration.KafkaConfig().bootstrapServers,
        //ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        CommonClientConfigs.CLIENT_ID_CONFIG to "Produser",//Configuration.KafkaConfig().clientId,
        //ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
        //ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.GROUP_ID_CONFIG to "testAAPAvroConsumer",
        //KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true,
        //KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistry,
        ConsumerConfig.CLIENT_ID_CONFIG to Configuration.KafkaConfig().groupID,
        //ConsumerConfig.GROUP_ID_CONFIG to Configuration.KafkaConfig().groupID,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to SecurityProtocol.SASL_SSL.name,
        SaslConfigs.SASL_MECHANISM to "PLAIN",
        SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to Configuration.KafkaConfig().keystoreLocation,
        SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to Configuration.KafkaConfig().keystorePassword,
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to Configuration.KafkaConfig().securityProtocol,
        //CommonClientConfigs.CLIENT_ID_CONFIG to Configuration.KafkaConfig().clientId,
        SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to Configuration.KafkaConfig().trustStorePath,
        SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to Configuration.KafkaConfig().keystorePassword,
        SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to Configuration.KafkaConfig().keystoreType,



    )
    private val consumer = createConsumer()
    private fun  createConsumer(): Consumer<String, String> {

        return KafkaConsumer<String, String>(inst2Config())
    }

    fun consume(ratePerSecond: Int) {
        consumer.subscribe(listOf("medlemskap.test-medlemskap-oppslag-avro"))
        while(true) {
            System.out.println("Polling");
            val records = consumer.poll(Duration.ofSeconds(1))
            logger.info("Received ${records.count()} records")
            records.iterator().forEach {
                val medlemAvro = it.value()
                println(medlemAvro)
            }
        }
        //val jsonString: String = File("./src/main/resources/sampleRequest.json").readText(Charsets.UTF_8)
    }
}