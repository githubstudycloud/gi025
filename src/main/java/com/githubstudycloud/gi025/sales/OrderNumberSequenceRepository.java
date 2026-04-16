package com.githubstudycloud.gi025.sales;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface OrderNumberSequenceRepository extends JpaRepository<OrderNumberSequence, String> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<OrderNumberSequence> findBySequenceName(String sequenceName);
}
