package net.nicomar.paintableblocks.design;

import it.unimi.dsi.fastutil.doubles.*;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.*;

/** Build the discrete occupancy once; native optimization merges boxes without thousands of unions. */
final class GridShape extends VoxelShape {
    private final DoubleList coordinates;
    GridShape(BitSetDiscreteVoxelShape occupied,int resolution){super(occupied);double[] values=new double[resolution+1];for(int i=0;i<=resolution;i++)values[i]=i/(double)resolution;coordinates=DoubleLists.unmodifiable(new DoubleArrayList(values));}
    @Override public DoubleList getCoords(Direction.Axis axis){return coordinates;}
}
