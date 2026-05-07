package com.moderationhelpergui.keybind;

import com.moderationhelpergui.ModerationHelperClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class KeybindManager {
    private static KeyBinding openStatsKey;
    private static KeyBinding stopObsKey;

    private KeybindManager() {}

    public static void register() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(ModerationHelperClient.MOD_ID, "main"));

        openStatsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.moderation_helper_gui.open_stats",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                category
        ));

        stopObsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.moderation_helper_gui.stop_obs",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(KeybindManager::tick);
    }

    private static void tick(MinecraftClient client) {
        while (openStatsKey.wasPressed()) {
            ModerationHelperClient.openStatsScreen();
        }

        while (stopObsKey.wasPressed()) {
            // Важно: если открыт чат, G не должна останавливать запись OBS.
            if (client.currentScreen instanceof ChatScreen) {
                continue;
            }
            ModerationHelperClient.stopObsRecording("Запись OBS остановлена клавишей G.");
        }
    }
}
