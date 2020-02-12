package com.acme.axonsample.axonsample;

import java.time.ZonedDateTime;
import lombok.Value;

@Value(staticConstructor = "of")
public class StopTimeTracked {
  private final WorkDayId workDayId;
  private final WorkLog openWorkLog;
  private final ZonedDateTime stopTime;
}
