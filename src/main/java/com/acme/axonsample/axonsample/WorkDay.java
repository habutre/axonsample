package com.acme.axonsample.axonsample;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Data;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;

@Data
@Aggregate
public class WorkDay {

  @AggregateIdentifier private WorkDayId id;
  private LocalDate day;
  @AggregateMember private Set<Schedule> schedules;
  private List<WorkLog> workLogs;

  public WorkDay() {}

  @CommandHandler
  public WorkDay(CreateWorkDay command) {
    WorkDayCreated workDayCreated =
        WorkDayCreated.of(command.getWorkDayId(), command.getDay(), command.getSchedule());

    AggregateLifecycle.apply(workDayCreated);
  }

  @CommandHandler
  public void apply(TrackStartTime command) {
    WorkLog openWorkLog = WorkLog.of(command.getStartTime());
    AggregateLifecycle.apply(StartTimeTracked.of(command.getWorkDayId(), openWorkLog));
    applyScheduleEvents(command, openWorkLog);
  }

  @CommandHandler
  public void apply(TrackStopTime command) {
    final WorkLog openWorkLog = findNearestOpenWorkLog(command.getStopTime());
    AggregateLifecycle.apply(
        StopTimeTracked.of(command.getWorkDayId(), openWorkLog, command.getStopTime()));
    applyScheduleEvents(command, openWorkLog);
  }

  @CommandHandler
  public void apply(SubmitSchedule command) {

    AggregateLifecycle.apply(
        ScheduleSubmitted.of(
            command.getWorkDayId(), command.getId(), command.getStart(), command.getEnd()));

    findScheduleById(command.getId())
        .ifPresent(
            schedule -> {
              AggregateLifecycle.apply(
                  ScheduleWorkLogsAssociated.of(
                      command.getWorkDayId(), schedule.getId(), this.getWorkLogs()));

              AggregateLifecycle.apply(
                  ActualScheduleDurationCalculated.of(
                      command.getWorkDayId(), schedule.getId(), schedule.getWorkDuration()));
            });
  }

  @EventSourcingHandler
  public void on(WorkDayCreated event) {
    this.id = event.getWorkDayId();
    this.day = event.getDay();
    this.addSchedule(event.getSchedule());
  }

  @EventSourcingHandler
  public void on(StartTimeTracked event) {
    this.addWorkLog(event.getOpenWorkLog());
  }

  @EventSourcingHandler
  public void on(StopTimeTracked event) {
    event.getOpenWorkLog().close(event.getStopTime());

    this.rebuildScheduleWorkLogs();
  }

  @EventSourcingHandler
  public void on(ScheduleSubmitted event) {
    Schedule schedule = Schedule.of(event.getId(), event.getStart(), event.getEnd());
    this.addSchedule(schedule);
  }

  private void addSchedule(Schedule schedule) {
    if (null == schedule) {
      return;
    }

    if (null == this.schedules) {
      this.schedules = new HashSet<>();
    }

    if (!this.getSchedules().contains(schedule)) {
      this.schedules.add(schedule);
    }
  }

  private void addWorkLog(WorkLog workLog) {
    if (null == workLog) {
      return;
    }

    if (null == this.workLogs) {
      this.workLogs = new ArrayList<>();
    }

    if (!this.getWorkLogs().contains(workLog)) {
      this.workLogs.add(workLog);
    }
  }

  private void rebuildScheduleWorkLogs() {
    if (null == this.getSchedules()) {
      return;
    }

    this.getSchedules().forEach(Schedule::rebuildWorkLogs);
  }

  private WorkLog findNearestOpenWorkLog(ZonedDateTime stopTime) {
    if (null == this.getWorkLogs() || this.getWorkLogs().isEmpty()) {
      throw new RuntimeException("Nearest not found");
    }

    return this.getWorkLogs().stream()
        .filter(WorkLog::isOpen)
        .filter(workLog -> workLog.getStart().isBefore(stopTime))
        .min(Comparator.comparing(WorkLog::getStart))
        .orElseThrow(() -> new RuntimeException("Nearest not found"));
  }

  private void applyScheduleEvents(TrackStartTime command, WorkLog openWorkLog) {
    if (null == this.getSchedules() || this.getSchedules().isEmpty()) {
      return;
    }

    this.getSchedules().stream()
        .filter(schedule -> schedule.intersects(openWorkLog))
        .forEach(
            schedule -> {
              AggregateLifecycle.apply(
                  ScheduleWorkLogAdded.of(command.getWorkDayId(), schedule.getId(), openWorkLog));

              AggregateLifecycle.apply(
                  ActualScheduleDurationCalculated.of(
                      command.getWorkDayId(), schedule.getId(), schedule.getWorkDuration()));
            });
  }

  private void applyScheduleEvents(TrackStopTime command, WorkLog openWorkLog) {
    if (null == this.getSchedules() || this.getSchedules().isEmpty()) {
      return;
    }

    this.getSchedules().stream()
        .filter(schedule -> schedule.intersects(openWorkLog))
        .forEach(
            schedule -> {
              AggregateLifecycle.apply(
                  ScheduleWorkLogClosed.of(command.getWorkDayId(), schedule.getId()));

              AggregateLifecycle.apply(
                  ActualScheduleDurationCalculated.of(
                      command.getWorkDayId(), schedule.getId(), schedule.getWorkDuration()));
            });
  }

  private Optional<Schedule> findScheduleById(String scheduleId) {
    if (null == this.getSchedules()) {
      return Optional.empty();
    }

    return this.getSchedules().stream()
        .filter(schedule -> schedule.getId().equals(scheduleId))
        .findFirst();
  }
}
