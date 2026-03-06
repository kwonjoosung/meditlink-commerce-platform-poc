-- 로컬 학습/데모용 더미 데이터
-- init 스크립트는 볼륨 초기화 시점에 1회 실행된다.
-- PRODUCT-BC.md 예시 데이터 기반 (Catalog → ProductGroup 리네이밍 적용)

-- ProductGroup (설계 문서의 Catalog)
INSERT INTO core.product_groups (product_group_id, slug, name, status, attributes) VALUES
('11111111-1111-1111-1111-111111111101', 'design-suite', 'Design Suite', 'ACTIVE',
 '{"product_group_type": "plan_tier", "display_style": "comparison_table"}')
ON CONFLICT (product_group_id) DO NOTHING;

-- Products
INSERT INTO core.products (product_id, product_group_id, external_id, name, type, billing_type, status, attributes) VALUES
('22222222-2222-2222-2222-222222222201', '11111111-1111-1111-1111-111111111101',
 'prod_basic', 'Basic Plan', 'PLAN', 'RECURRING', 'ACTIVE',
 '{"tier": 1, "exclusive_with": ["22222222-2222-2222-2222-222222222202", "22222222-2222-2222-2222-222222222203"]}'),
('22222222-2222-2222-2222-222222222202', '11111111-1111-1111-1111-111111111101',
 'prod_pro', 'Pro Plan', 'PLAN', 'RECURRING', 'ACTIVE',
 '{"tier": 2, "exclusive_with": ["22222222-2222-2222-2222-222222222201", "22222222-2222-2222-2222-222222222203"]}'),
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111101',
 'prod_analytics', 'Analytics Module', 'ADDON', 'RECURRING', 'ACTIVE',
 '{}')
ON CONFLICT (product_id) DO NOTHING;

-- Product condition (Analytics는 Pro 이상만)
UPDATE core.products
SET condition = '{"field": "active_products", "op": "CONTAINS_ANY", "value": ["22222222-2222-2222-2222-222222222202"]}'
WHERE product_id = '22222222-2222-2222-2222-222222222203';

-- ProductFeatures
INSERT INTO core.product_features (product_id, feature_code, quota) VALUES
('22222222-2222-2222-2222-222222222201', 'design-editor', null),
('22222222-2222-2222-2222-222222222201', 'storage', 10737418240),
('22222222-2222-2222-2222-222222222202', 'design-editor', null),
('22222222-2222-2222-2222-222222222202', 'storage', 53687091200),
('22222222-2222-2222-2222-222222222202', 'export', 100),
('22222222-2222-2222-2222-222222222203', 'analytics-dashboard', null)
ON CONFLICT (product_id, feature_code) DO NOTHING;

-- Prices
INSERT INTO core.prices (price_id, product_id, external_id, currency, amount, billing_interval, is_default) VALUES
('33333333-3333-3333-3333-333333333301', '22222222-2222-2222-2222-222222222201',
 'price_basic_usd', 'USD', 1900, 'MONTH', true),
('33333333-3333-3333-3333-333333333302', '22222222-2222-2222-2222-222222222201',
 'price_basic_eur', 'EUR', 1700, 'MONTH', true),
('33333333-3333-3333-3333-333333333303', '22222222-2222-2222-2222-222222222202',
 'price_pro_usd', 'USD', 4900, 'MONTH', true),
('33333333-3333-3333-3333-333333333304', '22222222-2222-2222-2222-222222222202',
 'price_pro_eur', 'EUR', 4500, 'MONTH', true)
ON CONFLICT (price_id) DO NOTHING;

-- 조건부 가격 (Enterprise 고객 할인)
INSERT INTO core.prices (price_id, product_id, external_id, currency, amount, billing_interval, is_default, condition, attributes) VALUES
('33333333-3333-3333-3333-333333333305', '22222222-2222-2222-2222-222222222202',
 'price_pro_usd_ent', 'USD', 3900, 'MONTH', false,
 '{"field": "customer_tags", "op": "CONTAINS", "value": "enterprise"}',
 '{"priority": 1, "discount_reason": "enterprise_discount"}')
ON CONFLICT (price_id) DO NOTHING;

-- 번들 무료 가격 (Pro 구독자에게 Analytics 무료)
INSERT INTO core.prices (price_id, product_id, external_id, currency, amount, billing_interval, is_default, condition, attributes) VALUES
('33333333-3333-3333-3333-333333333306', '22222222-2222-2222-2222-222222222203',
 'price_analytics_usd', 'USD', 1500, 'MONTH', true, null, '{}'),
('33333333-3333-3333-3333-333333333307', '22222222-2222-2222-2222-222222222203',
 'price_analytics_free', 'USD', 0, 'MONTH', false,
 '{"field": "active_products", "op": "CONTAINS_ANY", "value": ["22222222-2222-2222-2222-222222222202"]}',
 '{"priority": 0, "discount_reason": "pro_bundle_free"}')
ON CONFLICT (price_id) DO NOTHING;
