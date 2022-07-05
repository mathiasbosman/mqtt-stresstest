package be.mathiasbosman.mqttstresstest.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttConfig {

  /**
   * Complete base url of the server including the protocol and port
   */
  private String serverUrl;

  /**
   * MQTT QOS level (0, 1 or 2).
   * Make sure the broker supports the level
   */
  private int qosLevel;

  /**
   * Topic to publish too
   */
  private String topic;

  /**
   * This flag indicates to the broker that it should retain this message
   * until consumed by a subscriber.
   */
  private boolean retainMessages = true;

}
