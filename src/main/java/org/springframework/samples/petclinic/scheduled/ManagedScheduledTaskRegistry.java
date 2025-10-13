package org.springframework.samples.petclinic.scheduled;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ManagedScheduledTaskRegistry implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger log = LoggerFactory.getLogger(ManagedScheduledTaskRegistry.class);
	private final Map<String, ManagedScheduledTask> tasks = new ConcurrentHashMap<>();
	private final ScheduledAnnotationBeanPostProcessor processor;
	private final TaskScheduler scheduler;

	public ManagedScheduledTaskRegistry(
		ScheduledAnnotationBeanPostProcessor processor,
		TaskScheduler scheduler
	) {
		this.processor = processor;
		this.scheduler = scheduler;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		Set<ScheduledTask> allTasks = processor.getScheduledTasks();
		for (ScheduledTask task : allTasks) {
			register(task);
		}
		log.info("Registered {} managed scheduled tasks", tasks.size());

		logTasksDetails(allTasks);
	}

	private void logTasksDetails(Set<ScheduledTask> allTasks) {
		log.debug("Total tasks found in processor: {}", allTasks.size());
		for (ScheduledTask task : allTasks) {
			String id = resolveId(task);
			log.debug("Task: {} - Runnable: {}", id, task.getTask().getRunnable().getClass().getName());
		}
	}

	private void register(ScheduledTask task) {
		String id = resolveId(task);
		if (tasks.containsKey(id)) {
			return;
		}
		ManagedScheduledTask managed = new ManagedScheduledTask(id, task, scheduler);
		tasks.put(id, managed);
		log.info("→ Managed task registered: {}", id);
	}

	private String resolveId(ScheduledTask task) {
		Task t = task.getTask();
		Runnable r = t.getRunnable();
		return r.toString();
	}

	public Collection<ManagedScheduledTask> all() {
		return tasks.values();
	}

	public Optional<ManagedScheduledTask> find(String id) {
		return Optional.ofNullable(tasks.get(id));
	}
}
