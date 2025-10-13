package org.springframework.samples.petclinic.scheduled;

import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.config.TriggerTask;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ManagedScheduledTask {

	private final String id;
	private final ScheduledTask scheduledTask;
	private final TaskScheduler taskScheduler;
	private volatile boolean enabled = true;

	private final Runnable runnable;
	private final Trigger trigger;
	private static final Field SCHEDULED_TASK_FUTURE_FIELD;

	static {
		try {
			SCHEDULED_TASK_FUTURE_FIELD = ScheduledTask.class.getDeclaredField("future");
			SCHEDULED_TASK_FUTURE_FIELD.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new ExceptionInInitializerError(e.getMessage());
		}
	}

	public ManagedScheduledTask(String id, ScheduledTask scheduledTask, TaskScheduler taskScheduler) {
		this.id = id;
		this.scheduledTask = scheduledTask;
		this.taskScheduler = taskScheduler;

		Task t = scheduledTask.getTask();
		this.runnable = t.getRunnable();
		if (t instanceof CronTask cronTask) {
			this.trigger = cronTask.getTrigger();
		} else if (t instanceof TriggerTask triggerTask) {
			this.trigger = triggerTask.getTrigger();
		} else {
			this.trigger = null;
		}
	}

	public String getId() {
		return id;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void enable(boolean force) {
		if (!enabled) {
			this.enabled = true;
			reschedule(false);
		} else if (force) {
			this.enabled = true;
			reschedule(true);
		}
	}

	public void disable(boolean force) {
		this.enabled = false;
		cancelCurrentFuture(force);
	}

	private ScheduledFuture<?> getFuture() {
		try {
			return (ScheduledFuture<?>) SCHEDULED_TASK_FUTURE_FIELD.get(scheduledTask);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Failed to get 'future' from ScheduledTask", e);
		}
	}

	private void setFuture(ScheduledFuture<?> future) {
		try {
			SCHEDULED_TASK_FUTURE_FIELD.set(scheduledTask, future);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Failed to set 'future' in ScheduledTask", e);
		}
	}

	private void cancelCurrentFuture(boolean mayInterruptIfRunning) {
		ScheduledFuture<?> future = getFuture();
		if (future != null) {
			future.cancel(mayInterruptIfRunning);
		}
	}

	private void reschedule(boolean runImmediately) {
		cancelCurrentFuture(false);

		ScheduledFuture<?> newFuture;
		if (trigger != null) {
			newFuture = taskScheduler.schedule(runnable, trigger);
		} else if (scheduledTask.getTask() instanceof FixedRateTask frt) {
			newFuture = taskScheduler.scheduleAtFixedRate(
				runnable,
				runImmediately ? Instant.now() : Instant.now().plus(frt.getInitialDelayDuration()),
				frt.getIntervalDuration()
			);
		} else if (scheduledTask.getTask() instanceof FixedDelayTask fdt) {
			newFuture = taskScheduler.scheduleWithFixedDelay(
				runnable,
				runImmediately ? Instant.now() : Instant.now().plus(fdt.getInitialDelayDuration()),
				fdt.getIntervalDuration()
			);
		} else {
			throw new IllegalStateException("Unsupported task type: " + scheduledTask.getTask().getClass().getName());
		}

		setFuture(newFuture);
	}
}

