package config;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import service.ProcessingService;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.streams.StreamsConfig.*;

@Configuration
public class ApplicationConfig {

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig(
            @Value("${spring.kafka.config.application-id}") String applicationId,
            @Value("${spring.kafka.config.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(APPLICATION_ID_CONFIG, applicationId);
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    ProcessingService processingService(
            @Value("${spring.kafka.config.application-id}") String applicationId,
            @Value("${spring.kafka.config.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.test.inbox-topic}") String inboxTopic,
            @Value("${spring.kafka.test.outbox-topic}") String outboxTopic
    ) {
        ProcessingService processingService = new ProcessingService();
        processingService.startTopology(applicationId, bootstrapServers, inboxTopic, outboxTopic);
        return processingService;
    }
}
