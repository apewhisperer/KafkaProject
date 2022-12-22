package config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import service.ProcessingService;

@Configuration
public class ApplicationConfig {

    @Bean
    ProcessingService processingService(
            @Value("${spring.kafka.config.application-id}") String applicationId,
            @Value("${spring.kafka.config.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.config.inbox-topic}") String inboxTopic,
            @Value("${spring.kafka.config.outbox-topic}") String outboxTopic
    ) {
        ProcessingService processingService = new ProcessingService();
        processingService.startTopology(applicationId, bootstrapServers, inboxTopic, outboxTopic);
        return processingService;
    }
}
