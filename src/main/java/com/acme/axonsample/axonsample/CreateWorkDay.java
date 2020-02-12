package com.acme.axonsample.axonsample;

import java.time.LocalDate;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value(staticConstructor = "of")
public class CreateWorkDay {
  @TargetAggregateIdentifier private WorkDayId workDayId;
  private LocalDate day;
  private Schedule schedule;
}
