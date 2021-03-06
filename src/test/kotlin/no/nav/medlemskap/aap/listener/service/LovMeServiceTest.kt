package no.nav.medlemskap.aap.listener.service

import kotlinx.coroutines.runBlocking
import no.nav.aap.avro.medlem.v1.ErMedlem
import no.nav.aap.avro.medlem.v1.Medlem
import no.nav.aap.avro.medlem.v1.Request
import no.nav.medlemskap.aap.listener.clients.medloppslag.SimulatedLovMeResponseClient
import no.nav.medlemskap.aap.listener.config.Configuration
import no.nav.medlemskap.aap.listener.domain.AapRecord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class LovMeServiceTest {
    @Test
     fun `response skal inenholde respons objekt`() = runBlocking {
        val producer =TestKafkaProducer()
        val response = LovMeService(Configuration(),
                SimulatedLovMeResponseClient(),
            producer)
                .vurderAAPMeldemskap(
                Medlem(
                    "12345678911",
                    UUID.randomUUID().toString(),
                    Request(LocalDate.now(), "AAP", false),
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
            Medlem(
                "12345678911",
                UUID.randomUUID().toString(),
                Request(LocalDate.now(), "AAP", false),
                null
            )
        )
        Assertions.assertNotNull(response.response)
        Assertions.assertNotNull(response.response.erMedlem)
    }
    @Test
    fun `uavklart skal mappes `() = runBlocking {
        val response = LovMeService(Configuration(),
            SimulatedLovMeResponseClient(),
            TestKafkaProducer()
        ).vurderAAPMeldemskap(
            Medlem(
                "12345678911",
                "2",
                Request(LocalDate.now(), "AAP", false),
                null
            )
        )
        Assertions.assertNotNull(response.response)
        Assertions.assertEquals(ErMedlem.UAVKLART,response.response.erMedlem)
    }
    @Test
    fun `Ja uavklart skal mappes `() = runBlocking {
        val response = LovMeService(Configuration(),
            SimulatedLovMeResponseClient(),
            TestKafkaProducer()
        ).vurderAAPMeldemskap(
            Medlem(
                "12345678911",
                "1",
                Request(LocalDate.now(), "AAP", false),
                null
            )
        )
        Assertions.assertNotNull(response.response)
        Assertions.assertEquals(ErMedlem.JA,response.response.erMedlem)
    }

    @Test
    fun `Service skal publisere response objekt`() = runBlocking {
        val producer =TestKafkaProducer()
        val request = Medlem(
            "12345678911",
            UUID.randomUUID().toString(),
            Request(LocalDate.now(), "AAP", false),
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