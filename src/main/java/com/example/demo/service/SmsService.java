package com.example.demo.service;

import com.example.demo.model.SmsEvent;
import com.example.demo.model.SmsRequest;
import com.example.demo.model.SmsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger logger =
            LoggerFactory.getLogger(SmsService.class);

    private final VendorApiService vendorApiService;
    private final BlockedUserService blockedUserService;
    private final KafkaProducerService kafkaProducerService;

    public SmsService(
            VendorApiService vendorApiService,
            BlockedUserService blockedUserService,
            KafkaProducerService kafkaProducerService
    ) {

        this.vendorApiService = vendorApiService;
        this.blockedUserService = blockedUserService;
        this.kafkaProducerService = kafkaProducerService;
    }

    public SmsResponse sendSms(SmsRequest request) {

        if(blockedUserService.isBlocked(
                request.getPhoneNumber()
        )) {

            return new SmsResponse(
                    "FAILURE",
                    "User is blocked"
            );
        }

        logger.info(
                "Sending SMS to {}",
                request.getPhoneNumber()
        );

        boolean success =
                vendorApiService.sendSmsToVendor(
                        request.getPhoneNumber(),
                        request.getMessage()
                );

        if(success) {

            SmsEvent smsEvent = new SmsEvent(
                    request.getPhoneNumber(),
                    request.getMessage(),
                    "SUCCESS"
            );

            kafkaProducerService.publishSmsEvent(smsEvent);

            return new SmsResponse(
                    "SUCCESS",
                    "SMS Sent Successfully"
            );
        }
        return new SmsResponse(
                "FAILURE",
                "SMS Sending Failed"
        );
    }
}