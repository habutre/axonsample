package com.acme.axonsample.axonsample;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
public class WorkLog {
  @NonNull private final ZonedDateTime start;
  private ZonedDateTime stop;

  public static WorkLog of(ZonedDateTime start) {
    return new WorkLog(start, null);
  }

  public boolean isOpen() {
    return null == this.stop;
  }

  public void close(ZonedDateTime stopTime) {
    if (null != stopTime && this.getStart().isBefore(stopTime)) {
      this.stop = stopTime;
    }
  }
}
