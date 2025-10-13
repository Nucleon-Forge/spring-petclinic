package org.springframework.samples.petclinic.scheduled;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.util.Map;

@Component
@RestControllerEndpoint(id = "scheduledtasks-control")
public class ScheduledTaskControlEndpoint {

	private final ManagedScheduledTaskRegistry registry;

	public ScheduledTaskControlEndpoint(ManagedScheduledTaskRegistry registry) {
		this.registry = registry;
	}

	@PostMapping("/enable")
	public Map<String, ? extends Serializable> enableTask(@RequestBody TaskControlRequest req) {
		boolean runImmediately = Boolean.TRUE.equals(req.force());
		String target = req.target();

		return registry.find(target)
			.map(task -> {
				task.enable(runImmediately);
				return Map.of(
					"target", target,
					"enabled", true,
					"force", runImmediately,
					"status", "started"
				);
			})
			.orElse(Map.of("error", "Task not found", "target", target));
	}

	@PostMapping("/disable")
	public Map<String, ? extends Serializable> disableTask(@RequestBody TaskControlRequest req) {
		boolean forceStop = Boolean.TRUE.equals(req.force());
		String target = req.target();

		return registry.find(target)
			.map(task -> {
				task.disable(forceStop);
				return Map.of(
					"target", target,
					"enabled", false,
					"force", forceStop,
					"status", "stopped"
				);
			})
			.orElse(Map.of("error", "Task not found", "target", target));
	}
}
