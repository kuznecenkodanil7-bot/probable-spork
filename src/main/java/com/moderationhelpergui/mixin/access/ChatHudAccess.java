package com.moderationhelpergui.mixin.access;

import java.util.Optional;

public interface ChatHudAccess {
    Optional<String> mhg$getLineTextAt(double mouseX, double mouseY);
}
