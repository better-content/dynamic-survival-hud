package com.bettercontent.dynamicsurvivalhud.client.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

final class DynamicSurvivalHudClientApiTest {
    @Test
    void injuryPresentationAcceptsSemanticZeroAndRiskChanges() {
        assertDoesNotThrow(() -> {
            DynamicSurvivalHudClientApi.setInjuryHealth(true, 0, 0.75);
            DynamicSurvivalHudClientApi.setInjuryHealth(false, 1, 0.75);
            DynamicSurvivalHudClientApi.setInjuryHealth(false, 20, 0);
        });
    }

    @Test
    void acceptsPhysicalSneakTransitionsWithoutClientDiscovery() {
        assertDoesNotThrow(() -> {
            DynamicSurvivalHudClientApi.setPhysicalSneakDown(true);
            DynamicSurvivalHudClientApi.setPhysicalSneakDown(false);
            DynamicSurvivalHudClientApi.cancelPhysicalSneak();
        });
    }
}
