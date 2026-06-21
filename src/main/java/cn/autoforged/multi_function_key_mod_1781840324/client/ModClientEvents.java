package cn.autoforged.multi_function_key_mod_1781840324.client;

import cn.autoforged.multi_function_key_mod_1781840324.MainMod;
import cn.autoforged.multi_function_key_mod_1781840324.client.screen.MulkScreen;
import cn.autoforged.multi_function_key_mod_1781840324.config.ModConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;
import java.lang.reflect.Field;

@EventBusSubscriber(modid = MainMod.MODID, value = Dist.CLIENT)
public class ModClientEvents {
    public static final KeyMapping MULK_KEY = new KeyMapping(
            "key.mulk",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_CONTROL,
            "key.categories.multifunction"
    );

    private static boolean mulkWasDown = false;
    private static boolean otherKeyPressed = false;

    private static final Field CLICK_COUNT;

    static {
        Field f;
        try {
            f = KeyMapping.class.getDeclaredField("clickCount");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            f = null;
        }
        CLICK_COUNT = f;
    }

    public static void incrementClickCount(KeyMapping mapping) {
        if (CLICK_COUNT == null) return;
        try {
            CLICK_COUNT.setInt(mapping, CLICK_COUNT.getInt(mapping) + 1);
        } catch (IllegalAccessException ignored) {
        }
    }

    private static void resetBoundActionsClickCount(Minecraft mc) {
        if (CLICK_COUNT == null) return;
        for (String name : ModConfig.CLIENT.boundActions.get()) {
            for (KeyMapping km : mc.options.keyMappings) {
                if (km.getName().equals(name)) {
                    try {
                        CLICK_COUNT.setInt(km, 0);
                    } catch (IllegalAccessException ignored) {
                    }
                    break;
                }
            }
        }
    }

    private static InputConstants.Key getMulkKey() {
        return MULK_KEY.getKey();
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        InputConstants.Key mulk = getMulkKey();
        if (mulk == null) return;

        if (mulk.getType() == InputConstants.Type.KEYSYM) {
            handleKeyEvent(event, mc, mulk);
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        InputConstants.Key mulk = getMulkKey();
        if (mulk == null) return;

        if (mulk.getType() == InputConstants.Type.MOUSE) {
            if (event.getButton() == mulk.getValue()) {
                if (event.getAction() == GLFW.GLFW_PRESS) {
                    mulkWasDown = true;
                    otherKeyPressed = false;
                    resetBoundActionsClickCount(mc);
                    event.setCanceled(true);
                } else if (event.getAction() == GLFW.GLFW_RELEASE && mulkWasDown) {
                    mulkWasDown = false;
                    if (!otherKeyPressed) {
                        mc.setScreen(new MulkScreen());
                    }
                }
            } else if (mulkWasDown && event.getAction() == GLFW.GLFW_PRESS) {
                otherKeyPressed = true;
            }
        }
    }

    private static void handleKeyEvent(InputEvent.Key event, Minecraft mc, InputConstants.Key mulk) {
        InputConstants.Key eventKey = InputConstants.getKey(event.getKey(), event.getScanCode());

        if (eventKey.equals(mulk)) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                mulkWasDown = true;
                otherKeyPressed = false;
                resetBoundActionsClickCount(mc);
            } else if (event.getAction() == GLFW.GLFW_RELEASE && mulkWasDown) {
                mulkWasDown = false;
                if (!otherKeyPressed) {
                    mc.setScreen(new MulkScreen());
                }
            }
        } else if (mulkWasDown && event.getAction() == GLFW.GLFW_PRESS) {
            otherKeyPressed = true;
        }
    }
}
