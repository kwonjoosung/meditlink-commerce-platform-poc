-- 로컬 학습/데모용 더미 데이터
-- NEW_SPEC 기반 Merchandising 도메인 리팩토링 반영
-- Price = 정가만, 할인은 Coupon BC (PromotionPolicy)

-- ============================================================
-- Feature BC
-- ============================================================

INSERT INTO core.features (feature_id, feature_code, name, description, type, status) VALUES
('a0000000-0000-0000-0000-000000000001', 'design-editor', 'Design Editor', '디자인 에디터 기본 기능', 'BOOLEAN', 'ACTIVE'),
('a0000000-0000-0000-0000-000000000002', 'storage', 'Cloud Storage', '클라우드 저장 공간', 'QUOTA', 'ACTIVE'),
('a0000000-0000-0000-0000-000000000003', 'export', 'Export', '파일 내보내기 기능', 'QUOTA', 'ACTIVE'),
('a0000000-0000-0000-0000-000000000004', 'analytics-dashboard', 'Analytics Dashboard', '분석 대시보드', 'BOOLEAN', 'ACTIVE'),
('a0000000-0000-0000-0000-000000000005', 'api-access', 'API Access', 'REST API 접근', 'BOOLEAN', 'ACTIVE')
ON CONFLICT (feature_id) DO NOTHING;

-- ============================================================
-- ProductGroup
-- ============================================================

INSERT INTO core.product_groups (product_group_id, slug, name, description, type, status, sort_order, display_config, visibility_rules) VALUES
('c0000000-0000-0000-0000-000000000001', 'design-suite', 'Design Suite', '디자인 도구 플랜 라인업', 'PLAN_FAMILY', 'ACTIVE', 1,
 '{"display_style": "comparison_table"}', '{}'),
('c0000000-0000-0000-0000-000000000002', 'storage-addons', 'Storage Add-ons', '스토리지 추가 옵션', 'ADD_ON_FAMILY', 'ACTIVE', 2,
 '{"display_style": "list"}', '{}')
ON CONFLICT (product_group_id) DO NOTHING;

-- ============================================================
-- Products
-- ============================================================

INSERT INTO core.products (product_id, product_group_id, external_id, name, display_name, description, item_type, status, tier_order, visibility, display_config, visibility_rules, compatibility, tags) VALUES
('b0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'prod_basic', 'Basic Plan', '베이직', '기본 디자인 도구', 'SUBSCRIPTION', 'ACTIVE', 1, 'PUBLIC',
 '{"cta_text": "시작하기"}', '{}', '{}', '{}'),
('b0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', 'prod_pro', 'Pro Plan', '프로', '전문가용 디자인 도구', 'SUBSCRIPTION', 'ACTIVE', 2, 'PUBLIC',
 '{"badge": "POPULAR", "cta_text": "프로 시작하기"}', '{}', '{}', '{"promotional"}'),
('b0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000001', 'prod_enterprise', 'Enterprise Plan', '엔터프라이즈', '대기업용', 'SUBSCRIPTION', 'ACTIVE', 3, 'PUBLIC',
 '{"cta_text": "영업팀 문의"}', '{"segments": ["enterprise"]}', '{}', '{"enterprise_only"}'),
('b0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000001', 'prod_analytics', 'Analytics Module', '분석 모듈', '고급 분석 도구', 'ADD_ON', 'ACTIVE', 10, 'PUBLIC',
 '{}', '{}', '{"requires_any": ["prod_pro", "prod_enterprise"], "included_in": ["prod_enterprise"]}', '{}'),
('b0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000002', 'prod_storage_50', 'Storage 50GB', '스토리지 50GB', '추가 50GB', 'ADD_ON', 'ACTIVE', 1, 'PUBLIC',
 '{}', '{}', '{"requires_any": ["prod_basic", "prod_pro", "prod_enterprise"]}', '{}'),
('b0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000002', 'prod_storage_100', 'Storage 100GB', '스토리지 100GB', '추가 100GB', 'ADD_ON', 'ACTIVE', 2, 'PUBLIC',
 '{}', '{}', '{"requires_any": ["prod_basic", "prod_pro", "prod_enterprise"]}', '{}')
ON CONFLICT (product_id) DO NOTHING;

