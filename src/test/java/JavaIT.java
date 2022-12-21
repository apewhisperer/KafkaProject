import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import service.ProcessingService;

import java.util.List;
import java.util.Properties;

@EmbeddedKafka(
        partitions = 1,
        topics = {
                "inbox-topic",
                "outbox-topic"
        },
        brokerProperties = {
                "listeners=PLAINTEXT://127.0.0.1:9092"
        }
)
public class JavaIT {

        private String localhost = "127.0.0.1:9092";
        private String inboxTopic = "inbox-topic";
        private String outboxTopic = "outbox-topic";
        private String groupId = "group-id";
        private String offset = "earliest";

        @Test
        public void should() {
                ProcessingService service = new ProcessingService();
                service.buildTopology();
                KafkaProducer kafkaProducer = new KafkaProducer(createProducerProps());
                KafkaConsumer kafkaConsumer = new KafkaConsumer(createConsumerProps());
                kafkaConsumer.subscribe(List.of(outboxTopic));

                kafkaProducer.send(new ProducerRecord(inboxTopic, "msg"));

                assert KafkaTestUtils.getSingleRecord(kafkaConsumer, outboxTopic).value().equals("msg");
        }

        Properties createConsumerProps() {
                var props = new Properties();
                props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, localhost);
                props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
                props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);
                return props;
        }

        Properties createProducerProps() {
                var props = new Properties();
                props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, localhost);
                props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                return props;
        }
}
