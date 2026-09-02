package com.bettercontent.dynamicsurvivalhud.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.junit.jupiter.api.Test;

final class DynamicHudOverlayMappingTest {
    @Test
    void mapsTheChosenSurvivalOverlays() {
        assertEquals(HudElement.HOTBAR,
                DynamicHudController.overlayElements().get(VanillaGuiOverlay.HOTBAR.id()));
        assertEquals(HudElement.HOTBAR,
                DynamicHudController.overlayElements().get(VanillaGuiOverlay.ITEM_NAME.id()));
        assertEquals(HudElement.HEALTH,
                DynamicHudController.overlayElements().get(VanillaGuiOverlay.PLAYER_HEALTH.id()));
        assertEquals(HudElement.EXPERIENCE,
                DynamicHudController.overlayElements().get(VanillaGuiOverlay.EXPERIENCE_BAR.id()));
        assertEquals(HudElement.THIRST,
                DynamicHudController.overlayElements().get(new ResourceLocation("thirst", "thirst_level")));
        assertEquals(HudElement.BODY_TEMPERATURE,
                DynamicHudController.overlayElements().get(new ResourceLocation("cold_sweat", "vague_temp")));
        assertEquals(HudElement.WORLD_TEMPERATURE,
                DynamicHudController.overlayElements().get(new ResourceLocation("cold_sweat", "world_temp")));
        assertEquals(HudElement.FOOD_TEMPERATURE_EFFECTS,
                DynamicHudController.overlayElements().get(new ResourceLocation("cold_sweat", "food_effects")));
    }

    @Test
    void depletionAndPersistentHotbarSlotsUseExactBoundaries() {
        assertFalse(DynamicHudController.depleted(20, 20));
        assertTrue(DynamicHudController.depleted(19, 20));
        assertTrue(DynamicHudController.keepHotbarSlotVisible(4, 3));
        assertTrue(DynamicHudController.keepHotbarSlotVisible(10, 3));
        assertFalse(DynamicHudController.keepHotbarSlotVisible(3, 3));
    }

    @Test
    void leavesNonSurvivalOverlaysUnmanaged() {
        assertFalse(DynamicHudController.overlayElements().containsKey(VanillaGuiOverlay.CROSSHAIR.id()));
        assertFalse(DynamicHudController.overlayElements().containsKey(VanillaGuiOverlay.CHAT_PANEL.id()));
        assertFalse(DynamicHudController.overlayElements().containsKey(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id()));
        assertFalse(DynamicHudController.overlayElements().containsKey(VanillaGuiOverlay.POTION_ICONS.id()));
    }
}
