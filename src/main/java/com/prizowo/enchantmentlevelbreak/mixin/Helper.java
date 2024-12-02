package com.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnchantmentHelper.class)
public class Helper {
    @Inject(method = "storeEnchantment", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putShort(Ljava/lang/String;S)V"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private static void store(ResourceLocation p_182444_, int p_182445_, CallbackInfoReturnable<CompoundTag> cir, CompoundTag compoundtag) {
        compoundtag.putInt("lvl", p_182445_);
        cir.setReturnValue(compoundtag);
        cir.cancel();
    }

    @Inject(method = "getEnchantmentLevel(Lnet/minecraft/nbt/CompoundTag;)I", at = @At("RETURN"), cancellable = true)
    private static void get(CompoundTag p_182439_, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Mth.clamp(p_182439_.getInt("lvl"), 0, Integer.MAX_VALUE));
    }

    @Inject(method = "setEnchantmentLevel", at = @At("HEAD"), cancellable = true)
    private static void set(CompoundTag p_182441_, int p_182442_, CallbackInfo ci) {
        p_182441_.putInt("lvl", p_182442_);
        ci.cancel();
    }
}
