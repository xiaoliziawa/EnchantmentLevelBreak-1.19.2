package com.prizowo.enchantmentlevelbreak;

import com.prizowo.enchantmentlevelbreak.config.Config;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod("enchantmentlevelbreak")
public class Enchantmentlevelbreak {
    public static final String MODID = "enchantmentlevelbreak";

    public Enchantmentlevelbreak() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CEnchantCommand.register(event.getDispatcher());
    }
}
