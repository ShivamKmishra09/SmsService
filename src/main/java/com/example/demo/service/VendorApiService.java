package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class VendorApiService {

    private final Random random = new Random();

    public boolean sendSmsToVendor(
            String phoneNumber,
            String message
    ) {

        return random.nextBoolean();
    }
}