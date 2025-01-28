package com.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.enchantment.Enchantment;
import com.prizowo.enchantmentlevelbreak.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentHelper {

    @Inject(method = "getFullname", at = @At("HEAD"), cancellable = true)
    private void modifyEnchantmentName(int level, CallbackInfoReturnable<Component> cir) {
        Enchantment enchantment = (Enchantment) (Object) this;
        MutableComponent name = Component.translatable(enchantment.getDescriptionId());
        
        if (level != 1) {
            String levelText;
            if (level > 10000) {
                levelText = String.valueOf(level);
            } else {
                levelText = Config.useRomanNumerals ? enchantmentLevelBreak$intToRoman(level) : String.valueOf(level);
            }
            name.append(" ").append(Component.literal(levelText).withStyle(ChatFormatting.GRAY));
        }
        
        cir.setReturnValue(name);
    }

    @Unique
    private static String enchantmentLevelBreak$intToRoman(int num) {
        if (num <= 0) return "0";

        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (num >= values[i]) {
                roman.append(symbols[i]);
                num -= values[i];
            }
        }

        return roman.toString();
    }
}