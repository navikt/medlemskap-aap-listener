package no.nav.medlemskap.aap.river

import junit.framework.Assert.assertNotNull
import no.nav.aap.medlem.model.Svar
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.medlemskap.aap.listener.LovmeAAPSolver
import no.nav.medlemskap.aap.listener.config.Configuration
import no.nav.medlemskap.aap.listener.service.LovMeService
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*
import junit.framework.Assert.assertEquals as assertEquals

class MedlemskapAAPSolverTest {
    private val rapid = TestRapid().apply {
        LovmeAAPSolver(this, LovMeService(Configuration()))
    }
    @Test
    fun `at vi får medlemskapsvurdering tilbake`() {
       val uuid =  UUID.randomUUID().toString();
        //language=JSON
        rapid.sendTestMessage(
            """{
          "@behov": [
            "medlemskap-vurdering"
          ],
          "@id": "$uuid",
          "folkeregisteridentifikator": "12345678900",
          "medlemRequest" : {}
        }
            """.trimIndent()
        )

        with(rapid.inspektør) {
            val field = field(0,"MedlemResponse")
            assertNotNull(field(0, "MedlemResponse"))
            assertEquals(Svar.JA.name, field(0, "MedlemResponse")["erMedlem"].asText())
        }
    }
}