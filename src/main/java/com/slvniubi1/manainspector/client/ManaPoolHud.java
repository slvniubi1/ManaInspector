package com.slvniubi1.manainspector.client;

import java.lang.reflect.Method;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import com.slvniubi1.manainspector.ManaInspectorMod;

@EventBusSubscriber(modid = ManaInspectorMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ManaPoolHud {
    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_HEIGHT = 58;
    private static final int BAR_WIDTH = 196;

    private ManaPoolHud() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui) return;

        HitResult hit = minecraft.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return;

        var state = minecraft.level.getBlockState(blockHit.getBlockPos());
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (blockId == null || !"botania".equals(blockId.getNamespace()) || !blockId.getPath().endsWith("mana_pool")) return;

        BlockEntity blockEntity = minecraft.level.getBlockEntity(blockHit.getBlockPos());
        if (blockEntity == null) return;

        ManaValues values = readMana(blockEntity);
        if (values == null || values.capacity <= 0) return;

        double fraction = Math.max(0.0, Math.min(1.0, (double) values.current / values.capacity));
        int percent = (int) Math.round(fraction * 100.0);
        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int left = (screenWidth - PANEL_WIDTH) / 2;
        int top = 8;

        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xD91A1724);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 2, 0xFF35D6C7);
        graphics.drawString(minecraft.font, Component.literal("Mana Inspector"), left + 12, top + 8, 0xFFFFFFFF, false);
        graphics.drawString(minecraft.font, Component.literal(String.format(Locale.ROOT, "%,d / %,d mana", values.current, values.capacity)), left + 12, top + 21, 0xFFE6E1F2, false);
        String percentage = percent + "%";
        graphics.drawString(minecraft.font, Component.literal(percentage), left + PANEL_WIDTH - 12 - minecraft.font.width(percentage), top + 21, 0xFF72F1D8, false);

        int barLeft = left + 12;
        int barTop = top + 39;
        graphics.fill(barLeft, barTop, barLeft + BAR_WIDTH, barTop + 8, 0xFF494354);
        int filled = (int) Math.round(BAR_WIDTH * fraction);
        if (filled > 0) {
            graphics.fill(barLeft, barTop, barLeft + filled, barTop + 8, 0xFF35D6C7);
            if (filled > 2) graphics.fill(barLeft, barTop, barLeft + filled, barTop + 2, 0xFF8DFFE9);
        }
        graphics.fill(barLeft, barTop + 7, barLeft + BAR_WIDTH, barTop + 8, 0xFF201D29);
    }

    private static ManaValues readMana(BlockEntity blockEntity) {
        try {
            Method currentMethod = blockEntity.getClass().getMethod("getCurrentMana");
            Method capacityMethod = blockEntity.getClass().getMethod("getMaxMana");
            Object currentValue = currentMethod.invoke(blockEntity);
            Object capacityValue = capacityMethod.invoke(blockEntity);
            if (currentValue instanceof Number current && capacityValue instanceof Number capacity) {
                return new ManaValues(Math.max(0, current.intValue()), Math.max(0, capacity.intValue()));
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Not a compatible Botania pool entity, or its API is unavailable.
        }
        return null;
    }

    private record ManaValues(int current, int capacity) {}
}
