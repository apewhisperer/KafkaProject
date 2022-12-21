package service;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class ProcessingService {

    String inboxTopic = "inbox-topic";
    String outboxTopic = "outbox-topic";

    public void buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        builder.stream(inboxTopic)
                .peek((k, v) -> System.out.printf("KEY: %s \n:::\nVALUE: %s", k, v))
                .to(outboxTopic);
        Topology topology = builder.build();
        KafkaStreams kafkaStreams = new KafkaStreams(topology, createProps());
        kafkaStreams.start();
    }

    private Properties createProps() {
        Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "application");
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        return props;
    }
}
