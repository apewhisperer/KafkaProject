package logging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaProjectLogger {

    public static void info(String msg) {
        log.info(msg);
    }

    public static void debug(String msg) {
        log.debug(msg);
    }
}
