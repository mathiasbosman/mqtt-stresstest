package be.mathiasbosman.mqttstresstest.domain;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttAsyncService {

  private final ApplicationContext applicationContext;
  private final Set<IMqttClient> clients = new LinkedHashSet<>();
  private final Set<String> connectedUsers = new LinkedHashSet<>();

  private boolean isStopped;


  @Async
  public void createClientAndStartPublishing(String serverUrl,
      MqttConnectOptions options, Consumer<IMqttClient> publisher) {
    String username = options.getUserName();
    if (connectedUsers.contains(username)) {
      log.warn("Username {} already in connected pool", username);
      return;
    }
    if (isStopped) {
      log.warn("Stop signal received");
      return;
    }
    try {
      try (IMqttClient client = new MqttClient(
          serverUrl,
          "mock_client_" + username,
          new MemoryPersistence())) {
        log.debug("Connecting client {}", client.getClientId());
        client.connect(options);
        clients.add(client);
        connectedUsers.add(username);
        log.info("Start of publishing messages for client {}", client.getClientId());
        while (!isStopped) {
          publisher.accept(client);
        }
        closeConnection(client);
        if (clients.isEmpty()) {
          closeApplication();
        }
      }
    } catch (MqttException e) {
      if (isStopped) {
        log.warn("Stop signal received, connection closing", e);
      } else {
        log.error("Error connecting to client for user {}", username, e);
      }
    }
  }

  public void stopClients() {
    isStopped = true;
    log.info("Stop signal received, {} clients will start closing", clients.size());
  }

  private void closeConnection(IMqttClient client) {
    String clientId = client.getClientId();
    try {
      if (client.isConnected()) {
        log.info("Closing client {}", clientId);
        client.disconnect();
        log.info("Client {} closed successfully", clientId);
      }
      clients.remove(client);
    } catch (MqttException e) {
      log.error("Error while closing client {}", clientId, e);
    }
  }

  private void closeApplication() {
    ((ConfigurableApplicationContext) applicationContext).close();
  }
}
