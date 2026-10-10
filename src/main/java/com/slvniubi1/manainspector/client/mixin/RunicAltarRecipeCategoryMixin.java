package com.slvniubi1.manainspector.client.mixin;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.api.recipe.RunicAltarRecipe;
import vazkii.botania.client.integration.jei.RunicAltarRecipeCategory;

import java.util.Locale;

@Mixin(value = RunicAltarRecipeCategory.class, remap = false)
public abstract class RunicAltarRecipeCategoryMixin {
    @Inject(method = "draw", at = @At("TAIL"), remap = false)
    private void manaInspector$drawManaCost(RunicAltarRecipe recipe, IRecipeSlotsView slotsView,
            GuiGraphics gui, double mouseX, double mouseY, CallbackInfo ci) {
        String text = String.format(Locale.ROOT, "%,d mana", recipe.getMana());
        var font = Minecraft.getInstance().font;
        gui.drawString(font, Component.literal(text), 57 - font.width(text) / 2, 86, 0xFF303030, false);
    }
}
