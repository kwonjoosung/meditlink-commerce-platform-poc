-- 단일 PostgreSQL 데이터베이스 내 논리 분리
-- core/client가 서로 다른 스키마를 사용하도록 사전 생성

CREATE SCHEMA IF NOT EXISTS core;
CREATE SCHEMA IF NOT EXISTS client;
