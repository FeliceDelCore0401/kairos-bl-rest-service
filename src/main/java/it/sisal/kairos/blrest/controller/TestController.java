package it.sisal.kairos.blrest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.sisal.kairos.blrest.model.GenericMsg;
import it.sisal.kairos.blrest.service.BlRestService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class TestController {

    private final KafkaTemplate<String, GenericMsg> kafkaTemplate;
    
    private final BlRestService blRestService;

    public TestController(KafkaTemplate<String, GenericMsg> kafkaTemplate, BlRestService blRestService) {
        this.kafkaTemplate = kafkaTemplate;
        this.blRestService = blRestService;
    }

    @PostMapping("/genericTestkafka")
    public ResponseEntity<String> sendToKafka(@RequestParam("type") int type, 
                                              @RequestParam("queueName") String queueName, 
                                              @RequestBody GenericMsg genericMsg) {
        try {
        	log.debug("Preparing message for Kafka: {}", genericMsg);
        	log.debug("Sending message to Kafka topic: {}", queueName);
            kafkaTemplate.send(queueName, genericMsg);
            return ResponseEntity.ok("Message successfully sent to Kafka topic " + queueName);
        } catch (Exception e) {
            log.error("Error sending message to Kafka topic {}: {}", queueName, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error sending message to Kafka topic " + queueName);
        }
    }
    
    @PostMapping("/genericTest")
    public void test(@RequestParam("type") int type, @RequestBody(required = false) GenericMsg genericMsg) {
        blRestService.sendGenericMessageRequest(type, genericMsg);
    }

}