package be.mathiasbosman.mqttstresstest.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StatisticRecord {

  private final LocalDateTime created;
  private LocalDateTime latestPublish;
  private int amountOfPublishedMessages = 0;

  public void incrementPublishedMessages() {
    this.amountOfPublishedMessages++;
    this.latestPublish = LocalDateTime.now();
  }

  @Override
  public String toString() {
    long epochMilli = created.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    return "created at " + created + "(" + epochMilli + ") "
        + "published " + amountOfPublishedMessages + " messages "
        + "ended at " + latestPublish;
  }
}
