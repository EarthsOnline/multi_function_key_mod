package cn.autoforged.multi_function_key_mod_1781840324.client;

import cn.autoforged.multi_function_key_mod_1781840324.MainMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = MainMod.MODID, value = Dist.CLIENT)
public class ModKeyMappingRegistration {
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModClientEvents.MULK_KEY);
    }
}
