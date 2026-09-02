package com.bettercontent.dynamicsurvivalhud.mixin;

import com.bettercontent.dynamicsurvivalhud.client.hud.DynamicHudController;
import com.bettercontent.dynamicsurvivalhud.client.hud.DynamicHudRenderState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Unique
    private boolean dynamicSurvivalHud$visibleSlot;

    @Redirect(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
                    ordinal = 1
            )
    )
    private void dynamicSurvivalHud$keepSelectedFrameVisible(
            final GuiGraphics graphics,
            final ResourceLocation texture,
            final int x,
            final int y,
            final int u,
            final int v,
            final int width,
            final int height
    ) {
        dynamicSurvivalHud$renderPersistentFrame(graphics, texture, x, y, u, v, width, height);
    }

    @Redirect(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
                    ordinal = 2
            )
    )
    private void dynamicSurvivalHud$keepOffhandFrameVisible(
            final GuiGraphics graphics,
            final ResourceLocation texture,
            final int x,
            final int y,
            final int u,
            final int v,
            final int width,
            final int height
    ) {
        dynamicSurvivalHud$renderPersistentFrame(graphics, texture, x, y, u, v, width, height);
    }

    @Unique
    private static void dynamicSurvivalHud$renderPersistentFrame(
            final GuiGraphics graphics,
            final ResourceLocation texture,
            final int x,
            final int y,
            final int u,
            final int v,
            final int width,
            final int height
    ) {
        if (DynamicHudRenderState.isActive(VanillaGuiOverlay.HOTBAR.id())) {
            DynamicHudRenderState.runFullyVisible(() -> graphics.blit(texture, x, y, u, v, width, height));
        } else {
            graphics.blit(texture, x, y, u, v, width, height);
        }
    }

    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void dynamicSurvivalHud$beforeSlot(
            final GuiGraphics graphics,
            final int x,
            final int y,
            final float partialTick,
            final Player player,
            final ItemStack stack,
            final int renderSeed,
            final CallbackInfo callback
    ) {
        if (!DynamicHudRenderState.isActive(VanillaGuiOverlay.HOTBAR.id())) return;

        if (DynamicHudController.keepHotbarSlotVisible(renderSeed, player.getInventory().selected)) {
            dynamicSurvivalHud$visibleSlot = true;
            DynamicHudRenderState.pushFullVisibility();
            DynamicHudRenderState.applyShaderAlpha();
        } else if (DynamicHudRenderState.alpha() <= 0.0F) {
            callback.cancel();
        }
    }

    @Inject(method = "renderSlot", at = @At("RETURN"))
    private void dynamicSurvivalHud$afterSlot(
            final GuiGraphics graphics,
            final int x,
            final int y,
            final float partialTick,
            final Player player,
            final ItemStack stack,
            final int renderSeed,
            final CallbackInfo callback
    ) {
        if (!dynamicSurvivalHud$visibleSlot) return;
        dynamicSurvivalHud$visibleSlot = false;
        DynamicHudRenderState.popFullVisibility();
        DynamicHudRenderState.applyShaderAlpha();
    }
}
