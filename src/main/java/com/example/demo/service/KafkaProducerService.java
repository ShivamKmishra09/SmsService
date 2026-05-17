package com.example.demo.service;

import com.example.demo.model.SmsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class KafkaProducerService {

    private static final Logger logger =
            LoggerFactory.getLogger(KafkaProducerService.class);

    private static final String TOPIC = "sms-events";

    private final KafkaTemplate<String, SmsEvent> kafkaTemplate;

    public KafkaProducerService(
            KafkaTemplate<String, SmsEvent> kafkaTemplate
    ) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishSmsEvent(SmsEvent smsEvent) {

        logger.info(
                "Publishing SMS event to Kafka: {}",
                smsEvent
        );

        kafkaTemplate.send(TOPIC, smsEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.error("Failed to publish SMS event: {}", ex.getMessage());
                    } else {
                        logger.info("SMS event published to partition {}",
                                result.getRecordMetadata().partition());
                    }
                });
    }
}