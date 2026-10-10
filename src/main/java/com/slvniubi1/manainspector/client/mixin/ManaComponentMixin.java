package com.slvniubi1.manainspector.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.client.patchouli.component.ManaComponent;
import vazkii.patchouli.api.IComponentRenderContext;

import java.util.Locale;

@Mixin(value = ManaComponent.class, remap = false)
public abstract class ManaComponentMixin {
    @Shadow(remap = false) private int x;
    @Shadow(remap = false) private int y;
    @Shadow(remap = false) private int[] manaValues;

    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private void manaInspector$drawExactMana(GuiGraphics gui, IComponentRenderContext context,
            float pticks, int mouseX, int mouseY, CallbackInfo ci) {
        if (manaValues == null || manaValues.length == 0) {
            return;
        }
        int index = (context.getTicksInBook() / 20) % manaValues.length;
        String text = String.format(Locale.ROOT, "%,d mana", manaValues[index]);
        var font = Minecraft.getInstance().font;
        gui.drawString(font, Component.literal(text), x + 51 - font.width(text) / 2, y + 26,
                0xFF303030, false);
    }
}
