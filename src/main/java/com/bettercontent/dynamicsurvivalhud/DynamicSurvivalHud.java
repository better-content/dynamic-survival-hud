package com.bettercontent.dynamicsurvivalhud;

import com.bettercontent.dynamicsurvivalhud.config.DynamicSurvivalHudConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(DynamicSurvivalHud.MOD_ID)
public final class DynamicSurvivalHud {
    public static final String MOD_ID = "dynamic_survival_hud";

    public DynamicSurvivalHud() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DynamicSurvivalHudConfig.SPEC);
    }
}
