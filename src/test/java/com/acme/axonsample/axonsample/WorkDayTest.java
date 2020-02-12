package com.acme.axonsample.axonsample;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WorkDayTest {
  private static final String SCHEDULE_1_ID = "5dca3df439881c002543876e";
  private static final String SCHEDULE_2_ID = "7dca3bf439881c002543836b";
  private static final String SCHEDULE_3_ID = "2dca3be439871c022543836a";
  private static final LocalDate DAY = LocalDate.of(2020, 1, 2);
  private static final WorkDayId WORK_DAY_ID = WorkDayId.of(DAY);

  private FixtureConfiguration<WorkDay> fixture;

  @BeforeEach
  public void setup() {
    fixture = new AggregateTestFixture<>(WorkDay.class);
  }

  @Test
  @DisplayName(
      "calculate work duration using multiple schedule times intersecting multiple workLogs")
  public void multipleScheduleTimesIntersectingMultipleWorkLogs() {
    Duration _1H = Duration.ofHours(1);
    Duration _35M = Duration.ofMinutes(35);
    Duration _41M = Duration.ofMinutes(41);
    Duration _45M = Duration.ofMinutes(45);
    List<Schedule> schedules = multipleSchedule2H45MOnJan2ndFixture();
    List<WorkLog> openWorkLogs = multipleOpenWorkLogsOnJan2ndFixture();
    List<WorkLog> closedWorkLogs = multipleWorkLogs3H43MOnJan2ndFixture();

    final Schedule schedule1 = schedules.get(0);
    final Schedule schedule2 = schedules.get(1);
    final Schedule schedule3 = schedules.get(2);

    fixture
        .given(WorkDayCreated.of(WORK_DAY_ID, DAY, schedule1))
        .andGiven(
            ScheduleSubmitted.of(
                WORK_DAY_ID, schedule2.getId(), schedule2.getBegin(), schedule2.getEnd()),
            // --
            StartTimeTracked.of(WORK_DAY_ID, openWorkLogs.get(0)),
            ScheduleWorkLogAdded.of(WORK_DAY_ID, SCHEDULE_1_ID, openWorkLogs.get(0)),
            ActualScheduleDurationCalculated.of(WORK_DAY_ID, SCHEDULE_1_ID, _1H),
            // --
            StopTimeTracked.of(WORK_DAY_ID, openWorkLogs.get(0), closedWorkLogs.get(0).getStop()),
            ScheduleWorkLogClosed.of(WORK_DAY_ID, SCHEDULE_1_ID),
            ActualScheduleDurationCalculated.of(WORK_DAY_ID, SCHEDULE_1_ID, _35M),
            // --
            StartTimeTracked.of(WORK_DAY_ID, openWorkLogs.get(1)),
            ScheduleWorkLogAdded.of(WORK_DAY_ID, SCHEDULE_1_ID, openWorkLogs.get(0)),
            ActualScheduleDurationCalculated.of(WORK_DAY_ID, SCHEDULE_1_ID, _41M),
            ScheduleWorkLogAdded.of(WORK_DAY_ID, SCHEDULE_2_ID, openWorkLogs.get(1)),
            ActualScheduleDurationCalculated.of(WORK_DAY_ID, SCHEDULE_2_ID, _1H),
            // --
            StartTimeTracked.of(WORK_DAY_ID, openWorkLogs.get(2)),
            // --
            ScheduleSubmitted.of(
                WORK_DAY_ID, schedule3.getId(), schedule3.getBegin(), schedule3.getEnd()),
            ScheduleWorkLogsAssociated.of(WORK_DAY_ID, schedule3.getId(), openWorkLogs),
            ActualScheduleDurationCalculated.of(WORK_DAY_ID, SCHEDULE_1_ID, _45M),
            // --
            StopTimeTracked.of(WORK_DAY_ID, openWorkLogs.get(1), closedWorkLogs.get(1).getStop()),
            ScheduleWorkLogClosed.of(WORK_DAY_ID, SCHEDULE_1_ID),
            ActualScheduleDurationCalculated.of(WORK_DAY_ID, SCHEDULE_1_ID, _41M),
            ScheduleWorkLogClosed.of(WORK_DAY_ID, SCHEDULE_2_ID),
            ActualScheduleDurationCalculated.of(WORK_DAY_ID, SCHEDULE_1_ID, _1H))
        .when(TrackStopTime.of(WORK_DAY_ID, closedWorkLogs.get(2).getStop()))
        .expectSuccessfulHandlerExecution()
        .expectEvents(
            StopTimeTracked.of(WORK_DAY_ID, openWorkLogs.get(2), closedWorkLogs.get(2).getStop()),
            ScheduleWorkLogClosed.of(WORK_DAY_ID, SCHEDULE_3_ID),
            ActualScheduleDurationCalculated.of(WORK_DAY_ID, SCHEDULE_3_ID, _45M));
  }

  private List<Schedule> multipleSchedule2H45MOnJan2ndFixture() {
    ZonedDateTime schedule1Begin = ZonedDateTime.of(2020, 1, 2, 10, 0, 0, 0, UTC);
    ZonedDateTime schedule1End = ZonedDateTime.of(2020, 1, 2, 11, 0, 0, 0, UTC);
    ZonedDateTime schedule2Begin = ZonedDateTime.of(2020, 1, 2, 12, 0, 0, 0, UTC);
    ZonedDateTime schedule2End = ZonedDateTime.of(2020, 1, 2, 13, 0, 0, 0, UTC);
    ZonedDateTime schedule3Begin = ZonedDateTime.of(2020, 1, 2, 14, 0, 0, 0, UTC);
    ZonedDateTime schedule3End = ZonedDateTime.of(2020, 1, 2, 14, 45, 0, 0, UTC);

    return asList(
        Schedule.of(SCHEDULE_1_ID, schedule1Begin, schedule1End),
        Schedule.of(SCHEDULE_2_ID, schedule2Begin, schedule2End),
        Schedule.of(SCHEDULE_3_ID, schedule3Begin, schedule3End));
  }

  private List<WorkLog> multipleWorkLogs3H43MOnJan2ndFixture() {
    ZonedDateTime workLog1Start = ZonedDateTime.of(2020, 1, 2, 9, 45, 0, 0, UTC);
    ZonedDateTime workLog1Stop = ZonedDateTime.of(2020, 1, 2, 10, 35, 0, 0, UTC);
    ZonedDateTime workLog2Start = ZonedDateTime.of(2020, 1, 2, 10, 54, 0, 0, UTC);
    ZonedDateTime workLog2Stop = ZonedDateTime.of(2020, 1, 2, 13, 0, 0, 0, UTC);
    ZonedDateTime workLog3Start = ZonedDateTime.of(2020, 1, 2, 13, 58, 0, 0, UTC);
    ZonedDateTime workLog3Stop = ZonedDateTime.of(2020, 1, 2, 14, 45, 0, 0, UTC);

    return asList(
        WorkLog.of(workLog1Start, workLog1Stop),
        WorkLog.of(workLog2Start, workLog2Stop),
        WorkLog.of(workLog3Start, workLog3Stop));
  }

  private List<WorkLog> multipleOpenWorkLogsOnJan2ndFixture() {
    ZonedDateTime workLog1Start = ZonedDateTime.of(2020, 1, 2, 9, 45, 0, 0, UTC);
    ZonedDateTime workLog2Start = ZonedDateTime.of(2020, 1, 2, 10, 54, 0, 0, UTC);
    ZonedDateTime workLog3Start = ZonedDateTime.of(2020, 1, 2, 13, 58, 0, 0, UTC);

    return asList(WorkLog.of(workLog1Start), WorkLog.of(workLog2Start), WorkLog.of(workLog3Start));
  }
}
