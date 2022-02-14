package no.nav.medlemskap.aap.listener.config

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import no.nav.aap.avro.medlem.v1.Medlem
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

open class KafkaConfig(
    environment: Environment,
    private val securityStrategy: SecurityStrategy = PlainStrategy(environment = environment)
) {

    val topic = Configuration.KafkaConfig().topic
    val test_topic = Configuration.KafkaConfig().test_topic
    val enabled = Configuration.KafkaConfig().enabled


    fun inst2AvroConsumerConfig() = mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to Configuration.KafkaConfig().bootstrapServers,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        CommonClientConfigs.CLIENT_ID_CONFIG to Configuration.KafkaConfig().clientId,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,

        ConsumerConfig.GROUP_ID_CONFIG to Configuration.KafkaConfig().groupID,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 10,
        "schema.registry.url" to Configuration.KafkaConfig().kafka_schema_registry,
        "specific.avro.reader" to "true",
        SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO" ,
        SchemaRegistryClientConfig.USER_INFO_CONFIG to java.lang.String.format(
            "%s:%s",
            Configuration.KafkaConfig().kafka_schema_registry_user,
            Configuration.KafkaConfig().kafka_schema_registry_password
        )

    ) + securityStrategy.securityConfig()

    fun createAvroConsumer() = KafkaConsumer<String, Medlem>(inst2AvroConsumerConfig())

    interface SecurityStrategy {
        fun securityConfig(): Map<String, String>
    }
}