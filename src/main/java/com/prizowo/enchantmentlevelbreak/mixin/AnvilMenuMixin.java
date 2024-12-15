package com.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow private int repairItemCountCost;
    @Shadow private final DataSlot cost = DataSlot.standalone();

    protected AnvilMenuMixin(MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(menuType, containerId, inventory, access);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void onCreateResult(CallbackInfo ci) {
        ItemStack leftStack = this.inputSlots.getItem(0);
        ItemStack rightStack = this.inputSlots.getItem(1);

        if (!leftStack.isEmpty() && !rightStack.isEmpty()) {
            Map<Enchantment, Integer> rightEnchants = EnchantmentHelper.getEnchantments(rightStack);
            if (!rightEnchants.isEmpty()) {
                Map<Enchantment, Integer> leftEnchants = EnchantmentHelper.getEnchantments(leftStack);
                
                Map<Enchantment, Integer> resultEnchants = EnchantmentHelper.getEnchantments(leftStack);

                int totalCost = 0;
                boolean hasChanges = false;

                for (Map.Entry<Enchantment, Integer> entry : rightEnchants.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    int rightLevel = entry.getValue();
                    int leftLevel = leftEnchants.getOrDefault(enchantment, 0);

                    int newLevel = leftLevel + rightLevel;
                    
                    if (newLevel != leftLevel) {
                        hasChanges = true;
                        resultEnchants.put(enchantment, newLevel);
                        totalCost += newLevel;
                    }
                }

                if (hasChanges) {
                    ItemStack resultStack = leftStack.copy();
                    EnchantmentHelper.setEnchantments(resultEnchants, resultStack);
                    this.resultSlots.setItem(0, resultStack);

                    this.repairItemCountCost = Math.min(totalCost, 50);
                    this.cost.set(this.repairItemCountCost);
                    ci.cancel();
                }
            }
        }
    }
} 