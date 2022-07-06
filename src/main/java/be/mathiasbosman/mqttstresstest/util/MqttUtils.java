package be.mathiasbosman.mqttstresstest.util;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.util.StringUtils;

public final class MqttUtils {

  private MqttUtils() {
    // utils
  }

  public static MqttConnectOptions createConnectOptions(String username, String password) {
    MqttConnectOptions options = new MqttConnectOptions();
    options.setAutomaticReconnect(true);
    options.setCleanSession(true);
    options.setConnectionTimeout(10);
    options.setUserName(username);
    if (StringUtils.hasLength(password)) {
      options.setPassword(password.toCharArray());
    }
    return options;
  }

  public static MqttConnectOptions createConnectOptions(String accessToken) {
    return createConnectOptions(accessToken, null);
  }
}
