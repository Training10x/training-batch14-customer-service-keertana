package com.example.customer_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
public class CustomerConsumerService {


    private final Logger logger = LoggerFactory.getLogger(CustomerConsumerService.class);

    @KafkaListener(topics = "candidate-response-topic", groupId = "customer-group")
    public void consumeResponse(String message) {
        logger.info("Response received from Kafka: {}", message);
    }
}
