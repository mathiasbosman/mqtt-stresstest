package be.mathiasbosman.mqttstresstest.domain;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.PreDestroy;
import lombok.NonNull;
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
  private boolean isStopping;


  @Async
  public void createClientAndStartPublishing(@NonNull String serverUrl,
      @NonNull MqttConnectOptions options, @NonNull Consumer<IMqttClient> publisher,
      int delay, boolean retainConnection) {
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
        String clientId = client.getClientId();
        log.debug("Connecting client {}", clientId);
        clients.add(client);
        connectedUsers.add(username);
        log.info("Start of publishing messages for client {}", clientId);
        while (!isStopped) {
          consumePublisher(client, options, publisher, retainConnection);
          delayNexPublication(delay, clientId);
        }
        closeConnection(client);
      }
    } catch (MqttException e) {
      if (isStopped) {
        log.warn("Stop signal received during publish", e);
      } else {
        log.error("Error connecting to client for user {}", username, e);
      }
    }
  }

  private void delayNexPublication(int delay, String clientId) {
    try {
      log.trace("Delaying publication for {} ms", delay);
      TimeUnit.MILLISECONDS.sleep(delay);
    } catch (InterruptedException e) {
      log.debug("Thread interrupted for client {}", clientId);
    }
  }

  private void consumePublisher(IMqttClient client, MqttConnectOptions options,
      Consumer<IMqttClient> publisher, boolean retainConnection) throws MqttException {
    if (isStopped) {
      return;
    }
    if (!retainConnection || !client.isConnected()) {
      client.connect(options);
    }
    publisher.accept(client);
    if (!retainConnection) {
      client.disconnect();
    }
  }

  public void stopClients() {
    if (!isStopped) {
      isStopped = true;
      log.info("Stop signal received, {} clients will start closing", clients.size());
    }
  }

  @PreDestroy
  public void preDestroy() {
    isStopping = true;
    stopClients();
  }

  private void closeConnection(@NonNull IMqttClient client) {
    String clientId = client.getClientId();
    try {
      if (client.isConnected()) {
        log.info("Closing client {}", clientId);
        client.disconnectForcibly();
        log.info("Client {} closed successfully", clientId);
      }
      clients.remove(client);
    } catch (MqttException e) {
      log.error("Error while closing client {}", clientId, e);
    }

    if (clients.isEmpty()) {
      closeApplication();
    }
  }

  private void closeApplication() {
    if (!isStopping) {
      ((ConfigurableApplicationContext) applicationContext).close();
    }
  }
}
