package be.mathiasbosman.mqttstresstest.domain;

import be.mathiasbosman.mqttstresstest.configuration.MqttConfig;
import be.mathiasbosman.mqttstresstest.configuration.StressTestConfiguration;
import be.mathiasbosman.mqttstresstest.util.MqttUtils;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.NonNull;
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
  private final StressTestConfiguration testConfig;
  private final Map<String, StatisticRecord> processCount;

  @Value("${spring.task.execution.pool.core-size}")
  private int executionPoolSize;

  private LocalDateTime endTime;

  private boolean isConfigurationValid() {
    if (testConfig.getMinimumClientIndex() > testConfig.getMaximumClientIndex()) {
      log.error("The minimum index should not be bigger then the maximum index");
      return false;
    }

    int totalClients =
        testConfig.getMaximumClientIndex() - testConfig.getMinimumClientIndex() + 1;
    if (executionPoolSize < totalClients) {
      log.error("The task execution pool size {} is smaller than the amount of clients {}",
          executionPoolSize, totalClients);
      return false;
    }

    return true;
  }

  @PostConstruct
  public void setupStressTest() {
    if (!isConfigurationValid()) {
      throw new IllegalStateException(
          "Wrong configuration detected. Check the error logs for more info");
    }

    int totalClients =
        testConfig.getMaximumClientIndex() - testConfig.getMinimumClientIndex() + 1;
    endTime = LocalDateTime.now().plusSeconds(testConfig.getTtl());
    log.info("Start of stress test with {} clients sending {} data points every {} ms",
        totalClients,
        testConfig.getAmountOfDataPoints(),
        testConfig.getMessageDelay());
    log.info("Connection retention is set to {}", testConfig.isRetainConnection());
    log.info("Will run for {} s and initiate shutdown at {}", testConfig.getTtl(), endTime);
    log.info("Data point creation timestamp addition = {}",
        testConfig.isIncludeCreationTimestamp());

    IntStream.range(
            testConfig.getMinimumClientIndex(),
            testConfig.getMaximumClientIndex() + 1)
        .forEach(index -> {
          String token = testConfig.getTokenPrefix() + index;
          mqttAsyncService.createClientAndStartPublishing(
              mqttConfig.getServerUrl(),
              MqttUtils.createConnectOptions(token),
              this::publishForClient,
              testConfig.getMessageDelay(),
              testConfig.isRetainConnection());
        });
  }

  private void publishForClient(@NonNull IMqttClient client) {
    if (LocalDateTime.now().isAfter(endTime)) {
      mqttAsyncService.stopClients();
      return;
    }

    LocalDateTime publishTime = LocalDateTime.now();
    List<DataPointRecord> dataPoints = generateDataPoints(
        testConfig.getAmountOfDataPoints());
    if (testConfig.isIncludeCreationTimestamp()) {
      dataPoints.add(new DataPointRecord("created_at",
          publishTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
    }
    publishData(client, dataPoints, publishTime);
  }

  private void publishData(@NonNull IMqttClient client, @NonNull List<DataPointRecord> dataPoints,
      @NonNull LocalDateTime publishTime) {
    MqttMessage msg = new MqttMessage(getJsonString(dataPoints).getBytes(StandardCharsets.UTF_8));
    msg.setQos(mqttConfig.getQosLevel());
    msg.setRetained(mqttConfig.isRetainMessages());

    String clientId = client.getClientId();
    processCount.putIfAbsent(clientId, new StatisticRecord(LocalDateTime.now()));

    try {
      log.debug("Publishing data with client {}", clientId);
      client.publish(mqttConfig.getTopic(), msg);
      StatisticRecord statisticRecord = processCount.get(clientId);
      statisticRecord.publish(publishTime);
      processCount.put(clientId, statisticRecord);
      log.trace("Statistics for {} = {}", clientId, statisticRecord);
    } catch (MqttException e) {
      log.error("Error while publishing with {}", clientId, e);
    }
  }

  private List<DataPointRecord> generateDataPoints(int amount) {
    return IntStream.range(1, amount)
        .mapToObj(index -> {
          String key = "key_" + index;
          String value = String.valueOf(Math.random());
          return new DataPointRecord(key, value);
        })
        .collect(Collectors.toList());
  }

  private String getJsonString(@NonNull List<DataPointRecord> records) {
    return "{"
        + records.stream()
        .map(DataPointRecord::toString)
        .collect(Collectors.joining(","))
        + "}";
  }

}
