package com.acme.axonsample.axonsample;

import java.time.ZonedDateTime;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value(staticConstructor = "of")
public class TrackStopTime {
  @TargetAggregateIdentifier private final WorkDayId workDayId;
  private final ZonedDateTime stopTime;
}
