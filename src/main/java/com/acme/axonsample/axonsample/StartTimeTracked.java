package com.acme.axonsample.axonsample;

import lombok.Value;

@Value(staticConstructor = "of")
public class StartTimeTracked {
  private final WorkDayId workDayId;
  private final WorkLog openWorkLog;
}
