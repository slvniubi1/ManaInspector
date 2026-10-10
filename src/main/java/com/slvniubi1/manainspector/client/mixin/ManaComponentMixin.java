package com.slvniubi1.manainspector.client.mixin;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "vazkii.botania.client.patchouli.component.ManaComponent", remap = false)
public abstract class ManaComponentMixin {
    @Shadow(remap = false) private int x;
    @Shadow(remap = false) private int y;
    @Shadow(remap = false) private int[] manaValues;

    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private void manaInspector$drawExactMana(GuiGraphics gui, @Coerce Object context,
            float pticks, int mouseX, int mouseY, CallbackInfo ci) {
        if (manaValues == null || manaValues.length == 0) return;
        try {
            int ticks = ((Number) context.getClass().getMethod("getTicksInBook").invoke(context)).intValue();
            int index = (ticks / 20) % manaValues.length;
            String text = String.format(Locale.ROOT, "%,d mana", manaValues[index]);
            var font = Minecraft.getInstance().font;
            gui.drawString(font, Component.literal(text), x + 51 - font.width(text) / 2, y + 26,
                    0xFF303030, false);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Keep the original Lexica rendering intact if Patchouli changes its context API.
        }
    }
}
