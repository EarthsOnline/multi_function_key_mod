package cn.autoforged.multi_function_key_mod_1781840324.client;

import cn.autoforged.multi_function_key_mod_1781840324.MainMod;
import cn.autoforged.multi_function_key_mod_1781840324.client.screen.MulkScreen;
import cn.autoforged.multi_function_key_mod_1781840324.config.ModConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = MainMod.MODID, value = Dist.CLIENT)
public class ModClientEvents {
    private static boolean mulkWasDown = false;
    private static boolean otherKeyPressed = false;
    private static InputConstants.Key mulkKey = null;

    private static InputConstants.Key getMulkKey() {
        if (mulkKey == null) {
            String name = ModConfig.CLIENT.mulkKeyName.get();
            mulkKey = InputConstants.getKey(name);
        }
        return mulkKey;
    }

    public static void resetMulkKey() {
        mulkKey = null;
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
