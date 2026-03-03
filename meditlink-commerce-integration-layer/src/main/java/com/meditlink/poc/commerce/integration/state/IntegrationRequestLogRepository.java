package com.meditlink.poc.commerce.integration.state;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationRequestLogRepository extends JpaRepository<IntegrationRequestLogJpaEntity, Long> {
}
