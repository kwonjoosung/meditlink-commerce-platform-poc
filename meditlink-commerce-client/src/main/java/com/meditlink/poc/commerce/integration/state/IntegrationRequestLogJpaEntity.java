package com.meditlink.poc.commerce.integration.state;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

// integration-layer 내부 처리 상태/요청 이력을 저장하는 최소 엔티티
@Entity
@Table(name = "integration_request_logs")
public class IntegrationRequestLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_type", nullable = false, length = 50)
    private String requestType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "payload_json")
    private String payloadJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IntegrationRequestLogJpaEntity() {
    }

    public IntegrationRequestLogJpaEntity(
            String requestType,
            String referenceId,
            String status,
            String payloadJson,
            Instant createdAt
    ) {
        this.requestType = requestType;
        this.referenceId = referenceId;
        this.status = status;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
    }
}
