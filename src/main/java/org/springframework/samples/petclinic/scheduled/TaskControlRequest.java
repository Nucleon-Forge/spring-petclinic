package org.springframework.samples.petclinic.scheduled;

import org.jspecify.annotations.Nullable;

import java.io.Serializable;

public record TaskControlRequest(String target, @Nullable Boolean force) implements Serializable {}
