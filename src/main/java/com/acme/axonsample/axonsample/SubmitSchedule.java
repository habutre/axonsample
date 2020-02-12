package com.acme.axonsample.axonsample;

import java.time.ZonedDateTime;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value(staticConstructor = "of")
public class SubmitSchedule {
  @TargetAggregateIdentifier private final WorkDayId workDayId;
  private final String id;
  private final ZonedDateTime start;
  private final ZonedDateTime end;
}
