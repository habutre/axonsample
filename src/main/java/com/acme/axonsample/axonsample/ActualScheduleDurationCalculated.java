package com.acme.axonsample.axonsample;

import java.time.Duration;
import lombok.Value;

@Value(staticConstructor = "of")
public class ActualScheduleDurationCalculated {
  private WorkDayId workDayId;
  private String id;
  private Duration workDuration;
}
