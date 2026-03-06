-- 로컬 개발용 core 스키마 최종 형태 스냅샷
-- 운영/배포 스키마 진화는 Liquibase를 기준으로 관리한다.
-- Catalog Aggregate → ProductGroup으로 리네이밍 (IMPLEMENTATION-PLAN.md 참조)

CREATE TABLE IF NOT EXISTS core.product_groups (
    product_group_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug             TEXT UNIQUE,
    name             TEXT NOT NULL,
    description      TEXT,
    status           TEXT NOT NULL DEFAULT 'DRAFT',
    display_order    INT NOT NULL DEFAULT 0,
    condition        JSONB,
    attributes       JSONB NOT NULL DEFAULT '{}',
    metadata         JSONB NOT NULL DEFAULT '{}',
    tags             TEXT[] NOT NULL DEFAULT '{}',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS core.products (
    product_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_group_id UUID NOT NULL REFERENCES core.product_groups(product_group_id),
    external_id      TEXT NOT NULL UNIQUE,
    name             TEXT NOT NULL,
    description      TEXT,
    type             TEXT NOT NULL,
    billing_type     TEXT NOT NULL,
    status           TEXT NOT NULL DEFAULT 'ACTIVE',
    display_order    INT NOT NULL DEFAULT 0,
    condition        JSONB,
    attributes       JSONB NOT NULL DEFAULT '{}',
    metadata         JSONB NOT NULL DEFAULT '{}',
    tags             TEXT[] NOT NULL DEFAULT '{}',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS core.product_features (
    product_id    UUID NOT NULL REFERENCES core.products(product_id) ON DELETE CASCADE,
    feature_code  TEXT NOT NULL,
    quota         BIGINT,
    attributes    JSONB NOT NULL DEFAULT '{}',
    PRIMARY KEY (product_id, feature_code)
);

CREATE TABLE IF NOT EXISTS core.prices (
    price_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id       UUID NOT NULL REFERENCES core.products(product_id) ON DELETE CASCADE,
    external_id      TEXT NOT NULL UNIQUE,
    currency         TEXT NOT NULL,
    amount           BIGINT NOT NULL,
    billing_interval TEXT,
    interval_count   INT DEFAULT 1,
    is_default       BOOLEAN NOT NULL DEFAULT false,
    condition        JSONB,
    attributes       JSONB NOT NULL DEFAULT '{}',
    metadata         JSONB NOT NULL DEFAULT '{}',
    tags             TEXT[] NOT NULL DEFAULT '{}',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 통화별 default는 하나만
CREATE UNIQUE INDEX IF NOT EXISTS idx_prices_default
    ON core.prices(product_id, currency) WHERE is_default = true;

-- 조회 인덱스
CREATE INDEX IF NOT EXISTS idx_products_product_group ON core.products(product_group_id);
CREATE INDEX IF NOT EXISTS idx_products_status ON core.products(product_group_id, status);
CREATE INDEX IF NOT EXISTS idx_product_features_product ON core.product_features(product_id);
CREATE INDEX IF NOT EXISTS idx_product_features_feature ON core.product_features(feature_code);
CREATE INDEX IF NOT EXISTS idx_prices_product ON core.prices(product_id);

-- Tags GIN
CREATE INDEX IF NOT EXISTS idx_product_groups_tags ON core.product_groups USING GIN (tags);
CREATE INDEX IF NOT EXISTS idx_products_tags ON core.products USING GIN (tags);
CREATE INDEX IF NOT EXISTS idx_prices_tags ON core.prices USING GIN (tags);

-- Attributes GIN
CREATE INDEX IF NOT EXISTS idx_products_attributes ON core.products USING GIN (attributes);
CREATE INDEX IF NOT EXISTS idx_prices_attributes ON core.prices USING GIN (attributes);
