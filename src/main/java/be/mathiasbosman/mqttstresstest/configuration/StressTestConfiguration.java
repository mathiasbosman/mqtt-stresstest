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
   * Minimum index of a client. Threads will be generated for clients with indexes ranging from the
   * minimum index to the maximum.
   */
  private int minimumClientIndex = 1;

  /**
   * Maximum index of a client. Threads will be generated for clients with indexes ranging from the
   * minimum index to the maximum.
   */
  private int maximumClientIndex = 100;

  /**
   * Delay in ms between messages per client
   */
  private int messageDelay = 100;

  /**
   * Device prefix that will be appended with an index running from 1 to the amount of clients
   */
  private String tokenPrefix = "TestDevice_";

  /**
   * Amount of seconds that the application has to run
   */
  private int ttl = 60;

  /**
   * Indicates if a connection should be retained after publishing
   */
  private boolean retainConnection = true;
}
