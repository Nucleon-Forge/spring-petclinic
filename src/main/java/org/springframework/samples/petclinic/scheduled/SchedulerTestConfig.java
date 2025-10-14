package org.springframework.samples.petclinic.scheduled;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Instant;

@Configuration
@EnableScheduling
public class SchedulerTestConfig implements SchedulingConfigurer {

	private static final Logger log = LoggerFactory.getLogger(SchedulerTestConfig.class);

	/**
	 * CRON
	 */
	@Scheduled(cron = "*/2 * * * * *")
	public void alive() {
		log.info("alive task");
	}

	/**
	 * CRON
	 */
	@Scheduled(cron = "*/2 * * * * *")
	public void cronTask() {
		log.info("Running CRON task");
	}

	/**
	 * fixedDelay
	 */
	@Scheduled(fixedDelay = 2000)
	public void fixedDelayTask() throws InterruptedException {
		log.info("Running FIXED_DELAY task");
		Thread.sleep(50);
	}

	/**
	 * fixedRate
	 */
	@Scheduled(fixedRate = 2000, initialDelay = 100)
	public void fixedRateTask() {
		log.info("Running FIXED_RATE task");
	}

	/**
	 * Custom Trigger
	 */
	@Override
	public void configureTasks(ScheduledTaskRegistrar registrar) {
		registrar.addTriggerTask(new CustomTestTask(), new CustomTestTrigger());
	}

	static class CustomTestTask implements Runnable {
		@Override
		public void run() {
			log.info("Running CUSTOM task");
		}

		@Override
		public String toString() {
			return "customTestTask";
		}
	}

	static class CustomTestTrigger implements Trigger {
		@Override
		@Nullable
		public Instant nextExecution(@NonNull TriggerContext triggerContext) {
			return Instant.now().plusSeconds(2);
		}

		@Override
		public String toString() {
			return "customTrigger";
		}
	}
}
