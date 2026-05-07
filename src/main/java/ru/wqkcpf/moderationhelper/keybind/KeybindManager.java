package ru.wqkcpf.moderationhelper.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import ru.wqkcpf.moderationhelper.ModerationHelperClient;

public class KeybindManager {
    private KeyBinding statsKey;
    private KeyBinding stopObsKey;

    public void register() {
        statsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.moderation_helper_gui.open_stats",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.moderation_helper_gui"
        ));

        stopObsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.moderation_helper_gui.stop_obs",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.moderation_helper_gui"
        ));
    }

    public void tick(MinecraftClient client) {
        while (statsKey.wasPressed()) {
            ModerationHelperClient.openStatsScreen();
        }

        while (stopObsKey.wasPressed()) {
            if (ModerationHelperClient.shouldIgnoreStopKeyBecauseChatOpen(client)) {
                continue;
            }
            ModerationHelperClient.stopCheckRecording("клавиша G");
        }
    }
}
