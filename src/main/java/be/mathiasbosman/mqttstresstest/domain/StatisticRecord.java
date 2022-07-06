package be.mathiasbosman.mqttstresstest.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StatisticRecord {

  private static final ZoneId zoneId = ZoneId.systemDefault();

  private final LocalDateTime created;
  private LocalDateTime latestPublish;
  private int amountOfPublishedMessages = 0;

  public void publish(LocalDateTime publishTime) {
    this.amountOfPublishedMessages++;
    this.latestPublish = publishTime;
  }

  @Override
  public String toString() {
    long epochMilliStart = created.atZone(zoneId).toInstant().toEpochMilli();
    long epochMilliEnd = latestPublish.atZone(zoneId).toInstant().toEpochMilli();
    return "client created at " + created + " (" + epochMilliStart + ") "
        + "published " + amountOfPublishedMessages + " messages "
        + "latest at " + latestPublish + " (" + epochMilliEnd + ")";
  }
}
