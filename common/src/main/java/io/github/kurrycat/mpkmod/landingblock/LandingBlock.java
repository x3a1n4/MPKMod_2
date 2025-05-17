package io.github.kurrycat.mpkmod.landingblock;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer3D;
import io.github.kurrycat.mpkmod.gui.infovars.InfoString;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Vector3D;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@InfoString.DataClass
public class LandingBlock {
    public static final int MAX_OFFSETS_SAVED = 500;
    public LandingMode landingMode = LandingMode.LAND;
    // TODO: add setter to show change in color

    @InfoString.Field
    public BoundingBox3D boundingBox;
    public boolean enabled = true;
    public boolean highlight = false; // not accessed in this file

    public List<Vector3D> offsets = new ArrayList<>();

    // Personal bests
    // Seemingly unused
    @InfoString.Field
    public Vector3D pb = null;

    @InfoString.Field
    public Vector3D pbX = null;

    @InfoString.Field
    public Vector3D pbZ = null;

    public long lastTimeOffsetSaved = 0;

    // colours
    // TODO: make changeable via config
    private static Map<LandingMode, Color> DRAW_COLORS = new HashMap<LandingMode, Color>(){{
        put(LandingMode.LAND, new Color(255, 68, 68, 157));
        put(LandingMode.HIT, new Color(255, 162, 68, 157));
        put(LandingMode.Z_NEO, new Color(255, 234, 0, 158));
        put(LandingMode.ENTER, new Color(107, 255, 74, 157));
    }};
    private static Color HIGHLIGHT_COLOUR = new Color(213, 199, 199, 157);

    public static List<LandingBlock> asLandingBlocks(List<BoundingBox3D> collisionBoundingBoxes) {
        return collisionBoundingBoxes.stream().map(LandingBlock::new).collect(Collectors.toCollection(ArrayList<LandingBlock>::new));
    }

    public LandingBlock(BoundingBox3D boundingBox) {
        this.boundingBox = boundingBox;
    }

    @InfoString.Getter
    public Vector3D getOffset() {
        if (offsets.size() == 0) return null;
        return offsets.get(offsets.size() - 1);
    }

    // partialTicks needed because of minecraft interpolation
    // see Renderer3D.java
    public void render(float partialTicks){
        if (enabled || highlight && boundingBox != null){
            Color drawColour = DRAW_COLORS.get(landingMode);

            if (highlight) drawColour = HIGHLIGHT_COLOUR;

            Renderer3D.drawBox(
                    boundingBox.expand(0.005D),
                    drawColour,
                    partialTicks
            );
        }
    }

    public Vector3D saveOffsetIfInRange() {
        if (!isTryingToLandOn()) return null;
        BoundingBox3D playerBB = landingMode.getPlayerBB();
        if (playerBB == null) return null;

        Vector3D offset = boundingBox.distanceTo(playerBB).mult(-1D);
        if (offset.getX() <= -0.3F || offset.getZ() <= -0.3F) return null;

        offsets.add(offset);
        while (offsets.size() > MAX_OFFSETS_SAVED)
            offsets.remove(0);

        if (pb == null) pb = offset;
        else if (calculateOffsetDist(offset) > calculateOffsetDist(pb)) {
            pb = offset;
        }
        if (pbX == null || offset.getX() > pbX.getX()) pbX = offset;
        if (pbZ == null || offset.getZ() > pbZ.getZ()) pbZ = offset;

        lastTimeOffsetSaved = API.tickTime;

        return offset.copy();
    }

    public boolean isTryingToLandOn() {
        if (Player.getLatest() == null) return false;

        BoundingBox3D playerBB = Player.getLatest().getBoundingBox();
        BoundingBox3D lastPlayerBB = Player.getLatest().getLastBoundingBox();

        if (landingMode != LandingMode.ENTER)
            return playerBB.minY() <= boundingBox.maxY() && lastPlayerBB.minY() > boundingBox.maxY();
        else
            return playerBB.minY() < boundingBox.maxY() && playerBB.minY() >= boundingBox.minY() && playerBB.minY() < lastPlayerBB.minY();
    }

    private double calculateOffsetDist(Vector3D offset) {
        double xSign = Math.signum(offset.getX());
        double zSign = Math.signum(offset.getZ());

        if (xSign <= 0 && zSign >= 0) {
            return offset.getX();
        } else if (xSign >= 0 && zSign <= 0) {
            return offset.getZ();
        } else if (xSign <= 0 && zSign <= 0) {
            return -offset.lengthXZ();
        } else {
            return offset.lengthXZ();
        }
    }

    @Override
    public int hashCode() {
        return this.boundingBox.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LandingBlock && this.boundingBox.equals(((LandingBlock) obj).boundingBox);
    }

    public enum LandingMode {
        LAND("Land"),
        HIT("Hit"),
        Z_NEO("Z Neo"),
        ENTER("Enter");

        public final String displayString;

        LandingMode(String displayString) {
            this.displayString = displayString;
        }

        public BoundingBox3D getPlayerBB() {
            if (Player.getLatest() == null) return null;

            switch (this) {
                case Z_NEO:
                    if (Player.getBeforeLatest() == null) return null;
                    return Player.getBeforeLatest().getLastBoundingBox();
                case HIT:
                case ENTER:
                    return Player.getLatest().getBoundingBox();
                case LAND:
                default:
                    return Player.getLatest().getLastBoundingBox();
            }
        }

        public LandingMode getNext() {
            return LandingMode.values()[(Arrays.asList(LandingMode.values()).indexOf(this) + 1) % LandingMode.values().length];
        }


        @Override
        public String toString() {
            return displayString;
        }
    }
}
