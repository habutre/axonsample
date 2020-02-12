package com.acme.axonsample.axonsample;

import java.util.List;
import lombok.Value;

@Value(staticConstructor = "of")
public class ScheduleWorkLogsAssociated {
  private final WorkDayId workDayId;
  private final String id;
  private final List<WorkLog> workLogs;
}
