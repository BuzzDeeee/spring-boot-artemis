package net.seraph.artemistest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.Lifecycle;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.wildfly.common.annotation.NotNull;

@SpringBootTest
class ArtemisServiceTest {
    @Autowired
    ArtemisService artemisService;

    @Autowired
    JmsListenerEndpointRegistry listenerEndpointRegistry;

    @MockBean
    Logger logger;

    @Test
    void testMessage() throws InterruptedException {

        Instant nowPlus = Instant.now().plus(3000, ChronoUnit.MILLIS);

        artemisService.sendMessage("foobar", nowPlus);
        artemisService.sendMessage("foobar", nowPlus);

        Thread.sleep(2000);
        verify(logger, times(0)).debug("foo");

        Thread.sleep(2000);

        verify(logger, times(1)).debug("foo");
    }


    @NotNull
    private MessageListenerContainer getAccountUpdateSubscriber() {

        return listenerEndpointRegistry.getListenerContainers().stream()
                .filter(MessageListenerContainer::isPubSubDomain)
                .filter(messageListenerContainer ->
                        ((DefaultMessageListenerContainer) messageListenerContainer).getSubscriptionName().equals(ArtemisService.MESSAGE_TEST_QUEUE)
                )
                .collect(onlyElement());
    }
}