package be.mathiasbosman.mqttstresstest.domain;

import be.mathiasbosman.mqttstresstest.configuration.MqttConfig;
import be.mathiasbosman.mqttstresstest.configuration.StressTestConfiguration;
import be.mathiasbosman.mqttstresstest.util.MqttUtils;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StressService {

  private final MqttAsyncService mqttAsyncService;
  private final MqttConfig mqttConfig;
  private final StressTestConfiguration testConfiguration;

  @Value("${spring.task.execution.pool.core-size}")
  private int executionPoolSize;

  private LocalDateTime endTime;

  @PostConstruct
  public void startStressTest() {
    if (executionPoolSize < testConfiguration.getAmountOfClients()) {
      log.error("The task execution pool size {} is smaller than the amount of clients {}",
          executionPoolSize, testConfiguration.getAmountOfClients());
      return;
    }
    endTime = LocalDateTime.now().plusSeconds(testConfiguration.getTtl());
    log.info("Start of stress test with {} clients sending {} data points every {} ms",
        testConfiguration.getAmountOfClients(),
        testConfiguration.getAmountOfDataPoints(),
        testConfiguration.getMessageDelay());
    log.info("Will run for {} s and initiate shutdown at {}", testConfiguration.getTtl(), endTime);
    IntStream.range(1, testConfiguration.getAmountOfClients() + 1)
        .forEach(index -> {
          String token = testConfiguration.getTokenPrefix() + index;
          mqttAsyncService.createClientAndStartPublishing(
              mqttConfig.getServerUrl(),
              MqttUtils.createConnectOptions(token),
              this::publishForClient);
        });
  }

  public void publishForClient(IMqttClient client) {
    if (LocalDateTime.now().isAfter(endTime)) {
      mqttAsyncService.stopClients();
      return;
    }
    publishData(client, generateDataPoints());
    try {
      TimeUnit.MILLISECONDS.sleep(testConfiguration.getMessageDelay());
    } catch (InterruptedException e) {
      log.debug("Thread interrupted for client {}", client.getClientId());
    }
  }

  private void publishData(IMqttClient client, List<DataPointRecord> dataPoints) {
    log.trace("Creating message");
    MqttMessage msg = new MqttMessage(getJsonString(dataPoints).getBytes(StandardCharsets.UTF_8));
    msg.setQos(mqttConfig.getQosLevel());
    msg.setRetained(mqttConfig.isRetainMessages());
    try {
      log.debug("Publishing data with client {}", client.getClientId());
      client.publish(mqttConfig.getTopic(), msg);
    } catch (MqttException e) {
      log.error("Error while publishing", e);
    }
  }

  private List<DataPointRecord> generateDataPoints() {
    List<DataPointRecord> result = IntStream.range(1, testConfiguration.getAmountOfDataPoints())
        .mapToObj(index -> {
          String key = "key_" + index;
          String value = String.valueOf(Math.random());
          return new DataPointRecord(key, value);
        })
        .collect(Collectors.toList());
    if (testConfiguration.isIncludeCreationTimestamp()) {
      result.add(new DataPointRecord("created_at", System.currentTimeMillis()));
    }
    return result;
  }

  private String getJsonString(List<DataPointRecord> records) {
    return "{"
        + records.stream().map(DataPointRecord::toString).collect(Collectors.joining(","))
        + "}";
  }

}
