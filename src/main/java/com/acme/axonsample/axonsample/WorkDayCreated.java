package com.acme.axonsample.axonsample;

import java.time.LocalDate;
import lombok.Value;

@Value(staticConstructor = "of")
public class WorkDayCreated {
  private WorkDayId workDayId;
  private LocalDate day;
  private Schedule schedule;
}
