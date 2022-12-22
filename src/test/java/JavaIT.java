import config.KafkaConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import service.ProcessingService;

import java.util.List;
import java.util.Properties;

@EmbeddedKafka(
        partitions = 1,
        topics = {
                "${spring.kafka.test.inbox-topic}",
                "${spring.kafka.test.outbox-topic}"
        },
        brokerProperties = {
                "listeners=PLAINTEXT://${spring.kafka.test.bootstrap-servers}"
        }
)
@SpringBootTest(classes = {KafkaConfig.class, ProcessingService.class})
public class JavaIT {

        @Value("${spring.kafka.test.bootstrap-servers}")
        private String bootstrapServers;
        @Value("${spring.kafka.test.inbox-topic}")
        private String inboxTopic;
        @Value("${spring.kafka.test.outbox-topic}")
        private String outboxTopic;
        @Value("${spring.kafka.test.group-id}")
        private String groupId;
        @Value("${spring.kafka.test.auto-offset}")
        private String offset;
        @Autowired
        ProcessingService service;
        @Autowired
        KafkaConfig kafkaConfig;

        @Test
        public void shouldPollProcessedMessageWhenEventIsSentToKafka() {
                var expected = "nulla dies sine linea";
                KafkaProducer kafkaProducer = new KafkaProducer(createProducerProps());
                KafkaConsumer kafkaConsumer = new KafkaConsumer(createConsumerProps());
                kafkaConsumer.subscribe(List.of(outboxTopic));

                kafkaProducer.send(new ProducerRecord(inboxTopic, expected));

                assert KafkaTestUtils.getSingleRecord(kafkaConsumer, outboxTopic).value().equals(expected);
        }

        Properties createConsumerProps() {
                var props = new Properties();
                props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
                props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);
                return props;
        }

        Properties createProducerProps() {
                var props = new Properties();
                props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                return props;
        }
}
