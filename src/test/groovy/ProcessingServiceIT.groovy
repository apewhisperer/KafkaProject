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
                '${spring.kafka.config.input-topic}',
                '${spring.kafka.config.output-topic}'
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
    @Value('${spring.kafka.config.input-topic}')
    private String inputTopic;
    @Value('${spring.kafka.config.output-topic}')
    private String outputTopic;
    @Value('${spring.kafka.config.group-id}')
    private String groupId;
    @Value('${spring.kafka.config.auto-offset}')
    private String offset;

    def setup() {
        producer = new KafkaProducer(createProducerProps())
        consumer = new KafkaConsumer(createConsumerProps())
        consumer.subscribe([outputTopic])
    }

    def cleanup() {
        producer.close()
        consumer.close()
    }

    def 'should poll processed message from output topic when initial message is sent to input topic'() {
        given:
        def expected = 'nulla dies sine linea'

        when:
        producer.send(new ProducerRecord(inputTopic, expected))

        then:
        KafkaTestUtils.getSingleRecord(consumer, outputTopic).value == expected
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
