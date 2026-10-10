package com.slvniubi1.manainspector.client.mixin;

import java.lang.reflect.Method;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "vazkii.botania.client.integration.jei.TerrestrialAgglomerationRecipeCategory", remap = false)
public abstract class TerrestrialAgglomerationRecipeCategoryMixin {
    @Inject(method = "draw", at = @At("TAIL"), remap = false)
    private void manaInspector$drawManaCost(@Coerce Object recipe, @Coerce Object slotsView,
            GuiGraphics gui, double mouseX, double mouseY, CallbackInfo ci) {
        Integer cost = manaCost(recipe);
        if (cost == null) return;
        String text = String.format(Locale.ROOT, "%,d mana", cost);
        var font = Minecraft.getInstance().font;
        gui.drawString(font, Component.literal(text), 57 - font.width(text) / 2, 115, 0xFF303030, false);
    }

    private static Integer manaCost(Object recipe) {
        try {
            Method method = recipe.getClass().getMethod("getMana");
            Object value = method.invoke(recipe);
            return value instanceof Number n ? Math.max(0, n.intValue()) : null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}
