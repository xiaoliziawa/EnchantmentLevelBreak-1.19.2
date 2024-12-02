package com.prizowo.enchantmentlevelbreak.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(EnchantCommand.class)
public class Command {
    @Shadow @Final private static DynamicCommandExceptionType ERROR_INCOMPATIBLE;
    @Shadow @Final private static DynamicCommandExceptionType ERROR_NO_ITEM;
    @Shadow @Final private static DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY;
    @Shadow @Final private static SimpleCommandExceptionType ERROR_NOTHING_HAPPENED;
    @Shadow @Final private static Dynamic2CommandExceptionType ERROR_LEVEL_TOO_HIGH;

    @Inject(method = "enchant", at = @At("HEAD"), cancellable = true)
    private static void tooHigh(CommandSourceStack p_137015_, Collection<? extends Entity> p_137016_, 
            Enchantment p_137017_, int p_137018_, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        
        if (p_137018_ > p_137017_.getMaxLevel()) {
            throw ERROR_LEVEL_TOO_HIGH.create(p_137018_, p_137017_.getMaxLevel());
        }
        
        int i = 0;

        for(Entity entity : p_137016_) {
            if (entity instanceof LivingEntity livingentity) {
                ItemStack itemstack = livingentity.getMainHandItem();
                if (!itemstack.isEmpty()) {
                    if (p_137017_.canEnchant(itemstack) && EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantments(itemstack).keySet(), p_137017_)) {
                        itemstack.enchant(p_137017_, p_137018_);
                        ++i;
                    } else if (p_137016_.size() == 1) {
                        throw ERROR_INCOMPATIBLE.create(itemstack.getItem().getName(itemstack).getString());
                    }
                } else if (p_137016_.size() == 1) {
                    throw ERROR_NO_ITEM.create(livingentity.getName().getString());
                }
            } else if (p_137016_.size() == 1) {
                throw ERROR_NOT_LIVING_ENTITY.create(entity.getName().getString());
            }
        }

        if (i == 0) {
            throw ERROR_NOTHING_HAPPENED.create();
        } else {
            Component message;
            if (p_137016_.size() == 1) {
                String enchantName = p_137017_.getFullname(p_137018_).getString();
                String targetName = p_137016_.iterator().next().getDisplayName().getString();
                message = Component.empty().append(enchantName)
                    .append(" has been applied to ")
                    .append(targetName);
            } else {
                String enchantName = p_137017_.getFullname(p_137018_).getString();
                message = Component.empty().append(enchantName)
                    .append(" has been applied to ")
                    .append(String.valueOf(p_137016_.size()))
                    .append(" items");
            }
            p_137015_.sendSuccess(message, true);

            cir.setReturnValue(i);
        }

        cir.cancel();
    }
}
