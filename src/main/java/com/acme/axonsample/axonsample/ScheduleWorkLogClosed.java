package com.acme.axonsample.axonsample;

import lombok.Value;

@Value(staticConstructor = "of")
public class ScheduleWorkLogClosed {
  private final WorkDayId workDayId;
  private final String id;
}
