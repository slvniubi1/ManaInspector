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
    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 76;
    private static final int BAR_WIDTH = 216;

    private ManaPoolHud() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui) return;

        HitResult hit = minecraft.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return;

        var state = minecraft.level.getBlockState(blockHit.getBlockPos());
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (blockId == null || !"botania".equals(blockId.getNamespace())) return;

        String path = blockId.getPath();
        boolean manaPool = path.endsWith("mana_pool");
        boolean manaSpreader = path.endsWith("mana_spreader");
        BlockEntity blockEntity = minecraft.level.getBlockEntity(blockHit.getBlockPos());
        if (blockEntity == null) return;

        boolean generatingFlower = !manaPool && !manaSpreader && isGeneratingFlower(blockEntity, path);
        if (!manaPool && !manaSpreader && !generatingFlower) return;

        ManaValues values = readMana(blockEntity);
        // Do not silently hide the whole HUD if a Botania variant exposes its mana through a different API.
        if (values == null || values.capacity <= 0) {
            if (generatingFlower) {
                drawUnavailableHud(event.getGuiGraphics(), minecraft, "Generating Flower",
                        "Mana data unavailable", 0xFFB4E66E);
            } else if (manaSpreader) {
                drawUnavailableHud(event.getGuiGraphics(), minecraft, "Mana Spreader",
                        "Mana data unavailable", 0xFFFFB74D);
            }
            return;
        }

        double fraction = Math.max(0.0, Math.min(1.0, (double) values.current / values.capacity));
        int percent = (int) Math.round(fraction * 100.0);
        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int left = (screenWidth - PANEL_WIDTH) / 2;
        int top = 8;
        int accent = manaSpreader ? 0xFFFFB74D : generatingFlower ? 0xFFB4E66E : 0xFF35D6C7;
        String title = manaSpreader ? "Mana Spreader" : generatingFlower ? "Generating Flower" : "Mana Pool";

        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xD91A1724);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 2, accent);
        graphics.drawString(minecraft.font, Component.literal(title), left + 12, top + 7, 0xFFFFFFFF, false);
        graphics.drawString(minecraft.font, Component.literal(String.format(Locale.ROOT, "%,d / %,d mana", values.current, values.capacity)), left + 12, top + 20, 0xFFE6E1F2, false);
        String percentage = percent + "%";
        graphics.drawString(minecraft.font, Component.literal(percentage), left + PANEL_WIDTH - 12 - minecraft.font.width(percentage), top + 20, accent, false);

        int infoY = top + 32;
        if (manaSpreader) {
            Integer burstMana = readNextBurstMana(blockEntity);
            String burstText = burstMana == null
                    ? "Next burst: unavailable"
                    : "Next burst cost: " + String.format(Locale.ROOT, "%,d mana", burstMana);
            graphics.drawString(minecraft.font, Component.literal(burstText), left + 12, infoY, 0xFFFFD08A, false);
        } else if (generatingFlower) {
            graphics.drawString(minecraft.font, Component.literal("Stored mana in flower"), left + 12, infoY, 0xFFD9F4B0, false);
        }

        int barLeft = left + 12;
        int barTop = top + 49;
        graphics.fill(barLeft, barTop, barLeft + BAR_WIDTH, barTop + 8, 0xFF494354);
        int filled = (int) Math.round(BAR_WIDTH * fraction);
        if (filled > 0) {
            graphics.fill(barLeft, barTop, barLeft + filled, barTop + 8, accent);
            if (filled > 2) graphics.fill(barLeft, barTop, barLeft + filled, barTop + 2, 0xFFFFF0C2);
        }
        graphics.fill(barLeft, barTop + 7, barLeft + BAR_WIDTH, barTop + 8, 0xFF201D29);
    }

    private static boolean isGeneratingFlower(BlockEntity blockEntity, String path) {
        // Botania's flower classes inherit this API class; checking the hierarchy covers flowers
        // whose registry IDs are not in a hard-coded list.
        for (Class<?> type = blockEntity.getClass(); type != null; type = type.getSuperclass()) {
            String name = type.getSimpleName().toLowerCase(Locale.ROOT);
            if (name.contains("generatingflowerblockentity") || name.contains("generatingflower")) return true;
        }
        String id = path.toLowerCase(Locale.ROOT);
        return id.contains("endoflame") || id.contains("hydroangeas")
                || id.contains("gourmaryllis") || id.contains("entropinnyum")
                || id.contains("kekimurus") || id.contains("spectrolus")
                || id.contains("rafflowsia") || id.contains("dandelifeon")
                || id.contains("munchdew") || id.contains("narslimmus")
                || id.contains("shulk_me_not") || id.contains("orechid")
                || id.contains("thermalily") || id.contains("rosa_arcana");
    }

    private static void drawUnavailableHud(GuiGraphics graphics, Minecraft minecraft,
            String title, String message, int accent) {
        int left = (minecraft.getWindow().getGuiScaledWidth() - PANEL_WIDTH) / 2;
        int top = 8;
        graphics.fill(left, top, left + PANEL_WIDTH, top + 48, 0xD91A1724);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 2, accent);
        graphics.drawString(minecraft.font, Component.literal(title), left + 12, top + 7, 0xFFFFFFFF, false);
        graphics.drawString(minecraft.font, Component.literal(message), left + 12, top + 24, accent, false);
    }

    private static ManaValues readMana(BlockEntity blockEntity) {
        Object currentValue = invokeNumberMethod(blockEntity, new String[] {"getCurrentMana", "getMana"});
        Object capacityValue = invokeNumberMethod(blockEntity, new String[] {"getMaxMana"});
        if (currentValue instanceof Number current && capacityValue instanceof Number capacity) {
            return new ManaValues(Math.max(0, current.intValue()), Math.max(0, capacity.intValue()));
        }
        return null;
    }

    private static Integer readNextBurstMana(BlockEntity blockEntity) {
        try {
            Method simulationMethod = blockEntity.getClass().getMethod("runBurstSimulation");
            Object burst = simulationMethod.invoke(blockEntity);
            if (burst != null) {
                Object mana = invokeNumberMethod(burst, new String[] {"getMana"});
                if (mana instanceof Number number) return Math.max(0, number.intValue());
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Some Botania versions or spreader variants do not expose burst simulation.
        }
        return null;
    }

    private static Object invokeNumberMethod(Object target, String[] methodNames) {
        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName);
                Object value = method.invoke(target);
                if (value instanceof Number) return value;
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // Try the next compatible public API method name.
            }
        }
        return null;
    }

    private record ManaValues(int current, int capacity) {}
}