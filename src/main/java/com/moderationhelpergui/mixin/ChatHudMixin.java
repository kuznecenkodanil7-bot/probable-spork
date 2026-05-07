package com.moderationhelpergui.mixin;

import com.moderationhelpergui.mixin.access.ChatHudAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin implements ChatHudAccess {
    @Shadow @Final private List<ChatHudLine.Visible> visibleMessages;
    @Shadow private int scrolledLines;

    @Shadow private int getWidth() { throw new AssertionError(); }
    @Shadow private double getChatScale() { throw new AssertionError(); }
    @Shadow private int getLineHeight() { throw new AssertionError(); }

    @Override
    public Optional<String> mhg$getLineTextAt(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return Optional.empty();

        double scale = getChatScale();
        if (scale <= 0.0D) return Optional.empty();

        double chatX = Math.floor((mouseX - 4.0D) / scale);
        double chatY = Math.floor((client.getWindow().getScaledHeight() - mouseY - 40.0D) / scale);
        if (chatX < 0.0D || chatX > getWidth() || chatY < 0.0D) return Optional.empty();

        int lineIndex = (int) (chatY / Math.max(1, getLineHeight())) + scrolledLines;

        if (lineIndex < 0 || lineIndex >= visibleMessages.size()) return Optional.empty();
        ChatHudLine.Visible line = visibleMessages.get(lineIndex);
        return Optional.of(orderedTextToString(line.content()));
    }

    private String orderedTextToString(net.minecraft.text.OrderedText orderedText) {
        StringBuilder builder = new StringBuilder();
        orderedText.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });
        return builder.toString();
    }
}
