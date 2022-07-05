package be.mathiasbosman.mqttstresstest.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MqttAsyncService {

  private final List<IMqttClient> clients = new LinkedList<>();
  private boolean isStopped;


  @Async
  public void createClientAndStartPublishing(String serverUrl,
      String username, Consumer<IMqttClient> publisher) {
    if (isStopped) {
      log.warn("Stop signal received");
      return;
    }
    try {
      // configure options
      MqttConnectOptions options = new MqttConnectOptions();
      options.setAutomaticReconnect(true);
      options.setCleanSession(true);
      options.setConnectionTimeout(10);
      options.setUserName(username);
      try(IMqttClient client = new MqttClient(serverUrl, "mock_client_" + options.getUserName(),
          new MemoryPersistence())) {
        log.info("Connecting client {}", client.getClientId());
        client.connect(options);
        clients.add(client);
        while (!isStopped) {
          publisher.accept(client);
        }
        publisher.accept(client);
      }
    } catch (MqttException e) {
      if (isStopped) {
        log.warn("Stop signal received, connection closing");
      } else {
        log.error("Error connecting to client for user {}", username);
      }
    }
  }

  @PreDestroy
  public void stopClients() {
    isStopped = true;
    if (clients.isEmpty()) {
      log.debug("No clients to close");
      return;
    }
    log.warn("Stop signal received, closing {} client(s)", clients.size());
    clients.forEach(client -> {
      try {
        log.info("Closing client {}", client.getClientId());
        client.disconnect();
      } catch (MqttException e) {
        log.error("Error while closing client {}", client.getClientId(), e);
      }
    });
  }
}
