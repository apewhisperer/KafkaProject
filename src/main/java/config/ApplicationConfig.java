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
            @Value("${spring.kafka.config.input-topic}") String inputTopic,
            @Value("${spring.kafka.config.output-topic}") String outputTopic
    ) {
        ProcessingService processingService = new ProcessingService();
        processingService.startTopology(applicationId, bootstrapServers, inputTopic, outputTopic);
        return processingService;
    }
}
