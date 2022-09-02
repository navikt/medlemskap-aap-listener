package no.nav.medlemskap.aap.listener.service

import no.nav.medlemskap.aap.listener.jackson.JacksonParser
import org.junit.Assert
import org.junit.jupiter.api.Test

class JackSonParserTest {
    @Test
    fun testParseRequest(){
        val requestObj = JacksonParser().parse("{\n" +
                "  \"personident\" : \"id\",\n" +
                "  \"id\" : \"9fbe6378-ee66-4ee1-b475-9427546ff901\",\n" +
                "  \"request\" : {\n" +
                "    \"mottattDato\" : \"2022-09-02\",\n" +
                "    \"ytelse\" : \"AAP\",\n" +
                "    \"arbeidetUtenlands\" : false\n" +
                "  },\n" +
                "  \"response\" : null\n" +
                "}\n" +
                "\n" +
                "Process finished with exit code 130\n")
        Assert.assertNotNull(requestObj)
            }

    @Test
    fun testParseLoveMeResponseUavklart(){
        val uavklart = mockUavklartResponse()
        val node = JacksonParser().parseToJsonNode(uavklart)
    }
    @Test
    fun testParseLoveMeResponseJa(){
        val ja = mockJaResponse()
        val node = JacksonParser().parseToJsonNode(ja)
    }
}
