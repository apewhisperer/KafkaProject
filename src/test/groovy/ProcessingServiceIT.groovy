import config.KafkaConfig
import groovy.transform.CompileStatic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.ClassRule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.test.rule.EmbeddedKafkaRule
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.ContextConfiguration
import service.ProcessingService

import static org.apache.kafka.streams.StreamsConfig.*

@ContextConfiguration(classes = KafkaConfig)
class ProcessingServiceIT {

    @Autowired
    ProcessingService service

    @Autowired
    KafkaConfig kafkaConfig

    def inboxTopic = 'inbox-topic'
    def outboxTopic = 'outbox-topic'
    def groupId = 'group-id'
    def offset = 'earliest'

    @ClassRule
    EmbeddedKafkaRule rule = new EmbeddedKafkaRule(1, true, inboxTopic, outboxTopic)

    def setup() {
        rule.before()
    }

    def 'should process message when kafka message is received'() {
        given:
        def producer = new KafkaProducer(createProducerProps())
        def consumer = new KafkaConsumer(createConsumerProps())
        def expected = 'secret message'
        consumer.subscribe([outboxTopic])

        when:
        producer.send(new ProducerRecord(inboxTopic, expected))
        def actual = KafkaTestUtils.getSingleRecord(consumer, outboxTopic)

        then:
        assert actual.value == expected
    }

    Properties createConsumerProps() {
        def props = new Properties()
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, rule.getEmbeddedKafka().getBrokersAsString())
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.name)
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.name)
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset)
        props
    }

    Properties createProducerProps() {
        def props = new Properties()
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, rule.getEmbeddedKafka().getBrokersAsString())
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.name)
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.name)
        props
    }

    @CompileStatic
    @Configuration
    static class SpringConfig {
        @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
        KafkaStreamsConfiguration kStreamsConfig() {
            Map<String, Object> props = new HashMap<>()
            props.put(APPLICATION_ID_CONFIG, "streams-app")
            props.put(BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
            props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName())
            props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName())
            new KafkaStreamsConfiguration(props)
        }

        @Bean
        ProcessingService processingService() {
            new ProcessingService()
        }
    }
}
