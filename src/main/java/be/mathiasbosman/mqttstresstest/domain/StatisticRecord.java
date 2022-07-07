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
    StringBuilder toString = new StringBuilder(
        "client created at " + created + " (" + epochMilliStart + ") "
            + "published " + amountOfPublishedMessages + " messages ");

    if (latestPublish != null) {
      long epochMilliEnd = latestPublish.atZone(zoneId).toInstant().toEpochMilli();
      toString.append("latest at ").append(latestPublish)
          .append(" (").append(epochMilliEnd).append(")");
    }

    return toString.toString();
  }
}
