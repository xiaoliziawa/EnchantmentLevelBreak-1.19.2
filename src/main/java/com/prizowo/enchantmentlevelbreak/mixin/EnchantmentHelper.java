package com.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentHelper {

    @Inject(method = "getFullname", at = @At("RETURN"), cancellable = true)
    private void modifyEnchantmentName(int level, CallbackInfoReturnable<Component> cir) {
        Component originalName = cir.getReturnValue();
        String modifiedName = originalName.getString().replaceAll("enchantment\\.level\\.\\d+", String.valueOf(level));
        cir.setReturnValue(Component.literal(modifiedName));
    }
}