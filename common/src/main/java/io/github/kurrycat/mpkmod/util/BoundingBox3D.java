package io.github.kurrycat.mpkmod.util;

import io.github.kurrycat.mpkmod.gui.infovars.InfoString;

@InfoString.DataClass
public class BoundingBox3D implements FormatDecimals {
    public static final BoundingBox3D ZERO = new BoundingBox3D(Vector3D.ZERO, Vector3D.ZERO);
    private Vector3D min, max;

    public BoundingBox3D(Vector3D corner1, Vector3D corner2) {
        this.min = new Vector3D(
                Math.min(corner1.getX(), corner2.getX()),
                Math.min(corner1.getY(), corner2.getY()),
                Math.min(corner1.getZ(), corner2.getZ())
        );
        this.max = new Vector3D(
                Math.max(corner1.getX(), corner2.getX()),
                Math.max(corner1.getY(), corner2.getY()),
                Math.max(corner1.getZ(), corner2.getZ())
        );
    }

    public static BoundingBox3D asBlockPos(Vector3D pos) {
        return new BoundingBox3D(
                pos.floor(),
                pos.floor().add(1)
        );
    }

    @InfoString.Getter
    public Vector3D getSize() {
        return max.sub(min);
    }

    public BoundingBox3D setMinX(double minX) {
        this.min.setX(minX);
        return this;
    }

    public BoundingBox3D setMinY(double minY) {
        this.min.setY(minY);
        return this;
    }

    public BoundingBox3D setMinZ(double minZ) {
        this.min.setZ(minZ);
        return this;
    }

    public BoundingBox3D setMaxX(double maxX) {
        this.max.setX(maxX);
        return this;
    }

    public BoundingBox3D setMaxY(double maxY) {
        this.max.setY(maxY);
        return this;
    }

    public BoundingBox3D setMaxZ(double maxZ) {
        this.max.setZ(maxZ);
        return this;
    }

    public boolean intersectsOrTouchesXZ(BoundingBox3D other) {
        return other.maxX() >= this.minX() &&
                other.minX() <= this.maxX() &&
                other.maxZ() >= this.minZ() &&
                other.minZ() <= this.maxZ();
    }

    public double maxX() {
        return max.getX();
    }

    public double minX() {
        return min.getX();
    }

    public double maxZ() {
        return max.getZ();
    }

    public double minZ() {
        return min.getZ();
    }

    public Vector3D distanceTo(BoundingBox3D other) {
        return new Vector3D(
                this.midX() > other.midX() ? this.minX() - other.maxX() : other.minX() - this.maxX(),
                this.midY() > other.midY() ? this.minY() - other.maxY() : other.minY() - this.maxY(),
                this.midZ() > other.midZ() ? this.minZ() - other.maxZ() : other.minZ() - this.maxZ()
        );
    }

    public Vector2D getXZSide(BoundingBox3D other) {
        return new Vector2D(
                other.minX() >= this.maxX() ? 1 : other.maxX() <= this.minX() ? -1 : 0,
                other.minZ() >= this.maxZ() ? 1 : other.maxZ() <= this.minZ() ? -1 : 0
        );
    }

    public double midX() {
        return (minX() + maxX()) / 2D;
    }

    public double midY() {
        return (minY() + maxY()) / 2D;
    }

    public double minY() {
        return min.getY();
    }

    public double maxY() {
        return max.getY();
    }

    public double midZ() {
        return (minZ() + maxZ()) / 2D;
    }

    public BoundingBox3D expand(double amount) {
        return new BoundingBox3D(
                this.min.sub(amount),
                this.max.add(amount)
        );
    }

    @Override
    public int hashCode() {
        return this.min.hashCode() + this.max.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BoundingBox3D && this.min.equals(((BoundingBox3D) obj).min) && this.max.equals(((BoundingBox3D) obj).max);
    }

    @Override
    public String toString() {
        return "BoundingBox3D{" + min + " - " + max + "}";
    }

    public BoundingBox3D move(Vector3D v) {
        return move(v.getX(), v.getY(), v.getZ());
    }

    public BoundingBox3D move(double x, double y, double z) {
        return new BoundingBox3D(getMin().add(x, y, z), getMax().add(x, y, z));
    }

    @InfoString.Getter
    public Vector3D getMin() {
        return min;
    }

    @InfoString.Getter
    public Vector3D getMax() {
        return max;
    }

    @InfoString.Getter
    public Vector3D getMid() {
        return min.add(max).mult(0.5D);
    }

    @Override
    public String formatDecimals(int decimals, boolean keepZeros) {
        return "[" + min.formatDecimals(decimals, keepZeros) + " - " + max.formatDecimals(decimals, keepZeros) + "]";
    }
}
