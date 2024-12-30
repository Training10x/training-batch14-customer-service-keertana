package com.example.customer_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomerProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public CustomerProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private final Logger logger = LoggerFactory.getLogger(CustomerProducerService.class);


    public void sendCustomerId(Long customerId) {
        kafkaTemplate.send("candidate-topic", customerId.toString());
        logger.info("Customer ID sent to Kafka: {}", customerId);

    }

}
