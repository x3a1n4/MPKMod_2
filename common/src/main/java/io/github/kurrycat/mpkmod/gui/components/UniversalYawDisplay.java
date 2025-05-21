package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class UniversalYawDisplay extends ResizableComponent{
    /*
    The purpose of this class is to display horizontal mouse movement, and the angle on each tick
    */

    // copied from InputHistory
    @JsonProperty
    public boolean transparentBackground = false;
    @JsonProperty
    public Color backgroundColor = new Color(100, 100, 100, 40);
    @JsonProperty
    public Color edgeColor = new Color(100, 100, 100, 50);
    public Color selectedColor = new Color(255, 170, 0, 100);

    // Todo: use Main.mouseMovements
    @Override
    public void render(Vector2D mouse) {
        // see InputHistory.java
        if (!transparentBackground)
            Renderer2D.drawRectWithEdge(getDisplayedPos(), getDisplayedSize(), 1, selected ? selectedColor : backgroundColor, edgeColor);

        renderHoverEdges(mouse);
    }
}
