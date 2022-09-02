package no.nav.medlemskap.aap.listener.service

import kotlinx.coroutines.runBlocking

import no.nav.medlemskap.aap.listener.config.Configuration
import no.nav.medlemskap.aap.listener.domain.AapRecord
import no.nav.medlemskap.aap.listener.domain.MedlemKafkaDto
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*


 enum class UuidValues(val value:String)
{
    UAVLART("5fc03087-d265-11e7-b8c6-83e29cd24f4c"),
    JA("5fc03087-d265-11e7-b8c6-83e29cd24f4d"),
}
class LovMeServiceTest {

    val uavklartUUID = UUID.fromString(UuidValues.UAVLART.value)
    val JaUUID = UUID.fromString(UuidValues.JA.value)

    @Test
    fun `test`(){
        println(uavklartUUID)
        println(JaUUID)
    }
    @Test
     fun `response skal inenholde respons objekt`() = runBlocking {
        val producer =TestKafkaProducer()
        val response = LovMeService(Configuration(),
                SimulatedLovMeResponseClient(),
            producer)
                .vurderAAPMeldemskap(
                MedlemKafkaDto(
                    "12345678911",
                    UUID.randomUUID(),
                    MedlemKafkaDto.Request(LocalDate.now(), "AAP", false),
                    null
                )
            )
            Assertions.assertNotNull(response.response)

        }
    @Test
    fun `erMedlem skal ikke være null`() = runBlocking {
        val response = LovMeService(Configuration(),
            SimulatedLovMeResponseClient(),
            TestKafkaProducer()
        ).vurderAAPMeldemskap(
            MedlemKafkaDto(
                "12345678911",
                UUID.randomUUID(),
                MedlemKafkaDto.Request(LocalDate.now(), "AAP", false),
                null
            )
        )
        Assertions.assertNotNull(response.response)
        Assertions.assertNotNull(response.response!!.erMedlem)
    }
    @Test
    fun `uavklart skal mappes `() = runBlocking {
        val response = LovMeService(Configuration(),
            SimulatedLovMeResponseClient(),
            TestKafkaProducer()
        ).vurderAAPMeldemskap(
            MedlemKafkaDto(
                "12345678911",
               UUID.fromString(UuidValues.UAVLART.value),
                MedlemKafkaDto.Request(LocalDate.now(), "AAP", false),
                null
            )
        )
        Assertions.assertNotNull(response.response)
        Assertions.assertEquals(MedlemKafkaDto.ErMedlem.UAVKLART,response.response!!.erMedlem)
    }
    @Test
    fun `Ja uavklart skal mappes `() = runBlocking {
        val response = LovMeService(Configuration(),
            SimulatedLovMeResponseClient(),
            TestKafkaProducer()
        ).vurderAAPMeldemskap(
            MedlemKafkaDto(
                "12345678911",
                UUID.fromString(UuidValues.JA.value),
                MedlemKafkaDto.Request(LocalDate.now(), "AAP", false),
                null
            )
        )
        Assertions.assertNotNull(response.response)
        Assertions.assertEquals(MedlemKafkaDto.ErMedlem.JA,response.response!!.erMedlem)
    }

    @Test
    fun `Service skal publisere response objekt`() = runBlocking {
        val producer =TestKafkaProducer()
        val request = MedlemKafkaDto(
            "12345678911",
            UUID.randomUUID(),
            MedlemKafkaDto.Request(LocalDate.now(), "AAP", false),
            null
        )
        val aapRecord:AapRecord = AapRecord(0,0,"1",Configuration().kafkaConfig.topic,request)
        LovMeService(Configuration(),
            SimulatedLovMeResponseClient(),
            producer)
            .handle(aapRecord)

        Assertions.assertNotNull(producer.broker.get(Configuration.KafkaConfig().topic)?.first())
    }
}