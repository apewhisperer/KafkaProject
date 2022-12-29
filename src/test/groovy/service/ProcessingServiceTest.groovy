package service


import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import spock.lang.Specification

import static org.apache.kafka.streams.StreamsConfig.*

class ProcessingServiceTest extends Specification {

    private final String APPLICATION_ID = 'id'
    private final String BOOTSTRAP_SERVERS = 'localhost:1234'
    private final String INPUT_TOPIC = 'input-topic'
    private final String OUTPUT_TOPIC = 'output-topic'
    private ProcessingService processingService
    private TopologyTestDriver testDriver
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;

    def setup() {
        processingService = Spy(ProcessingService)
        testDriver = new TopologyTestDriver(processingService.buildTopology(INPUT_TOPIC, OUTPUT_TOPIC), createProperties())
        inputTopic = testDriver.createInputTopic(INPUT_TOPIC, new StringSerializer(), new StringSerializer())
        outputTopic = testDriver.createOutputTopic(OUTPUT_TOPIC, new StringDeserializer(), new StringDeserializer())
    }

    def cleanup() {
        if (testDriver != null) {
            testDriver.close()
        }
    }

    def 'should invoke build topology method when starting the topology'() {
        when:
        processingService.startTopology(APPLICATION_ID, BOOTSTRAP_SERVERS, INPUT_TOPIC, OUTPUT_TOPIC)

        then:
        1 * processingService.buildTopology(INPUT_TOPIC, OUTPUT_TOPIC)
    }

    def 'should return processed message from output topic when initial message is sent to input topic'() {
        given:
        testDriver = new TopologyTestDriver(processingService.buildTopology(INPUT_TOPIC, OUTPUT_TOPIC), createProperties())
        def expected = 'nulla dies sine linea'

        when:
        inputTopic.pipeInput(expected)

        then:
        outputTopic.readValue() == expected
    }

    private Properties createProperties() {
        Properties props = new Properties()
        props.put(APPLICATION_ID_CONFIG, APPLICATION_ID)
        props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS)
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().class.name)
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().class.name)
        props
    }
}
