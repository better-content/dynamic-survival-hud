package com.bettercontent.dynamicsurvivalhud.client.hud;

import com.bettercontent.dynamicsurvivalhud.DynamicSurvivalHud;
import com.bettercontent.dynamicsurvivalhud.config.DynamicSurvivalHudConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = DynamicSurvivalHud.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DynamicHudController {
    private static final ResourceLocation THIRST_OVERLAY = new ResourceLocation("thirst", "thirst_level");
    private static final ResourceLocation COLD_SWEAT_BODY_TEMP = new ResourceLocation("cold_sweat", "body_temp");
    private static final ResourceLocation COLD_SWEAT_VAGUE_TEMP = new ResourceLocation("cold_sweat", "vague_temp");
    private static final ResourceLocation COLD_SWEAT_WORLD_TEMP = new ResourceLocation("cold_sweat", "world_temp");
    private static final ResourceLocation COLD_SWEAT_FOOD_EFFECTS = new ResourceLocation("cold_sweat", "food_effects");

    private static final Map<ResourceLocation, HudElement> OVERLAY_ELEMENTS = Map.ofEntries(
            Map.entry(VanillaGuiOverlay.HOTBAR.id(), HudElement.HOTBAR),
            Map.entry(VanillaGuiOverlay.ITEM_NAME.id(), HudElement.HOTBAR),
            Map.entry(VanillaGuiOverlay.PLAYER_HEALTH.id(), HudElement.HEALTH),
            Map.entry(VanillaGuiOverlay.ARMOR_LEVEL.id(), HudElement.ARMOR),
            Map.entry(VanillaGuiOverlay.FOOD_LEVEL.id(), HudElement.HUNGER),
            Map.entry(VanillaGuiOverlay.AIR_LEVEL.id(), HudElement.AIR),
            Map.entry(VanillaGuiOverlay.EXPERIENCE_BAR.id(), HudElement.EXPERIENCE),
            Map.entry(VanillaGuiOverlay.MOUNT_HEALTH.id(), HudElement.MOUNT_HEALTH),
            Map.entry(VanillaGuiOverlay.JUMP_BAR.id(), HudElement.MOUNT_JUMP),
            Map.entry(THIRST_OVERLAY, HudElement.THIRST),
            Map.entry(COLD_SWEAT_BODY_TEMP, HudElement.BODY_TEMPERATURE),
            Map.entry(COLD_SWEAT_VAGUE_TEMP, HudElement.BODY_TEMPERATURE),
            Map.entry(COLD_SWEAT_WORLD_TEMP, HudElement.WORLD_TEMPERATURE),
            Map.entry(COLD_SWEAT_FOOD_EFFECTS, HudElement.FOOD_TEMPERATURE_EFFECTS)
    );

    private static final EnumMap<HudElement, HudFadeState> STATES = new EnumMap<>(HudElement.class);
    private static final EnumMap<HudElement, Object> PREVIOUS_VALUES = new EnumMap<>(HudElement.class);
    private static LocalPlayer trackedPlayer;
    private static boolean peeking;
    private static boolean wasEnabled = true;

    static {
        for (HudElement element : HudElement.values()) {
            STATES.put(element, new HudFadeState());
        }
    }

    private DynamicHudController() {
    }

    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        final boolean enabled = DynamicSurvivalHudConfig.dynamicHudEnabled();
        if (enabled && !wasEnabled) {
            reset(player);
        }
        wasEnabled = enabled;

        if (player == null) {
            reset(null);
            return;
        }
        if (minecraft.screen != null) {
            return;
        }
        if (player != trackedPlayer) {
            reset(player);
            sample(player, minecraft);
            return;
        }

        STATES.values().forEach(HudFadeState::tick);
        sample(player, minecraft);
    }

    public static void onPhysicalSneak(final boolean down) {
        if (down == peeking) return;
        peeking = down;
        if (!down) {
            revealAll();
        }
    }

    public static void cancelPhysicalSneak() {
        peeking = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void beforeOverlay(final RenderGuiOverlayEvent.Pre event) {
        clearStaleRenderState();
        if (event.isCanceled() || !DynamicSurvivalHudConfig.dynamicHudEnabled()) return;

        final HudElement element = OVERLAY_ELEMENTS.get(event.getOverlay().id());
        if (element == null) return;
        final float alpha = STATES.get(element).alpha(
                event.getPartialTick(),
                DynamicSurvivalHudConfig.dynamicHudHoldTicks(),
                DynamicSurvivalHudConfig.dynamicHudFadeTicks(),
                peeking
        );
        DynamicHudRenderState.begin(event.getOverlay().id(), alpha);
        DynamicHudRenderState.applyShaderAlpha();
        if (alpha <= 0.0F && !VanillaGuiOverlay.HOTBAR.id().equals(event.getOverlay().id())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void afterCanceledOverlay(final RenderGuiOverlayEvent.Pre event) {
        if (event.isCanceled() && DynamicHudRenderState.isActive(event.getOverlay().id())) {
            clearRenderState();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void afterOverlay(final RenderGuiOverlayEvent.Post event) {
        if (DynamicHudRenderState.isActive(event.getOverlay().id())) {
            clearRenderState();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void afterGui(final RenderGuiEvent.Post event) {
        clearStaleRenderState();
    }

    static Map<ResourceLocation, HudElement> overlayElements() {
        return OVERLAY_ELEMENTS;
    }

    private static void sample(final LocalPlayer player, final Minecraft minecraft) {
        final double dangerFraction = DynamicSurvivalHudConfig.dynamicHudDangerFraction();

        update(HudElement.HEALTH, new HealthValue(player.getHealth(), player.getAbsorptionAmount(), player.getMaxHealth()));
        danger(HudElement.HEALTH, healthDangerous(player.getHealth(), player.getMaxHealth(), dangerFraction));

        update(HudElement.ARMOR, armorValue(player));

        final int food = player.getFoodData().getFoodLevel();
        update(HudElement.HUNGER, food);
        danger(HudElement.HUNGER, depleted(food, 20));

        final int air = player.getAirSupply();
        update(HudElement.AIR, air);
        danger(HudElement.AIR, air < player.getMaxAirSupply());

        update(HudElement.EXPERIENCE,
                new ExperienceValue(player.experienceLevel, player.totalExperience, player.experienceProgress));

        update(HudElement.HOTBAR, new HotbarValue(
                player.getInventory().selected,
                itemValue(player.getMainHandItem()),
                itemValue(player.getOffhandItem())
        ));
        if (player.isUsingItem()) {
            reveal(HudElement.HOTBAR);
        }

        final Entity vehicle = player.getVehicle();
        update(HudElement.MOUNT_HEALTH, mountHealth(vehicle));
        update(HudElement.MOUNT_JUMP, vehicle == null ? null : vehicle.getId());
        if (vehicle instanceof PlayerRideableJumping && minecraft.options.keyJump.isDown()) {
            reveal(HudElement.MOUNT_JUMP);
        }

        if (ModList.get().isLoaded("thirst")) {
            final int thirst = ThirstHudTelemetry.sample(player);
            update(HudElement.THIRST, thirst);
            danger(HudElement.THIRST, depleted(thirst, ThirstHudTelemetry.MAX_THIRST));
        }

        if (ModList.get().isLoaded("cold_sweat")) {
            final ColdSweatHudTelemetry.Snapshot snapshot = ColdSweatHudTelemetry.sample(player);
            update(HudElement.BODY_TEMPERATURE, snapshot.bodyTemperature());
            update(HudElement.WORLD_TEMPERATURE, snapshot.worldSeverity());
            update(HudElement.FOOD_TEMPERATURE_EFFECTS, snapshot.foodEffects());
            danger(HudElement.BODY_TEMPERATURE, snapshot.dangerousBodyTemperature());
            danger(HudElement.WORLD_TEMPERATURE, snapshot.dangerousWorldTemperature());
        }
    }

    private static List<ItemValue> armorValue(final LocalPlayer player) {
        final List<ItemValue> armor = new ArrayList<>();
        for (ItemStack stack : player.getArmorSlots()) {
            armor.add(itemValue(stack));
        }
        return List.copyOf(armor);
    }

    private static MountHealthValue mountHealth(final Entity vehicle) {
        if (vehicle instanceof LivingEntity living) {
            return new MountHealthValue(vehicle.getId(), living.getHealth(), living.getMaxHealth());
        }
        return null;
    }

    private static ItemValue itemValue(final ItemStack stack) {
        return new ItemValue(stack.getItem(), stack.getCount(), stack.getDamageValue(), Objects.hashCode(stack.getTag()));
    }

    private static double ratio(final double value, final double maximum) {
        return maximum <= 0.0D ? 1.0D : value / maximum;
    }

    static boolean healthDangerous(final double health, final double maximum, final double dangerFraction) {
        return health < maximum && ratio(health, maximum) <= dangerFraction;
    }

    static boolean depleted(final int value, final int maximum) {
        return value < maximum;
    }

    public static boolean keepHotbarSlotVisible(final int renderSeed, final int selectedSlot) {
        return renderSeed == selectedSlot || renderSeed == -1;
    }

    private static void update(final HudElement element, final Object value) {
        if (PREVIOUS_VALUES.containsKey(element) && !Objects.equals(PREVIOUS_VALUES.get(element), value)) {
            reveal(element);
        }
        PREVIOUS_VALUES.put(element, value);
    }

    private static void danger(final HudElement element, final boolean dangerous) {
        STATES.get(element).setDangerous(dangerous);
    }

    private static void reveal(final HudElement element) {
        STATES.get(element).reveal();
    }

    private static void revealAll() {
        STATES.values().forEach(HudFadeState::reveal);
    }

    private static void reset(final LocalPlayer player) {
        trackedPlayer = player;
        peeking = false;
        PREVIOUS_VALUES.clear();
        STATES.values().forEach(state -> {
            state.hide();
        });
        DynamicHudRenderState.clear();
    }

    private static void clearStaleRenderState() {
        if (DynamicHudRenderState.hasActiveOverlay()) {
            clearRenderState();
        }
    }

    private static void clearRenderState() {
        DynamicHudRenderState.clear();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private record HealthValue(float health, float absorption, float maximum) {
    }

    private record ExperienceValue(int level, int total, float progress) {
    }

    private record ItemValue(Item item, int count, int damage, int tagHash) {
    }

    private record HotbarValue(int selectedSlot, ItemValue mainHand, ItemValue offHand) {
    }

    private record MountHealthValue(int entityId, float health, float maximum) {
    }
}
