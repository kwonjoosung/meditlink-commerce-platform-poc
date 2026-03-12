-- 로컬 개발용 core 스키마 최종 형태 스냅샷
-- 운영/배포 스키마 진화는 Liquibase를 기준으로 관리한다.
-- NEW_SPEC 기반 Merchandising 도메인 리팩토링 반영

-- ============================================================
-- Product BC
-- ============================================================

CREATE TABLE IF NOT EXISTS core.product_groups (
    product_group_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug             TEXT UNIQUE,
    name             TEXT NOT NULL,
    description      TEXT,
    type             TEXT NOT NULL DEFAULT 'PLAN_FAMILY',
    status           TEXT NOT NULL DEFAULT 'DRAFT',
    sort_order       INT NOT NULL DEFAULT 0,
    display_config   JSONB NOT NULL DEFAULT '{}',
    visibility_rules JSONB NOT NULL DEFAULT '{}',
    tags             TEXT[] NOT NULL DEFAULT '{}',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS core.products (
    product_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_group_id UUID REFERENCES core.product_groups(product_group_id),
    external_id      TEXT NOT NULL UNIQUE,
    name             TEXT NOT NULL,
    display_name     TEXT,
    description      TEXT,
    item_type        TEXT NOT NULL,
    status           TEXT NOT NULL DEFAULT 'ACTIVE',
    tier_order       INT NOT NULL DEFAULT 0,
    visibility       TEXT NOT NULL DEFAULT 'PUBLIC',
    display_config   JSONB NOT NULL DEFAULT '{}',
    visibility_rules JSONB NOT NULL DEFAULT '{}',
    compatibility    JSONB NOT NULL DEFAULT '{}',
    tags             TEXT[] NOT NULL DEFAULT '{}',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS core.product_features (
    product_id           UUID NOT NULL REFERENCES core.products(product_id) ON DELETE CASCADE,
    feature_code         TEXT NOT NULL,
    quota                BIGINT,
    display_label        TEXT,
    is_highlighted       BOOLEAN NOT NULL DEFAULT false,
    stripe_entitlement_id TEXT,
    PRIMARY KEY (product_id, feature_code)
);

CREATE TABLE IF NOT EXISTS core.prices (
    price_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id     UUID NOT NULL REFERENCES core.products(product_id) ON DELETE CASCADE,
    external_id    TEXT NOT NULL UNIQUE,
    currency       TEXT NOT NULL,
    amount         BIGINT NOT NULL,
    billing_period TEXT NOT NULL,
    is_default     BOOLEAN NOT NULL DEFAULT false,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
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

-- ============================================================
-- Feature BC
-- ============================================================

CREATE TABLE IF NOT EXISTS core.features (
    feature_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feature_code TEXT NOT NULL UNIQUE,
    name         TEXT NOT NULL,
    description  TEXT,
    type         TEXT NOT NULL DEFAULT 'BOOLEAN',
    status       TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_features_code ON core.features(feature_code);
CREATE INDEX IF NOT EXISTS idx_features_status ON core.features(status);

-- ============================================================
-- Coupon BC
-- ============================================================

CREATE TABLE IF NOT EXISTS core.promotion_policies (
    policy_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                   TEXT NOT NULL,
    description            TEXT,
    discount_type          TEXT NOT NULL,
    discount_value         BIGINT NOT NULL,
    eligibility            JSONB NOT NULL DEFAULT '{}',
    applicable_product_ids JSONB NOT NULL DEFAULT '[]',
    max_redemptions        INT,
    current_redemptions    INT NOT NULL DEFAULT 0,
    valid_from             TIMESTAMPTZ NOT NULL,
    valid_until            TIMESTAMPTZ,
    status                 TEXT NOT NULL DEFAULT 'ACTIVE',
    stripe_coupon_id       TEXT,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_promotion_policies_status ON core.promotion_policies(status);

CREATE TABLE IF NOT EXISTS core.coupons (
    coupon_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_id        UUID NOT NULL REFERENCES core.promotion_policies(policy_id),
    code             TEXT NOT NULL UNIQUE,
    customer_id      TEXT,
    discount_type    TEXT NOT NULL,
    discount_value   BIGINT NOT NULL,
    status           TEXT NOT NULL DEFAULT 'ACTIVE',
    stripe_coupon_id TEXT,
    expires_at       TIMESTAMPTZ,
    redeemed_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_coupons_code ON core.coupons(code);
CREATE INDEX IF NOT EXISTS idx_coupons_policy ON core.coupons(policy_id);
CREATE INDEX IF NOT EXISTS idx_coupons_status ON core.coupons(status);
