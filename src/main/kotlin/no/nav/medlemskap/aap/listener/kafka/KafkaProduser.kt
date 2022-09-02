package no.nav.medlemskap.aap.listener.kafka

import no.nav.medlemskap.aap.listener.config.Configuration
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
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

interface KafkaProduser {
    fun publish(topic:String,key:String,value:MedlemKafkaDto)
}

 class AapKafkaProducer(val config:Configuration):KafkaProduser {

     private val producer = createProducer()
     private fun createProducer(): Producer<String, String> {
         return KafkaProducer<String, String>(inst2JsonConsumerConfig())
     }

     override fun publish(topic: String, key: String, value: MedlemKafkaDto) {
         val futureResult = producer.send(
             ProducerRecord(
                 topic,
                 UUID.randomUUID().toString(), JacksonParser().parseToJsonString(value)
             )
         )
         // wait for the write acknowledgment
         //futureResult.get()
    }
     fun inst2JsonConsumerConfig() = mapOf(
         CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to Configuration.KafkaConfig().bootstrapServers,
         ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
         CommonClientConfigs.CLIENT_ID_CONFIG to config.kafkaConfig.clientId,
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
}