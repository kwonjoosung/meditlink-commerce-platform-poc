-- 로컬 개발용 client 스키마 최종 형태 스냅샷
-- 운영/배포 스키마 진화는 Liquibase를 기준으로 관리한다.

CREATE TABLE IF NOT EXISTS client.integration_request_logs (
    id BIGSERIAL PRIMARY KEY,
    request_type VARCHAR(50) NOT NULL,
    reference_id VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    payload_json TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_integration_request_logs_type_created_at
    ON client.integration_request_logs(request_type, created_at);
