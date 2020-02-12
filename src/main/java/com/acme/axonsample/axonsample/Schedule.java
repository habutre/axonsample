package com.acme.axonsample.axonsample;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.EntityId;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
public class Schedule {
  @EntityId @NonNull private final String id;
  private Duration workDuration;
  private ZonedDateTime begin;
  private ZonedDateTime end;
  private List<WorkLog> workLogs;

  public static Schedule of(String scheduleId, ZonedDateTime begin, ZonedDateTime end) {
    return Schedule.of(scheduleId, Duration.ZERO, begin, end, null);
  }

  @EventSourcingHandler
  public void on(ScheduleWorkLogAdded event) {
    this.addWorkLog(event.getWorkLog());
  }

  @EventSourcingHandler
  public void on(ScheduleWorkLogClosed event) {
    this.rebuildWorkLogs();
  }

  @EventSourcingHandler
  public void on(ScheduleWorkLogsAssociated event) {
    if (this.id.equals(event.getId())) {
      this.addWorkLogs(event.getWorkLogs());
    }
  }

  public boolean intersects(WorkLog workLog) {
    if (null == workLog || null == workLog.getStart()) {
      return false;
    }

    if (null == workLog.getStop()) {
      return workLog.getStart().isBefore(this.getEnd());
    }

    return workLog.getStart().isBefore(this.getEnd()) && workLog.getStop().isAfter(this.getBegin());
  }

  public Schedule rebuildWorkLogs() {
    if (this.getWorkLogs().isEmpty()) {
      return this;
    }

    this.workLogs = this.workLogs.stream().filter(this::intersects).collect(Collectors.toList());
    this.calculateWorkDuration();

    return this;
  }

  public Schedule addWorkLogs(List<WorkLog> workLogs) {
    if (null == workLogs) {
      return this;
    }

    if (this.getWorkLogs().isEmpty()) {
      this.workLogs = new ArrayList<>();
    }

    workLogs.forEach(this::addWorkLog);

    return this;
  }

  public Schedule addWorkLog(WorkLog workLog) {
    if (this.getWorkLogs().isEmpty()) {
      this.workLogs = new ArrayList<>();
    }

    if (this.intersects(workLog)) {
      if (!this.workLogs.contains(workLog)) this.workLogs.add(workLog);
    }

    this.calculateWorkDuration();

    return this;
  }

  public Optional<List<WorkLog>> getWorkLogs() {
    return Optional.ofNullable(this.workLogs);
  }

  private Schedule calculateWorkDuration() {
    if (this.getWorkLogs().isEmpty()) {
      this.workDuration = Duration.ZERO;
      return this;
    }

    this.workDuration =
        this.getWorkLogs().get().stream()
            .map(this::getIntersectionDuration)
            .reduce(Duration::plus)
            .orElse(Duration.ZERO);

    return this;
  }

  private Duration getIntersectionDuration(WorkLog workLog) {
    ZonedDateTime begin =
        this.getBegin().isAfter(workLog.getStart()) ? this.getBegin() : workLog.getStart();
    ZonedDateTime end =
        null != workLog.getStop() && this.getEnd().isAfter(workLog.getStop())
            ? workLog.getStop()
            : this.getEnd();
    Duration intersectionDuration = Duration.between(begin, end);

    return intersectionDuration.isNegative() ? Duration.ZERO : intersectionDuration;
  }
}
