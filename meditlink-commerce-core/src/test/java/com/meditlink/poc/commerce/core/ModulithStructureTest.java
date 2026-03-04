package com.meditlink.poc.commerce.core;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

// 학습 포인트: 아키텍처 회귀 방지 테스트
// - 코드가 컴파일돼도 모듈 경계를 깨뜨리면 테스트가 실패하도록 강제
class ModulithStructureTest {

    @Test
    void verifiesModuleStructure() {
        ApplicationModules.of(CoreServiceApplication.class).verify();
    }
}
