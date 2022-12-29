package service;

import logging.KafkaProjectLogger;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.*;

public class ProcessingService {

    public void startTopology(String applicationId, String bootstrapServers, String inputTopic, String outputTopic) {
        KafkaStreams kafkaStreams = createKafkaStreams(buildTopology(inputTopic, outputTopic), createStreamsConfig(applicationId, bootstrapServers));
        kafkaStreams.start();
    }

    public Topology buildTopology(String inputTopic, String outputTopic) {
        StreamsBuilder streamsBuilder = createStreamsBuilder();
        streamsBuilder.stream(inputTopic)
                .peek((k, v) -> KafkaProjectLogger.debug("$k ::: $v"))
                .to(outputTopic);
        return streamsBuilder.build();
    }

    private static KafkaStreams createKafkaStreams(Topology topology, StreamsConfig streamsConfig) {
        return new KafkaStreams(topology, streamsConfig);
    }

    private static StreamsBuilder createStreamsBuilder() {
        return new StreamsBuilder();
    }

    private StreamsConfig createStreamsConfig(String applicationId, String bootstrapServers) {
        Properties props = new Properties();
        props.put(APPLICATION_ID_CONFIG, applicationId);
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        return new StreamsConfig(props);
    }
}
