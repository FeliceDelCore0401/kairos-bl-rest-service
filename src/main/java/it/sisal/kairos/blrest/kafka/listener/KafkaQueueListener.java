package it.sisal.kairos.blrest.kafka.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import it.sisal.kairos.blrest.model.GenericMsg;

@Component
public class KafkaQueueListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaQueueListener.class);

    @KafkaListener(topics = "${kafka.consumer.topics}", groupId = "${kafka.consumer.group-id}",  containerFactory = "kafkaListenerContainerFactory")
    public void testListener(GenericMsg message) {
        logger.info("KAFKAQUEUELISTENER #KAIROS# #IN# Received message: {}", message);
        
    }
}



