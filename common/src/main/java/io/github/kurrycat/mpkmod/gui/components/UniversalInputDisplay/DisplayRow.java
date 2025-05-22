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

        for (int tickIndex = inputHistory.size() - 1; parent.tickIndexToRelativeX(tickIndex) > 0; tickIndex--){
            if (tickIndex == 0) break;
            if (getPressedFromButton(inputHistory.get(tickIndex), this.button)){
                // TODO: make it look nicer
                Renderer2D.drawRect(
                        parent.getDisplayedPos().add(parent.tickIndexToRelativeX(tickIndex), verticalIndex * height),
                        new Vector2D(parent.TICK_WIDTH, height),
                        this.tickFillColor);
            }
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
