package com.acme.axonsample.axonsample;

import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode
public class WorkDayId {
  private static final String ID_PREFIX = "workDay-for-";
  private static final String ID_TEMPLATE = ID_PREFIX.concat("%s");
  @NonNull private final LocalDate day;

  public WorkDayId(@NonNull LocalDate day) {
    this.day = day;
  }

  public static WorkDayId of(LocalDate day) {
    return new WorkDayId(day);
  }

  @Override
  public String toString() {
    return String.format(ID_TEMPLATE, this.day);
  }
}
