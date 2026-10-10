package com.slvniubi1.manainspector.client.mixin;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.api.recipe.ManaInfusionRecipe;
import vazkii.botania.client.integration.jei.ManaPoolRecipeCategory;

import java.util.Locale;

@Mixin(value = ManaPoolRecipeCategory.class, remap = false)
public abstract class ManaPoolRecipeCategoryMixin {
    @Inject(method = "draw", at = @At("TAIL"), remap = false)
    private void manaInspector$drawManaCost(ManaInfusionRecipe recipe, IRecipeSlotsView slotsView,
            GuiGraphics gui, double mouseX, double mouseY, CallbackInfo ci) {
        String text = String.format(Locale.ROOT, "%,d mana", recipe.getManaToConsume());
        var font = Minecraft.getInstance().font;
        gui.drawString(font, Component.literal(text), 71 - font.width(text) / 2, 39, 0xFF303030, false);
    }
}
