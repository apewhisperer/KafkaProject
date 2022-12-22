package service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessingService {

    String inboxTopic = "inbox-topic";
    String outboxTopic = "outbox-topic";

    @Autowired
    public void buildTopology(StreamsBuilder builder) {
        builder.stream(inboxTopic)
                .peek((k, v) -> System.out.println(""))
                .to(outboxTopic);
    }
}
