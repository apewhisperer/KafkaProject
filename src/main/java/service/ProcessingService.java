package service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.*;

@Slf4j
public class ProcessingService {

    public void startTopology(String applicationId, String bootstrapServers, String inboxTopic, String outboxTopic) {
        KafkaStreams kafkaStreams = new KafkaStreams(buildTopology(inboxTopic, outboxTopic), createProperties(applicationId, bootstrapServers));
        kafkaStreams.start();
    }

    public Topology buildTopology(String inboxTopic, String outboxTopic) {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        streamsBuilder.stream(inboxTopic)
                .peek((k, v) -> log.debug("$k ::: $v"))
                .to(outboxTopic);
        return streamsBuilder.build();
    }

    private StreamsConfig createProperties(String applicationId, String bootstrapServers) {
        Properties props = new Properties();
        props.put(APPLICATION_ID_CONFIG, applicationId);
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        return new StreamsConfig(props);
    }
}
