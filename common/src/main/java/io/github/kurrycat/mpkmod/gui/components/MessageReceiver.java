package io.github.kurrycat.mpkmod.gui.components;

import java.awt.*;

public interface MessageReceiver {
    void postMessage(String receiverID, String content, Color highlightColor);
}
