package com.sanket.localstacksqs.localsqs;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanket.localstacksqs.localsqs.model.EventData;
import com.sanket.localstacksqs.localsqs.model.SampleEvent;
import lombok.SneakyThrows;
import org.awaitility.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class ControllerIntegrationTest {

  @Autowired
  public AmazonSQSAsync amazonSQSAsync;

  @Autowired
  @Qualifier("jsonMapper")
  private ObjectMapper objectMapper;

  @Value("${cloud.aws.sqs.outgoing-queue.url}")
  private String incoming = "incoming-queue";

  @Value("${cloud.aws.sqs.outgoing-queue.url}")
  private String outgoing = "outgoing-queue";

  @SneakyThrows
  @Test
  void testCompleteFlow() {
    amazonSQSAsync.sendMessage(incoming, objectMapper.writeValueAsString(mockEvent()));

    Thread.sleep(3000);
    await()
        .pollInterval(Duration.FIVE_HUNDRED_MILLISECONDS)
        .atMost(Duration.TEN_SECONDS)
        .untilAsserted(this::assertOutgoingEvent);
  }

  @SneakyThrows
  private void assertOutgoingEvent() {
    var events = amazonSQSAsync.receiveMessage(outgoing).getMessages();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isNotNull();
    var sampleEvent = objectMapper.readValue(events.get(0).getBody(), SampleEvent.class);
    assertThat(events.get(0)).isNotNull();
    assertThat(sampleEvent.getType()).isEqualTo("incoming-message");
    assertThat(sampleEvent.getData().getEventType()).isEqualTo(EventData.EventType.PROCESSED);
  }

  private SampleEvent mockEvent() {
    return SampleEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventTime(ZonedDateTime.now())
        .type("incoming-message")
        .version("1.0")
        .data(EventData.builder()
            .eventType(EventData.EventType.CREATED)
            .age(20)
            .name("spring")
            .description("This is user created  incoming event")
            .build())
        .build();
  }
}
