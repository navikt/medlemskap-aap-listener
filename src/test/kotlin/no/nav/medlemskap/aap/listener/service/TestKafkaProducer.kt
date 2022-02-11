package no.nav.medlemskap.aap.listener.service

import no.nav.aap.avro.medlem.v1.Medlem
import no.nav.medlemskap.aap.listener.Kafka.KafkaProduser

class TestKafkaProducer:KafkaProduser {
    val broker: HashMap<String,ArrayList<Medlem>> = HashMap()
    override fun publish(topic: String, key: String, value: Medlem) {
        if (broker.containsKey(topic)){
            broker.get(topic)?.add(value)
        }
        else{
            broker.put(topic, arrayListOf())
            broker.get(topic)?.add(value)
        }

    }
}