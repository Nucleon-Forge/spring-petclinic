package org.springframework.samples.petclinic.vet;

import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Component
public class CacheWarmupScheduler {

	private final VetRepository vetRepository;

	public CacheWarmupScheduler(VetRepository vetRepository) {
		this.vetRepository = vetRepository;
	}

	@Scheduled(fixedRate = 10000)
	@Transactional(readOnly = true)
	public void warmUpVetsCache() {
		try {
			vetRepository.findAll();
		}
		catch (DataAccessException e) {
		}
	}

}
