package cn.autoforged.multi_function_key_mod_1781840324;

import cn.autoforged.multi_function_key_mod_1781840324.config.ModConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;

@Mod(MainMod.MODID)
public class MainMod {
    public static final String MODID = "multi_function_key_mod_1781840324";

    public MainMod(IEventBus modEventBus) {
        ModLoadingContext.get().getActiveContainer().registerConfig(Type.CLIENT, ModConfig.SPEC);
    }
}
