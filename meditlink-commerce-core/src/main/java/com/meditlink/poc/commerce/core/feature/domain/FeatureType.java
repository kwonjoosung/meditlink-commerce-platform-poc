package com.meditlink.poc.commerce.core.feature.domain;

public enum FeatureType {
    BOOLEAN,    // 있다/없다 (예: analytics-dashboard)
    QUOTA,      // 수량 제한 (예: storage 50GB)
    UNLIMITED   // 무제한 (예: unlimited API calls)
}
