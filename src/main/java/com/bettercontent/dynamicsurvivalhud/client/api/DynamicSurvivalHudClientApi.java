package com.bettercontent.dynamicsurvivalhud.client.api;

import com.bettercontent.dynamicsurvivalhud.client.hud.DynamicHudController;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Client presentation integration; gameplay state remains with its owning mod. */
@OnlyIn(Dist.CLIENT)
public final class DynamicSurvivalHudClientApi {
    private DynamicSurvivalHudClientApi() {}

    /** Injury owner supplies semantic HP and risk so health fades and danger cues agree. */
    public static void setInjuryHealth(boolean atDoor, float semanticHealth, double probability) {
        DynamicHudController.setInjuryHealth(atDoor, semanticHealth, probability);
    }

    /** Matching opacity for adornments on the health bar. */
    public static float healthAlpha(float partialTick) {
        return DynamicHudController.healthAlpha(partialTick);
    }

    public static void setPhysicalSneakDown(boolean down) {
        DynamicHudController.onPhysicalSneak(down);
    }

    public static void cancelPhysicalSneak() {
        DynamicHudController.cancelPhysicalSneak();
    }
}
