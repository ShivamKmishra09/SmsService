package com.example.demo.service;

import com.example.demo.model.SmsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger logger =
            LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(
            topics = "sms-events",
            groupId = "sms-group"
    )
    public void consumeSmsEvent(SmsEvent smsEvent) {

        logger.info(
                "Consumed SMS Event -> Phone: {}, Message: {}, Status: {}",
                smsEvent.getPhoneNumber(),
                smsEvent.getMessage(),
                smsEvent.getStatus()
        );
    }
}