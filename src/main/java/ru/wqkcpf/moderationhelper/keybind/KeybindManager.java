package ru.wqkcpf.moderationhelper.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import ru.wqkcpf.moderationhelper.ModerationHelperClient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class KeybindManager {
    private KeyBinding openStatsKey;
    private KeyBinding stopObsKey;

    public KeybindManager() {
    }

    public void register() {
        KeyBinding.Category category = KeyBinding.Category.create(
                Identifier.of(ModerationHelperClient.MOD_ID, "main")
        );

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
    }

    public void tick(MinecraftClient client) {
        if (openStatsKey != null) {
            while (openStatsKey.wasPressed()) {
                ModerationHelperClient.openStatsScreen();
            }
        }

        if (stopObsKey != null) {
            while (stopObsKey.wasPressed()) {
                // Если открыт чат, G НЕ должен останавливать OBS.
                if (client.currentScreen instanceof ChatScreen) {
                    continue;
                }

                stopObsSafely(client);
            }
        }
    }

    private void stopObsSafely(MinecraftClient client) {
        try {
            /*
             * Вариант 1:
             * если в ModerationHelperClient есть метод stopObsRecording(String),
             * вызываем его.
             */
            try {
                Method method = ModerationHelperClient.class.getMethod("stopObsRecording", String.class);
                method.invoke(null, "Запись OBS остановлена клавишей G.");
                return;
            } catch (NoSuchMethodException ignored) {
                // Если такого метода нет, идём ниже.
            }

            /*
             * Вариант 2:
             * ищем public static поле OBS и вызываем stopRecording().
             */
            Field obsField = ModerationHelperClient.class.getField("OBS");
            Object obs = obsField.get(null);

            if (obs != null) {
                Method stopMethod = obs.getClass().getMethod("stopRecording");
                stopMethod.invoke(obs);
                showMessage(client, "Запись OBS остановлена клавишей G.");
            }
        } catch (Exception e) {
            showMessage(client, "Не удалось остановить OBS. Проверь конфиг OBS.");
        }
    }

    private void showMessage(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[MHG] " + message), false);
        }
    }
}
