package be.mathiasbosman.mqttstresstest.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Getter
@Setter
@EnableAsync
@Configuration
@ConfigurationProperties(prefix = "test.config")
public class StressTestConfiguration {

  /**
   * Amount of data points to generate for each message
   */
  private int amountOfDataPoints = 100;
  /**
   * Whether to create an extra data point with the creation timestamp
   */
  private boolean includeCreationTimestamp = true;
  /**
   * Amount of clients to simulate (this is the amount of async threads that will be started)
   */
  private int amountOfClients = 100;
  /**
   * Delay in ms between messages per client
   */
  private int messageDelay = 100;
  /**
   * Device prefix that will be appended with an index running from 1 to the amount of clients
   */
  private String devicePrefix = "TestDevice_";
}
