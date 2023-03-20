package net.seraph.artemistest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
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
    void testLVQWithScheduledMessage() throws InterruptedException {

        Instant nowPlus = Instant.now().plus(3000, ChronoUnit.MILLIS);

        artemisService.sendMessage("foobar", nowPlus);
        artemisService.sendMessage("foobar", nowPlus);

        Thread.sleep(2000);
        // scheduled message shouldn't be processed that soon
        verify(logger, times(0)).debug("foo");

        Thread.sleep(2000);

        // due to last-value only 1 message should appear
        verify(logger, times(1)).debug("foo");
    }


    @Test
    void testLVQ() throws InterruptedException {

        // stop subscriber so we can force lvq
        getSubscriber().stop();
        await().untilAsserted(() -> assertThat(getSubscriber()).matches(not(Lifecycle::isRunning)));

        artemisService.sendMessage("foobar");
        artemisService.sendMessage("foobar");

        Thread.sleep(500);

        getSubscriber().start();
        await().untilAsserted(() -> assertThat(getSubscriber()).matches(Lifecycle::isRunning));

        // due to last-value only 1 message should appear
        verify(logger, times(1)).debug("foo");
    }


    @NotNull
    private MessageListenerContainer getSubscriber() {

        return listenerEndpointRegistry.getListenerContainers().stream()
                .filter(MessageListenerContainer::isPubSubDomain)
                .filter(messageListenerContainer ->
                        Objects.equals(((DefaultMessageListenerContainer) messageListenerContainer).getSubscriptionName(),
                                ArtemisService.MESSAGE_TEST_QUEUE)
                )
                .collect(onlyElement());
    }
}