-- ============================================================
-- Product Features
-- ============================================================

INSERT INTO core.product_features (product_id, feature_code, quota, display_label, is_highlighted) VALUES
('b0000000-0000-0000-0000-000000000001', 'design-editor', null, '디자인 에디터', true),
('b0000000-0000-0000-0000-000000000001', 'storage', 10737418240, '10GB 스토리지', false),
('b0000000-0000-0000-0000-000000000002', 'design-editor', null, '디자인 에디터', true),
('b0000000-0000-0000-0000-000000000002', 'storage', 53687091200, '50GB 스토리지', true),
('b0000000-0000-0000-0000-000000000002', 'export', 100, '월 100회 내보내기', true),
('b0000000-0000-0000-0000-000000000002', 'api-access', null, 'API 접근', false),
('b0000000-0000-0000-0000-000000000003', 'design-editor', null, '디자인 에디터', true),
('b0000000-0000-0000-0000-000000000003', 'storage', 107374182400, '100GB 스토리지', true),
('b0000000-0000-0000-0000-000000000003', 'export', null, '무제한 내보내기', true),
('b0000000-0000-0000-0000-000000000003', 'api-access', null, 'API 접근', true),
('b0000000-0000-0000-0000-000000000003', 'analytics-dashboard', null, '분석 대시보드', true),
('b0000000-0000-0000-0000-000000000004', 'analytics-dashboard', null, '분석 대시보드', true)
ON CONFLICT (product_id, feature_code) DO NOTHING;

-- ============================================================
-- Prices (정가만. 할인은 Coupon BC의 PromotionPolicy)
-- ============================================================

INSERT INTO core.prices (price_id, product_id, external_id, currency, amount, billing_period, is_default) VALUES
('e0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'price_basic_usd_m', 'USD', 1900, 'MONTHLY', true),
('e0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 'price_basic_usd_y', 'USD', 19000, 'YEARLY', false),
('e0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000001', 'price_basic_eur_m', 'EUR', 1700, 'MONTHLY', true),
('e0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000002', 'price_pro_usd_m', 'USD', 4900, 'MONTHLY', true),
('e0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000002', 'price_pro_usd_y', 'USD', 49000, 'YEARLY', false),
('e0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000002', 'price_pro_eur_m', 'EUR', 4500, 'MONTHLY', true),
('e0000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-000000000003', 'price_ent_usd_m', 'USD', 19900, 'MONTHLY', true),
('e0000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000003', 'price_ent_usd_y', 'USD', 199000, 'YEARLY', false),
('e0000000-0000-0000-0000-000000000009', 'b0000000-0000-0000-0000-000000000004', 'price_analytics_usd_m', 'USD', 1500, 'MONTHLY', true),
('e0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000005', 'price_storage50_usd_m', 'USD', 500, 'MONTHLY', true),
('e0000000-0000-0000-0000-000000000011', 'b0000000-0000-0000-0000-000000000006', 'price_storage100_usd_m', 'USD', 900, 'MONTHLY', true)
ON CONFLICT (price_id) DO NOTHING;

-- ============================================================
-- Promotion Policies (Coupon BC)
-- ============================================================

INSERT INTO core.promotion_policies (policy_id, name, description, discount_type, discount_value, eligibility, applicable_product_ids, max_redemptions, valid_from, valid_until, status, stripe_coupon_id) VALUES
('d0000000-0000-0000-0000-000000000001', 'Enterprise 20% 할인', '엔터프라이즈 고객 대상 20% 할인', 'PERCENTAGE', 20,
 '{"segments": ["enterprise"]}',
 '["b0000000-0000-0000-0000-000000000002"]',
 null, '2026-01-01T00:00:00Z', '2026-12-31T23:59:59Z', 'ACTIVE', 'cpn_stub_ent20'),
('d0000000-0000-0000-0000-000000000002', 'Pro 구독자 Analytics 무료', 'Pro 플랜 구독 시 Analytics 모듈 무료', 'PERCENTAGE', 100,
 '{"active_products": ["prod_pro"]}',
 '["b0000000-0000-0000-0000-000000000004"]',
 null, '2026-01-01T00:00:00Z', null, 'ACTIVE', 'cpn_stub_profree')
ON CONFLICT (policy_id) DO NOTHING;
