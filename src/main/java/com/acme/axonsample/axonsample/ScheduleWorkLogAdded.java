package com.acme.axonsample.axonsample;

import lombok.Value;

@Value(staticConstructor = "of")
public class ScheduleWorkLogAdded {
  private final WorkDayId workDayId;
  private final String id;
  private final WorkLog workLog;
}
