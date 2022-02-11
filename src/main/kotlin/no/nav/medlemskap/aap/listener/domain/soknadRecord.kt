package no.nav.medlemskap.aap.listener.domain

import no.nav.aap.avro.medlem.v1.Medlem

data class AapRecord(val partition:Int, val offset:Long, val key:String?, val topic:String, val aapRequest:Medlem) {

}
