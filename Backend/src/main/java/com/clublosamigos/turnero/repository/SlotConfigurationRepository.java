package com.clublosamigos.turnero.repository;

import com.clublosamigos.turnero.model.SlotConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlotConfigurationRepository extends JpaRepository<SlotConfiguration, Long> {
    List<SlotConfiguration> findByIsDeletedFalse();
    Optional<SlotConfiguration> findByIdAndIsDeletedFalse(Long id);
}
