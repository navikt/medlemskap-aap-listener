package no.nav.kafkaproduser

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.aap.avro.medlem.v1.Medlem
import no.nav.aap.avro.medlem.v1.Request
import no.nav.medlemskap.aap.listener.config.Configuration
import no.nav.medlemskap.aap.listener.config.Environment
import no.nav.medlemskap.aap.listener.config.KafkaConfig
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.time.LocalDate
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

fun main(args: Array<String>) {
    TestConsumerAvro2(KafkaConfig(System.getenv())).consume(10)
}

class TestConsumerAvro2(brokers: KafkaConfig) {
    private val env: Environment = System.getenv()
    private val logger = Logger.getLogger("SimpleProducer")



    fun inst2Config() = mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to Configuration.KafkaConfig().bootstrapServers,
        //ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        CommonClientConfigs.CLIENT_ID_CONFIG to "Produser",//Configuration.KafkaConfig().clientId,
        //ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
        //ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
        ConsumerConfig.GROUP_ID_CONFIG to "testAAPAvroConsumer_new",
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
        "schema.registry.url" to "https://nav-dev-kafka-nav-dev.aivencloud.com:26487",
        "specific.avro.reader" to "true",
        SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO" ,
        SchemaRegistryClientConfig.USER_INFO_CONFIG to java.lang.String.format(
            "%s:%s",
            "medlemskap_oppslag_a0ee9eab_NVs",
            "qyIiyGxjMCLRtr2K"
        )



    )
    private val consumer = createConsumer()
    private fun  createConsumer(): Consumer<String, Medlem> {

        return KafkaConsumer<String, Medlem>(inst2Config())
    }

    fun consume(ratePerSecond: Int) {
        consumer.subscribe(listOf("medlemskap.test-medlemskap-oppslag-avro"))
        while(true) {
            System.out.println("Polling");
            var records: ConsumerRecords<String, Medlem> = consumer.poll(Duration.ofSeconds(5))
            logger.info("Received ${records.count()} records")
            records.iterator().forEach {
                val medlemAvro:Medlem = it.value()
                println(medlemAvro)
            }
        }
        //val jsonString: String = File("./src/main/resources/sampleRequest.json").readText(Charsets.UTF_8)
    }
}