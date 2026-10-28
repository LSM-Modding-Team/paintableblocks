package net.nicomar.paintableblocks.balance;

import java.util.List;

/** Shared gameplay units. No renderer, menu or recipe owns a second copy. */
public final class PaintingBalance {
    public static final List<Integer> RESOLUTIONS = List.of(1, 2, 4, 8, 16);
    public static final int PAINTS = 337;
    public static final int UNITS_PER_PAINT_ITEM = 256;
    public static final float HARDNESS = 1.5F;
    public static final float BLAST_RESISTANCE = 6.0F;
    public static final int MACHINE_TICKS = 80;
    private PaintingBalance() {}
    public static int requireResolution(int n) {
        if (!RESOLUTIONS.contains(n)) throw new IllegalArgumentException("Unsupported resolution: " + n);
        return n;
    }
    public static int surfaceUnits(int n) { requireResolution(n); return 256 / (n * n); }
    public enum Material {
        COBBLESTONE(1, 64, 1024), COPPER(4, 128, 2048), IRON(8, 256, 4096),
        GOLD(16, 128, 8192), DIAMOND(32, 1024, 16384), NETHERITE(64, 4096, 65536);
        public final int manualYield, scraperUses, brushCapacity;
        Material(int yield, int uses, int capacity) {
            manualYield = yield; scraperUses = uses; brushCapacity = capacity;
        }
    }
    public enum Station {
        WORKBENCH(2, 1024), CUTTER(4, 4096), ASSEMBLER(8, 16384);
        public final int multiplier, capacity;
        Station(int multiplier, int capacity) { this.multiplier = multiplier; this.capacity = capacity; }
        public int yield(Material material) { return multiplier * material.manualYield; }
    }
    public enum Bucket {
        COPPER(1, 262144), GOLD(4, 524288), DIAMOND(8, 262144), NETHERITE(8, 524288);
        public final int paints, capacityPerPaint;
        Bucket(int paints, int capacity) { this.paints = paints; capacityPerPaint = capacity; }
    }
    public enum Production {
        SCRAPER(1), MILL(4), PRESS(16);
        public final int itemsPerBlock;
        Production(int items) { itemsPerBlock = items; }
        public int unitsPerBlock() { return itemsPerBlock * UNITS_PER_PAINT_ITEM; }
    }
}
