import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import spock.lang.Specification

import java.time.Duration

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = ['inbox-topic', 'outbox-topic'],
        brokerProperties = [
                'listeners=PLAINTEXT://localhost:9090'
        ])
class ProcessingServiceIT extends Specification {

    def 'should process message when kafka message is received'() {
        given:
        def producer = new KafkaProducer(createProducerProps())
        def consumer = new KafkaConsumer(createConsumerProps())
        def actual
        def expected = 'secret message'
        consumer.subscribe(['outbox-topic'])

        when:
        producer.send(new ProducerRecord<String, String>('outbox-topic', 'secret message'))
        actual = consumer.poll(Duration.ofMillis(1000))

        then:
        assert actual.first() == expected
    }

    Properties createConsumerProps() {
        def props = new Properties()
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 'localhost:9090')
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.name)
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.name)
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, 'group-id')
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, 'earliest')
        props
    }

    Properties createProducerProps() {
        def props = new Properties()
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 'localhost:9090')
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.name)
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.name)
        props
    }
}
