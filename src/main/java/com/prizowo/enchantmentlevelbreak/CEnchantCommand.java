package com.prizowo.enchantmentlevelbreak;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.network.chat.Component;
import java.util.Map;

public class CEnchantCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("cenchant")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("enchantment", ResourceArgument.resource(context, Registries.ENCHANTMENT))
                        .executes(ctx -> enchantItem(ctx.getSource(),
                                ResourceArgument.getResource(ctx, "enchantment", Registries.ENCHANTMENT).value(),
                                1))
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                .executes(ctx -> enchantItem(ctx.getSource(),
                                        ResourceArgument.getResource(ctx, "enchantment", Registries.ENCHANTMENT).value(),
                                        IntegerArgumentType.getInteger(ctx, "level"))))));
    }

    private static int enchantItem(CommandSourceStack source, Enchantment enchantment, int level) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();

        if (itemStack.isEmpty()) {
            source.sendFailure(Component.literal("You must be holding an item to enchant"));
            return 0;
        }

        Map<Enchantment, Integer> currentEnchantments = EnchantmentHelper.getEnchantments(itemStack);
        
        currentEnchantments.remove(enchantment);
        
        currentEnchantments.put(enchantment, level);
        
        EnchantmentHelper.setEnchantments(currentEnchantments, itemStack);

        source.sendSuccess(
            () -> Component.literal("Applied " + enchantment.getFullname(level).getString() + " to the item"),
            true
        );

        return 1;
    }
}
