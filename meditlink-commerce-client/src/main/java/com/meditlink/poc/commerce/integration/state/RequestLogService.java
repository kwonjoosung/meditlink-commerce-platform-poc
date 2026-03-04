package com.meditlink.poc.commerce.integration.state;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestLogService {

    private final IntegrationRequestLogRepository integrationRequestLogRepository;

    public RequestLogService(IntegrationRequestLogRepository integrationRequestLogRepository) {
        this.integrationRequestLogRepository = integrationRequestLogRepository;
    }

    @Transactional
    public void log(String requestType, String referenceId, String status, String payloadJson) {
        IntegrationRequestLogJpaEntity logEntity = new IntegrationRequestLogJpaEntity(
                requestType,
                referenceId,
                status,
                payloadJson,
                Instant.now()
        );
        integrationRequestLogRepository.save(logEntity);
    }
}
