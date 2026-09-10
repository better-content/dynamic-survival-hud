package com.bettercontent.dynamicsurvivalhud.client.api;

import com.bettercontent.dynamicsurvivalhud.client.hud.DynamicHudController;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Stable client-only integration surface for physical sneak-key transitions. */
@OnlyIn(Dist.CLIENT)
public final class DynamicSurvivalHudClientApi {
    private DynamicSurvivalHudClientApi() {}

    public static void setPhysicalSneakDown(boolean down) {
        DynamicHudController.onPhysicalSneak(down);
    }

    public static void cancelPhysicalSneak() {
        DynamicHudController.cancelPhysicalSneak();
    }
}
