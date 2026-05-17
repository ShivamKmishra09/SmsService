package com.example.demo.controller;

import com.example.demo.model.SmsRequest;
import com.example.demo.model.SmsResponse;
import com.example.demo.service.SmsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v1/sms")
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/send")
    public SmsResponse sendSms(
            @Valid @RequestBody SmsRequest request) {

        return smsService.sendSms(request);
    }
}