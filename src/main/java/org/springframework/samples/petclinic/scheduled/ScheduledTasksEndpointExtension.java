package org.springframework.samples.petclinic.scheduled;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EndpointWebExtension(endpoint = ScheduledTasksEndpoint.class)
public class ScheduledTasksEndpointExtension {

    private final ScheduledTasksEndpoint delegate;
    private final ManagedScheduledTaskRegistry registry;

    public ScheduledTasksEndpointExtension(ScheduledTasksEndpoint delegate, ManagedScheduledTaskRegistry registry) {
        this.delegate = delegate;
        this.registry = registry;
    }

    @ReadOperation
    public ExtendedScheduledTasksDescriptor scheduledTasks() {
        ScheduledTasksEndpoint.ScheduledTasksDescriptor scheduledTasksDescriptor = delegate.scheduledTasks();

        return new ExtendedScheduledTasksDescriptor(
            enrich(scheduledTasksDescriptor.getCron()),
            enrich(scheduledTasksDescriptor.getFixedDelay()),
            enrich(scheduledTasksDescriptor.getFixedRate()),
            enrich(scheduledTasksDescriptor.getCustom())
        );
    }

    private List<ExtendedTaskDescriptor> enrich(List<? extends ScheduledTasksEndpoint.TaskDescriptor> tasks) {
        return tasks.stream()
            .map(td -> new ExtendedTaskDescriptor(td, resolveEnabled(td)))
            .toList();
    }

    private boolean resolveEnabled(ScheduledTasksEndpoint.TaskDescriptor taskDescriptor) {
        String target = taskDescriptor.getRunnable().getTarget();
        return registry.find(target)
            .map(ManagedScheduledTask::isEnabled)
            .orElse(true);
    }

    public record ExtendedScheduledTasksDescriptor(List<ExtendedTaskDescriptor> cron,
                                                   List<ExtendedTaskDescriptor> fixedDelay,
                                                   List<ExtendedTaskDescriptor> fixedRate,
                                                   List<ExtendedTaskDescriptor> custom) {
    }

	public record ExtendedTaskDescriptor(
		ScheduledTasksEndpoint.TaskDescriptor delegate,
		boolean enabled
	) {}
}
