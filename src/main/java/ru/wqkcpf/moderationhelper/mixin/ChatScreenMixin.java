package ru.wqkcpf.moderationhelper.mixin;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.wqkcpf.moderationhelper.ModerationHelperClient;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void moderationhelper$middleClickNickname(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            return;
        }

        String line = ModerationHelperClient.latestTrackedLineAt(click.x(), click.y());
        if (line.isBlank()) {
            return;
        }

        if (ModerationHelperClient.handleChatMiddleClick(line)) {
            cir.setReturnValue(true);
        }
    }
}
