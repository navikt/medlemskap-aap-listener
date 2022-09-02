package no.nav.medlemskap.aap.listener.jackson

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging
import no.nav.medlemskap.aap.listener.domain.MedlemKafkaDto
import java.util.*


class JacksonParser {
    private val log = KotlinLogging.logger { }
    fun parse(jsonString: String): MedlemKafkaDto {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.readValue(jsonString)
        } catch (t: Throwable) {
            log.error("Unable to parse json to MedlemKafkaDto . Dropping message. Cause : ${t.message}. json : $jsonString")
            log.error(t.printStackTrace().toString())
            return MedlemKafkaDto(
                personident = "",
                id= UUID.randomUUID(),
                request = null,
                response = null
            )
        }
    }
    fun parseToJsonNode(jsonString: String): JsonNode {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return  mapper.readValue(jsonString)
        }
        catch (t:Throwable){
            log.error("Unable to parse json String ti JsonNode. Dropping message. Cause : ${t.message} . Json: $jsonString")
            return ObjectMapper().createObjectNode()
        }
    }

    fun parseToJsonString(obj: Any): String {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.writeValueAsString(obj)
        } catch (t: Throwable) {
            log.error("Unable to parse Object to String. Dropping message. Cause : ${t.message}")
            throw t;
        }
    }

}