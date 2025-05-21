package io.github.kurrycat.mpkmod.gui.components.UniversalInputDisplay;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.NumberSlider;
import io.github.kurrycat.mpkmod.gui.components.ResizableComponent;
import io.github.kurrycat.mpkmod.gui.components.TextCheckButton;
import io.github.kurrycat.mpkmod.ticks.ButtonMS;
import io.github.kurrycat.mpkmod.ticks.TimingInput;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.util.Arrays;
import java.util.List;

import java.awt.*;

public class UniversalInputDisplay extends ResizableComponent {
    /*
    The purpose of this class is to display Tick Inputs, Real Inputs, and on-ground info
    */

    // copied from InputHistory
    @JsonProperty
    public boolean transparentBackground = false;
    @JsonProperty
    public Color backgroundColor = new Color(100, 100, 100, 40);
    @JsonProperty
    public Color edgeColor = new Color(100, 100, 100, 19);
    @JsonProperty
    public Color tickLineColor = new Color(100, 100, 100, 50);
    public Color selectedColor = new Color(255, 170, 0, 100);

    @JsonProperty
    public double TICK_WIDTH = 0.1;

    public static final long NANOS_PER_TICK = (long) 50e6;

    public List<DisplayRow> displayRows;

    // get inputs
    // get ticks
    public UniversalInputDisplay(){
        // Note: this is the order they are rendered in top-to-bottom
        displayRows = Arrays.asList(
                new DisplayRow(ButtonMS.Button.FORWARD,     new Color(250, 116, 116, 100)),
                new DisplayRow(ButtonMS.Button.LEFT,        new Color(214, 134, 236, 100)),
                new DisplayRow(ButtonMS.Button.BACKWARD,    new Color(113, 220, 200, 100)),
                new DisplayRow(ButtonMS.Button.RIGHT,       new Color(255, 239, 38, 100)),
                new DisplayRow(ButtonMS.Button.SPRINT,      new Color(121, 122, 246, 100)),
                new DisplayRow(ButtonMS.Button.SNEAK,       new Color(140, 209, 85, 100)),
                new DisplayRow(ButtonMS.Button.JUMP,        new Color(225, 169, 104, 100))
                );

    }

    // From BarrierDisplayComponent
    @Override
    public io.github.kurrycat.mpkmod.gui.components.PopupMenu getPopupMenu() {
        io.github.kurrycat.mpkmod.gui.components.PopupMenu menu = new io.github.kurrycat.mpkmod.gui.components.PopupMenu();
        menu.addComponent(
                new NumberSlider(0.1,
                        10,
                        0.1, TICK_WIDTH, Vector2D.OFFSCREEN, new Vector2D(56, 11), sliderValue -> {
                    TICK_WIDTH = (int) sliderValue;
                })
        );
        menu.addComponent(
                new Button("Delete", mouseButton -> {
                    if (Mouse.Button.LEFT.equals(mouseButton)) {
                        menu.paneHolder.removeComponent(this);
                        menu.close();
                    }
                })
        );
        return menu;
    }

    @JsonProperty
    public Color onGroundColour = new Color(255, 255, 255, 50);

    private long renderTime;

    @Override
    public void render(Vector2D mouse) {
        // see InputHistory.java
        // Render base component
        if (!transparentBackground)
            Renderer2D.drawRectWithEdge(getDisplayedPos(), getDisplayedSize(), 1, selected ? selectedColor : backgroundColor, edgeColor);
        renderHoverEdges(mouse);

        // Get input history
        List<TimingInput> inputHistory = Player.getInputHistory();
        // Get current time, in nanos
        renderTime = System.nanoTime();
        // Get tick times, in nanos

        // Render tickLines
        for (int tickIndex = Player.maxSavedTicks; tickIndexToRelativeX(tickIndex) > 0; tickIndex--){
            // add slight buffer
            Renderer2D.drawLine(
                    pos.add(tickIndexToRelativeX(tickIndex), -1),
                    pos.add(tickIndexToRelativeX(tickIndex), size.getY() + 1),
                    tickLineColor);
        }

        // Render ground (G)
        // Note: render left to right

        int tickIndex = -1;
        // TODO: iterate backwards
        for (TimingInput input : inputHistory){
            tickIndex++;

            if (tickIndexToRelativeX(tickIndex) <= 0) continue;
            if (input == null) continue;

            if (input.G){
                // On ground, draw rect in background!
                // TODO: make it look nicer
                Renderer2D.drawRect(pos.add(tickIndexToRelativeX(tickIndex), 0), new Vector2D(TICK_WIDTH, size.getY()), onGroundColour);
            }
        }

        // Render rows
        int drIndex = 0;
        double rowHeight = size.getY() / displayRows.size();
        for (DisplayRow dr : displayRows){
            dr.render(this, drIndex, rowHeight);
            drIndex++;
        }

        // API.LOGGER.info("inputHistory: {}", Player.getInputHistory()); // Holds inputs, snapped to ticks

    }

    public int nanoToRelativeX(long nanos){
        int out = (int) (size.getX() - (double) (renderTime - nanos) / NANOS_PER_TICK * TICK_WIDTH);
        API.LOGGER.info("out: {}", out); // Holds inputs, snapped to ticks
        return Math.max(out, 0); // limit to 0
    }

    private static long lastTickNano;
    public int tickIndexToRelativeX(int tickIndex){
        // Note: index is backwards!
        int out = nanoToRelativeX(lastTickNano - NANOS_PER_TICK * (Player.maxSavedTicks - tickIndex));
        API.LOGGER.info("in: {}, out: {}", tickIndex, out); // Holds inputs, snapped to ticks
        return out;
    }

    public static void onTickEnd(){
        lastTickNano = System.nanoTime();
    }

}

