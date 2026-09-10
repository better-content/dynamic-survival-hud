package com.bettercontent.dynamicsurvivalhud.client.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

final class DynamicSurvivalHudClientApiTest {
    @Test
    void acceptsPhysicalSneakTransitionsWithoutClientDiscovery() {
        assertDoesNotThrow(() -> {
            DynamicSurvivalHudClientApi.setPhysicalSneakDown(true);
            DynamicSurvivalHudClientApi.setPhysicalSneakDown(false);
            DynamicSurvivalHudClientApi.cancelPhysicalSneak();
        });
    }
}
