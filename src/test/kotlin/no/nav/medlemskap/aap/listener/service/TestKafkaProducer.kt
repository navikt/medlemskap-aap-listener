package no.nav.medlemskap.aap.listener.service


import no.nav.medlemskap.aap.listener.domain.MedlemKafkaDto
import no.nav.medlemskap.aap.listener.kafka.KafkaProduser

class TestKafkaProducer:KafkaProduser {
    val broker: HashMap<String,ArrayList<MedlemKafkaDto>> = HashMap()
    override fun publish(topic: String, key: String, value: MedlemKafkaDto) {
        if (broker.containsKey(topic)){
            broker.get(topic)?.add(value)
        }
        else{
            broker.put(topic, arrayListOf())
            broker.get(topic)?.add(value)
        }

    }
}