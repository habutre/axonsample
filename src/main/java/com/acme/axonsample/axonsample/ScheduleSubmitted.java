package com.acme.axonsample.axonsample;

import java.time.ZonedDateTime;
import lombok.Value;

@Value(staticConstructor = "of")
public class ScheduleSubmitted {
  private final WorkDayId workDayId;
  private final String id;
  private final ZonedDateTime start;
  private final ZonedDateTime end;
}
