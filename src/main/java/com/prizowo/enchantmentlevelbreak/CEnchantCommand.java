package com.prizowo.enchantmentlevelbreak;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class CEnchantCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ENCHANTMENTS = (context, builder) ->
            SharedSuggestionProvider.suggestResource(Registry.ENCHANTMENT.keySet(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cenchant")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("enchantment", StringArgumentType.greedyString())
                        .suggests(SUGGEST_ENCHANTMENTS)
                        .executes(context -> enchantItem(context.getSource(),
                                StringArgumentType.getString(context, "enchantment"),
                                1))
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                .executes(context -> enchantItem(context.getSource(),
                                        StringArgumentType.getString(context, "enchantment"),
                                        IntegerArgumentType.getInteger(context, "level"))))));
    }

    private static int enchantItem(CommandSourceStack source, String enchantmentInput, int level) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();

        if (itemStack.isEmpty()) {
            source.sendFailure(Component.literal("You must be holding an item to enchant"));
            return 0;
        }

        String[] parts = enchantmentInput.split("\\s+", 2);
        String enchantmentName = parts[0];
        if (parts.length > 1) {
            try {
                level = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                // 如果第二个部分不是数字，就忽略它
            }
        }

        ResourceLocation enchantmentId;
        if (!enchantmentName.contains(":")) {
            enchantmentId = new ResourceLocation("minecraft", enchantmentName);
        } else {
            enchantmentId = new ResourceLocation(enchantmentName);
        }

        Enchantment enchantment = Registry.ENCHANTMENT.get(enchantmentId);

        if (enchantment == null) {
            source.sendFailure(Component.literal("Invalid enchantment: " + enchantmentName));
            return 0;
        }

        itemStack.enchant(enchantment, level);

        String romanLevel = Enchantmentlevelbreak.intToRoman(level);
        int finalLevel = level;
        source.sendSuccess( Component.literal("Applied " + enchantment.getFullname(finalLevel).getString() + " "  + " to the item"), true);

        return 1;
    }
}
