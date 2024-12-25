package com.prizowo.enchantmentlevelbreak;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.network.chat.Component;
import java.util.Map;

public class CEnchantCommand {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ENCHANTMENTS = (context, builder) ->
            SharedSuggestionProvider.suggestResource(Registry.ENCHANTMENT.keySet(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cenchant")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("enchantment", ResourceLocationArgument.id())
                        .suggests(SUGGEST_ENCHANTMENTS)
                        .executes(context -> enchantItem(context.getSource(),
                                Registry.ENCHANTMENT.get(ResourceLocationArgument.getId(context, "enchantment")),
                                1))
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                .executes(context -> enchantItem(context.getSource(),
                                        Registry.ENCHANTMENT.get(ResourceLocationArgument.getId(context, "enchantment")),
                                        IntegerArgumentType.getInteger(context, "level"))))));
    }

    private static int enchantItem(CommandSourceStack source, Enchantment enchantment, int level) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();

        if (itemStack.isEmpty()) {
            source.sendFailure(Component.literal("You must be holding an item to enchant"));
            return 0;
        }

        if (enchantment == null) {
            source.sendFailure(Component.literal("Invalid enchantment"));
            return 0;
        }

        Map<Enchantment, Integer> currentEnchantments = EnchantmentHelper.getEnchantments(itemStack);
        
        currentEnchantments.remove(enchantment);
        
        currentEnchantments.put(enchantment, level);
        
        EnchantmentHelper.setEnchantments(currentEnchantments, itemStack);

        source.sendSuccess(Component.literal("Applied " + enchantment.getFullname(level).getString() + " to the item"), true);

        return 1;
    }
}
