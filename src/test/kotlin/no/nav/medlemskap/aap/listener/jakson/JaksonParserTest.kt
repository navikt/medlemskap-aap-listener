package no.nav.medlemskap.aap.listener.jakson

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test



class JaksonParserTest {

    @Test
    fun `parse Json String`() {
        val fileContent = this::class.java.classLoader.getResource("sampleRequest.json").readText(Charsets.UTF_8)
        val sykepengeSoknad = JaksonParser().parse(fileContent)
        assertNotNull(sykepengeSoknad)
    }

}