package net.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "enchant", at = @At("HEAD"), cancellable = true)
    private void onEnchant(Enchantment enchantment, int level, CallbackInfo ci) {
        ItemStack stack = (ItemStack)(Object)this;
        ListTag enchantments = stack.getEnchantmentTags();
        enchantments.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), level));
        stack.getOrCreateTag().put("Enchantments", enchantments);
        ci.cancel();
    }
}
