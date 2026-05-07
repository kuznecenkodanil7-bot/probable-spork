package com.moderationhelpergui.mixin;

import com.moderationhelpergui.ModerationHelperClient;
import com.moderationhelpergui.mixin.access.ChatHudAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mhg$onMiddleClick(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.inGameHud == null) return;

        Optional<String> line = ((ChatHudAccess) client.inGameHud.getChatHud()).mhg$getLineTextAt(click.x(), click.y());
        if (line.isEmpty()) {
            ModerationHelperClient.notifyClient("Сообщение чата под курсором не найдено.");
            return;
        }

        ModerationHelperClient.openPunishmentFromChatLine(line.get());
        cir.setReturnValue(true);
    }
}
