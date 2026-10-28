package net.nicomar.paintableblocks.design;

import net.minecraft.core.BlockPos;

/** Scoped to BlockItem.place, including native callbacks and component restoration. */
public final class PlacementDesign implements AutoCloseable {
    private static final ThreadLocal<PlacementDesign> ACTIVE=new ThreadLocal<>();
    private final PlacementDesign previous;
    private final BlockPos pos;
    private final BlockDesign design;
    private PlacementDesign(BlockPos pos,BlockDesign design){previous=ACTIVE.get();this.pos=pos.immutable();this.design=design;ACTIVE.set(this);}
    public static PlacementDesign enter(BlockPos pos,BlockDesign design){return new PlacementDesign(pos,design);}
    public static BlockDesign current(BlockPos pos,int resolution){var scope=ACTIVE.get();return scope!=null&&scope.pos.equals(pos)&&scope.design.resolution()==resolution?scope.design:null;}
    @Override public void close(){if(previous==null)ACTIVE.remove();else ACTIVE.set(previous);}
}
