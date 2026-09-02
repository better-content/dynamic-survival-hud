package com.bettercontent.dynamicsurvivalhud.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;

public final class DynamicHudRenderState {
    private static ResourceLocation activeOverlay;
    private static float alpha = 1.0F;
    private static int fullVisibilityDepth;

    private DynamicHudRenderState() {
    }

    public static void begin(final ResourceLocation overlay, final float overlayAlpha) {
        activeOverlay = overlay;
        alpha = clamp(overlayAlpha);
    }

    public static boolean isActive(final ResourceLocation overlay) {
        return activeOverlay != null && activeOverlay.equals(overlay);
    }

    public static boolean hasActiveOverlay() {
        return activeOverlay != null;
    }

    public static float alpha() {
        return fullVisibilityDepth > 0 ? 1.0F : alpha;
    }

    public static void pushFullVisibility() {
        fullVisibilityDepth++;
    }

    public static void popFullVisibility() {
        if (fullVisibilityDepth <= 0) {
            throw new IllegalStateException("Dynamic HUD full-visibility stack underflow");
        }
        fullVisibilityDepth--;
    }

    public static void runFullyVisible(final Runnable action) {
        pushFullVisibility();
        applyShaderAlpha();
        try {
            action.run();
        } finally {
            popFullVisibility();
            applyShaderAlpha();
        }
    }

    public static void applyShaderAlpha() {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void clear() {
        activeOverlay = null;
        alpha = 1.0F;
        fullVisibilityDepth = 0;
    }

    public static float multiplyAlpha(final float requestedAlpha) {
        return requestedAlpha * alpha();
    }

    private static float clamp(final float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
