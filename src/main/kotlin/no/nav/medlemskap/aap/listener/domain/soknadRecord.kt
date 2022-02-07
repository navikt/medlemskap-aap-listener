package no.nav.medlemskap.aap.listener.domain

data class SoknadRecord(val partition:Int,val offset:Long,val value : String, val key:String?,val topic:String,val sykepengeSoknad:LovmeSoknadDTO)
