package net.buzzdeeee.artemistest;

import java.time.Instant;

import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class ArtemisService {

    public static final String MESSAGE_TEST_ID = "messageTestId";
    public static final String MESSAGE_TEST_ADDRESS = "messageTest?last-value=true&last-value-key=" + MESSAGE_TEST_ID;
    public static final ActiveMQTopic MESSAGE_TEST_TOPIC = new ActiveMQTopic(MESSAGE_TEST_ADDRESS);
    public static final String MESSAGE_TEST_QUEUE = "messageTest";


    JmsTemplate jmsTemplate;
    Logger logger;

    @Autowired
    public ArtemisService(JmsTemplate jmsTemplate, Logger logger) {

        this.jmsTemplate = jmsTemplate;
        this.logger = logger;
    }

    public void sendMessage(String message) {
        sendMessage(message, null);
    }


    public void sendMessage(String message, Instant deliveryTime) {
        jmsTemplate.convertAndSend(MESSAGE_TEST_TOPIC, message, msg -> {
                    if (deliveryTime != null) {
                        msg.setJMSDeliveryTime(deliveryTime.toEpochMilli());
                    }
                    msg.setStringProperty(MESSAGE_TEST_ID, "1");
                    return msg;
                }
        );
    }


    @JmsListener(destination = MESSAGE_TEST_ADDRESS, containerFactory = "myFactory", subscription = MESSAGE_TEST_QUEUE)
    public void receiveMessage(Message<String> message) {

        System.out.println("Received <headers=" + message.getHeaders() + " payLoad=" + message.getPayload() + ">");
        logger.debug("foo");
    }
}
