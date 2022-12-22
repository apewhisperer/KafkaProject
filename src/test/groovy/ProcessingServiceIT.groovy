import config.ApplicationConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import service.ProcessingService
import spock.lang.Specification

@EmbeddedKafka(
        partitions = 1,
        topics = [
                '${spring.kafka.config.inbox-topic}',
                '${spring.kafka.config.outbox-topic}'
        ],
        brokerProperties = [
                'listeners=PLAINTEXT://${spring.kafka.config.bootstrap-servers}'
        ]
)
@SpringBootTest(classes = ApplicationConfig.class)
class ProcessingServiceIT extends Specification {

    private KafkaProducer producer
    private KafkaConsumer consumer
    @Autowired
    private ProcessingService processingService;
    @Autowired
    private ApplicationConfig applicationConfig;
    @Value('${spring.kafka.config.bootstrap-servers}')
    private String bootstrapServers;
    @Value('${spring.kafka.config.inbox-topic}')
    private String inboxTopic;
    @Value('${spring.kafka.config.outbox-topic}')
    private String outboxTopic;
    @Value('${spring.kafka.config.group-id}')
    private String groupId;
    @Value('${spring.kafka.config.auto-offset}')
    private String offset;

    def setup() {
        producer = new KafkaProducer(createProducerProps())
        consumer = new KafkaConsumer(createConsumerProps())
        consumer.subscribe([outboxTopic])
    }

    def cleanup() {
        producer.close()
        consumer.close()
    }

    def 'should poll processed message from outbox topic when initial message is sent to inbox topic'() {
        given:
        def expected = 'nulla dies sine linea'

        when:
        producer.send(new ProducerRecord(inboxTopic, expected))

        then:
        KafkaTestUtils.getSingleRecord(consumer, outboxTopic).value == expected
    }

    private Properties createConsumerProps() {
        def props = new Properties()
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.name)
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.name)
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset)
        props
    }

    private Properties createProducerProps() {
        def props = new Properties()
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.name)
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.name)
        props
    }
}
