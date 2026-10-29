package net.nicomar.paintableblocks.design;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Broad-phase voxel bounds, exact polygon ray tracing. Collision remains a separate 1/16 shape. */
public class DesignOutlineShape extends VoxelShape {
    private final BlockDesign design;
    private final DoubleList coordinates;
    public DesignOutlineShape(BlockDesign design){super(discrete(design));this.design=design;var values=new DoubleArrayList();for(int i=0;i<=design.resolution();i++)values.add(i/(double)design.resolution());coordinates=values;}
    private static BitSetDiscreteVoxelShape discrete(BlockDesign d){int n=d.resolution();var bits=new BitSetDiscreteVoxelShape(n,n,n);for(int c=0;c<n*n*n;c++)if(d.occupied(c)){int[] p=DesignOrientation.cell(d.orientation(),d.x(c),d.y(c),d.z(c),n);bits.fill(p[0],p[1],p[2]);}return bits;}
    @Override public DoubleList getCoords(Direction.Axis axis){return coordinates;}
    @Override public void forAllEdges(net.minecraft.world.phys.shapes.Shapes.DoubleLineConsumer consumer){
        for(var s:DesignGeometry.surfaces(design))emit(design,s,consumer);
    }
    static void emit(BlockDesign d,DesignGeometry.Surface s,net.minecraft.world.phys.shapes.Shapes.DoubleLineConsumer consumer){
        var p=SlopeGeometry.polygon(d,s);for(int i=0;i<p.size();i++){var a=DesignOrientation.rotate(d.orientation(),p.get(i));var b=DesignOrientation.rotate(d.orientation(),p.get((i+1)%p.size()));consumer.consume(a.x,a.y,a.z,b.x,b.y,b.z);}
    }
    @Override public BlockHitResult clip(Vec3 start,Vec3 end,BlockPos pos){return SlopeGeometry.ray(design,start,end,pos);}
}
