package io.github.kurrycat.mpkmod.gui.components.UniversalInputDisplay;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.ticks.ButtonMS;
import io.github.kurrycat.mpkmod.ticks.TimingInput;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// Class to hold individual display rows
public class DisplayRow{
    private final Color tickFillColor;
    private final ButtonMS.Button button;

    public List<DisplayRow> allDisplayRows = new ArrayList<>();

    public DisplayRow(ButtonMS.Button button, Color tickFillColor){
        this.button = button;
        this.tickFillColor = tickFillColor;
    }

    public void render(UniversalInputDisplay parent, int verticalIndex, double height){
        // duplicate from UniversalInputDisplay, ah well
        List<TimingInput> inputHistory = Player.getInputHistory();

        int tickIndex = -1;
        // TODO: iterate backwards
        for (TimingInput input : inputHistory){
            tickIndex++;
            if (parent.tickIndexToRelativeX(tickIndex) <= 0) continue;

            if (getPressedFromButton(input, this.button)){
                // On ground, draw rect in background!
                Renderer2D.drawRect(
                        parent.getDisplayedPos().add(parent.tickIndexToRelativeX(tickIndex), verticalIndex * height),
                        new Vector2D(parent.TICK_WIDTH, height),
                        this.tickFillColor);
            }
            tickIndex += 1;
        }
    }

    private boolean getPressedFromButton(TimingInput timingInput, ButtonMS.Button button){
        switch (button){
            case FORWARD:
                return timingInput.W;
            case LEFT:
                return timingInput.A;
            case BACKWARD:
                return timingInput.S;
            case RIGHT:
                return timingInput.D;
            case SPRINT:
                return timingInput.P;
            case SNEAK:
                return timingInput.N;
            case JUMP:
                return timingInput.J;
            default:
                return false;
        }
    }
}
