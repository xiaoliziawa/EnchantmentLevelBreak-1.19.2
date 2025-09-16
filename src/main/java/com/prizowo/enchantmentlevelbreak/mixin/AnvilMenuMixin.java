package com.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import com.prizowo.enchantmentlevelbreak.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.HashMap;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow private int repairItemCountCost;
    @Shadow @Final private DataSlot cost;

    @Unique
    private static final ThreadLocal<Boolean> IS_PROCESSING = ThreadLocal.withInitial(() -> false);

    protected AnvilMenuMixin(MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(menuType, containerId, inventory, access);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void onCreateResult(CallbackInfo ci) {
        if (IS_PROCESSING.get()) {
            return;
        }

        try {
            IS_PROCESSING.set(true);
            ItemStack left = this.inputSlots.getItem(0);
            ItemStack right = this.inputSlots.getItem(1);

            if (!left.isEmpty() && !right.isEmpty()) {
                handleAnvilOperation(left, right, ci);
            }
        } finally {
            IS_PROCESSING.set(false);
        }
    }

    @Unique
    private void handleAnvilOperation(ItemStack left, ItemStack right, CallbackInfo ci) {
        Map<Enchantment, Integer> leftEnchants = EnchantmentHelper.getEnchantments(left);
        Map<Enchantment, Integer> rightEnchants = EnchantmentHelper.getEnchantments(right);

        if (left.getItem() == right.getItem()) {
            if (!leftEnchants.isEmpty() || !rightEnchants.isEmpty()) {
                handleEnchantmentMerge(left, leftEnchants, rightEnchants, true, ci);
            }
            return;
        }

        if (!rightEnchants.isEmpty() && isEnchantedBook(right)) {
            handleEnchantmentMerge(left, leftEnchants, rightEnchants, false, ci);
        }
    }

    @Unique
    private boolean isEnchantedBook(ItemStack stack) {
        return stack.getItem() == Items.ENCHANTED_BOOK;
    }

    @Unique
    private void handleEnchantmentMerge(ItemStack target, Map<Enchantment, Integer> leftEnchants, Map<Enchantment, Integer> rightEnchants, boolean isSameItemMerge, CallbackInfo ci) {
        // If both stacking options are disabled, let vanilla handle it

        if (!Config.allowLevelStacking && !Config.allowVanillaLevelStacking && !Config.allowAnyEnchantment) {
            return;
        }

        Map<Enchantment, Integer> resultEnchants = new HashMap<>(leftEnchants);
        int totalCost = 0;
        boolean anyEnchantmentApplied = false;

        for (Map.Entry<Enchantment, Integer> entry : rightEnchants.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int rightLevel = entry.getValue();
            boolean canApply = isSameItemMerge || Config.allowAnyEnchantment || enchantment.canEnchant(target);
            if (canApply) {
                int leftLevel = resultEnchants.getOrDefault(enchantment, 0);
                int newLevel = calculateNewLevel(leftLevel, rightLevel);
                newLevel = Math.min(newLevel, Config.maxEnchantmentLevel);

                resultEnchants.put(enchantment, newLevel);
                totalCost += newLevel;
                anyEnchantmentApplied = true;
            }
        }

        if (anyEnchantmentApplied) {
            applyResult(target, resultEnchants, totalCost);
            ci.cancel();
        }
    }

    @Unique
    private int calculateNewLevel(int leftLevel, int rightLevel) {
        if (Config.allowLevelStacking) {
            return leftLevel + rightLevel;
        } else if (Config.allowVanillaLevelStacking && leftLevel == rightLevel) {
            return leftLevel + 1;
        } else {
            return Math.max(leftLevel, rightLevel);
        }
    }

    @Unique
    private void applyResult(ItemStack target, Map<Enchantment, Integer> enchantments, int totalCost) {
        ItemStack result = target.copy();
        EnchantmentHelper.setEnchantments(enchantments, result);
        this.resultSlots.setItem(0, result);

        this.repairItemCountCost = Math.min(totalCost, 50);
        this.cost.set(this.repairItemCountCost);
    }
} 
