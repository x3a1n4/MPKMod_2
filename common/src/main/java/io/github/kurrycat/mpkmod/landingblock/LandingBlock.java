package io.github.kurrycat.mpkmod.landingblock;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer3D;
import io.github.kurrycat.mpkmod.gui.infovars.InfoString;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import jdk.nashorn.internal.objects.annotations.Setter;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.kurrycat.mpkmod.Main.mainGUI;

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
        put(LandingMode.OBSTACLE, new Color(74, 213, 255, 157));
        put(LandingMode.JUMP, new Color(74, 89, 255, 157));
    }};
    private static Color HIGHLIGHT_COLOUR = new Color(213, 199, 199, 157);

    // TODO: make json property
    @JsonProperty
    public static Color messageBackgroundColor = new Color(31, 31, 31, 47);
    @JsonProperty
    public static Color messageHighlightBackgroundColor = new Color(98, 255, 74, 157);
    @JsonProperty
    public static Color messageHighlightJumpColor = new Color(74, 125, 255, 157);
    @JsonProperty
    public static Color messageHighlightObstXColor = new Color(255, 240, 74, 157);
    @JsonProperty
    public static Color messageHighlightObstZColor = new Color(189, 74, 255, 157);

    private static final double RENDER_EXPANSION_AMOUNT = 0.005D;

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
                    boundingBox.expand(RENDER_EXPANSION_AMOUNT),
                    drawColour,
                    partialTicks
            );
        }
    }

    public void saveAndPostOffsetIfInRange() {
        // note: must be called before getPlayerBB
        if (!isTryingToLandOn()) return; // if not trying to land on, return null

        /*
        BoundingBox3D playerBB = landingMode.getPlayerBB(this); // get the player BB
        if (playerBB == null) return null; // if null, return null

        Vector3D offset = boundingBox.distanceTo(playerBB).mult(-1D); // get the distance, mult -1 as positive should mean landing
        */
        Vector3D offset = landingMode.getOffset(boundingBox).mult(-1D);
        if (offset.getX() <= -0.3F || offset.getZ() <= -0.3F) return; // if too far away, stop

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

        // get highlight color
        Color highlightColor = new Color(0, 0, 0, 255);
        if (offset.getX() > 0 && offset.getZ() > 0){
            highlightColor = messageHighlightBackgroundColor;

            if (landingMode == LandingMode.JUMP) highlightColor = messageHighlightJumpColor;
        }else{
            highlightColor = messageBackgroundColor;
        }

        if (landingMode == LandingMode.OBSTACLE){
            if (landingMode.isZFacing) {highlightColor = messageHighlightObstZColor;}
            else {highlightColor = messageHighlightObstXColor;}
        }

        // Post the message
        if (mainGUI != null)
            // This is the only time a message is posted
            mainGUI.postMessage(
                    "offset",
                    MathUtil.formatDecimals(offset.getX(), 5, false) +
                            ", " + MathUtil.formatDecimals(offset.getZ(), 5, false),
                    highlightColor
            ); // show input
    }

    public boolean isTryingToLandOn() {
        if (Player.getLatest() == null) return false;

        BoundingBox3D playerBB = Player.getLatest().getBoundingBox();
        BoundingBox3D lastPlayerBB = Player.getLatest().getLastBoundingBox();
        BoundingBox3D lastLastPlayerBB = Player.getBeforeLatest().getLastBoundingBox();

        // this is where the logic happens, so
        switch (landingMode) {
            case LAND:
            case HIT:
            case Z_NEO:
                // note < is due to the bounding box being 0.005 bigger
                return playerBB.minY() <= boundingBox.maxY() && lastPlayerBB.minY() > boundingBox.maxY();
            case ENTER:
                // default, return true when going downwards
                // key difference is minY should be greater than bb minY
                return playerBB.minY() < boundingBox.maxY() && playerBB.minY() >= boundingBox.minY() && playerBB.minY() < lastPlayerBB.minY();
            case JUMP:
                // return true on tick where jump is pressed and on ground
                // TODO: maybe tick after?
                Player.KeyInput keyInput = Player.getLatest().keyInput;
                boolean onGround = Objects.requireNonNull(Player.getBeforeLatest()).onGround;

                // Use the rendering expansion amount as an epsilon
                boolean onBlock = lastPlayerBB.minY() <= boundingBox.maxY() && lastPlayerBB.minY() > (boundingBox.maxY() - 2 * RENDER_EXPANSION_AMOUNT);

                // API.LOGGER.info(API.DISCORD_RPC_MARKER, String.format("Is jumping: %s %s %s", keyInput.jump , onGround , onBlock));
                return keyInput.jump && onGround && onBlock;
                // TODO: highlight in log to represent a jump, not a landing
            case OBSTACLE:
                // TODO: highlight in log to represent a miss
                // We only return true on "tight" passes
                // step 1: get "sides" to bounding box
                Vector2D side = boundingBox.getXZSide(playerBB);
                Vector2D lastSide = boundingBox.getXZSide(lastPlayerBB);
                Vector2D lastLastSide = boundingBox.getXZSide(lastLastPlayerBB);

                // X facing check
                // flip lastSide to -, + corner (so that we are going around the bottom right
                Vector2D transform = new Vector2D(1, 1);
                if (lastSide.getX() > 0) {
                    side.setX(side.getX() * -1);
                    lastSide.setX(lastSide.getX() * -1);
                    lastLastSide.setX(lastLastSide.getX() * -1);
                    transform.setX(-1);
                }
                if (lastSide.getY() < 0) {
                    side.setY(side.getY() * -1);
                    lastSide.setY(lastSide.getY() * -1);
                    lastLastSide.setY(lastLastSide.getY() * -1);
                    transform.setY(-1);
                }

                // now check X
                if (side.getX() > lastSide.getX() && lastSide.getY() > lastLastSide.getY()) {
                    landingMode.setZFacing(false);
                    return true;
                }

                // and check Z
                if (side.getX() != lastSide.getX() && side.getY() != lastSide.getY()) {
                    landingMode.setZFacing(true);
                    return true;
                }

                // TODO: what if both true?

                return false;
            default:
                return false;
        }

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
        ENTER("Enter"),
        OBSTACLE("Obst"),
        JUMP("Jump");

        private boolean isZFacing;
        @Setter
        public void setZFacing(boolean isZFacing){this.isZFacing = isZFacing;}

        public final String displayString;

        LandingMode(String displayString) {
            this.displayString = displayString;
        }

        public Vector3D getOffset(BoundingBox3D boundingBox) {
            if (Player.getLatest() == null) return null;

            switch (this) {
                case OBSTACLE:
                    if (isZFacing) {
                        // return distance comprising this tick and last tick
                        return new Vector3D(
                                boundingBox.distanceTo(Player.getLatest().getBoundingBox()).getX(),
                                0,
                                boundingBox.distanceTo(Player.getLatest().getLastBoundingBox()).getZ()
                        );
                    } else {
                        /*
                        API.LOGGER.info(API.DISCORD_RPC_MARKER, String.format(
                                "X misses: %s %s %s",
                                boundingBox.distanceTo(Player.getLatest().getBoundingBox()),
                                boundingBox.distanceTo(Player.getLatest().getLastBoundingBox()),
                                boundingBox.distanceTo(Objects.requireNonNull(Player.getBeforeLatest()).getLastBoundingBox())));
                         */
                        return boundingBox.distanceTo(Player.getLatest().getLastBoundingBox()); // x facing, get the last bounding box
                    }
                case Z_NEO:
                case JUMP:
                    if (Player.getBeforeLatest() == null) return null;
                    return boundingBox.distanceTo(Player.getBeforeLatest().getLastBoundingBox());
                case HIT:
                case ENTER:
                    return boundingBox.distanceTo(Player.getLatest().getBoundingBox());
                case LAND:
                default:
                    return boundingBox.distanceTo(Player.getLatest().getLastBoundingBox());
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
