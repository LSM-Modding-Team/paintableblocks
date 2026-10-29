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
    private record Key(BlockDesign.GeometryKey geometry,int orientation){}
    private record Segment(Vec3 a,Vec3 b){}
    private static final net.nicomar.paintableblocks.performance.WorkCache<Key,java.util.List<Segment>> EDGES=new net.nicomar.paintableblocks.performance.WorkCache<>("edges",256,262144);
    private final DoubleList coordinates;
    public DesignOutlineShape(BlockDesign design){super(discrete(design));this.design=design.withoutPaint();var values=new DoubleArrayList();for(int i=0;i<=design.resolution();i++)values.add(i/(double)design.resolution());coordinates=values;}
    private static final net.nicomar.paintableblocks.performance.WorkCache<Key,BitSetDiscreteVoxelShape> GRIDS=new net.nicomar.paintableblocks.performance.WorkCache<>("outline-grids",256,1048576);
    private static BitSetDiscreteVoxelShape discrete(BlockDesign d){return GRIDS.get(new Key(d.geometryKey(),d.orientation()),()->buildDiscrete(d),g->d.resolution()*d.resolution()*d.resolution());}
    private static BitSetDiscreteVoxelShape buildDiscrete(BlockDesign d){int n=d.resolution();var bits=new BitSetDiscreteVoxelShape(n,n,n);for(int c=0;c<n*n*n;c++)if(d.occupied(c)){int[] p=DesignOrientation.cell(d.orientation(),d.x(c),d.y(c),d.z(c),n);bits.fill(p[0],p[1],p[2]);}return bits;}
    @Override public DoubleList getCoords(Direction.Axis axis){return coordinates;}
    @Override public void forAllEdges(net.minecraft.world.phys.shapes.Shapes.DoubleLineConsumer consumer){
        for(var segment:EDGES.get(new Key(design.geometryKey(),design.orientation()),()->{
            java.util.Map<String,Segment> unique=new java.util.LinkedHashMap<>();
            for(var s:DesignGeometry.surfaces(design))emit(design,s,(x,y,z,X,Y,Z)->{var a=new Vec3(x,y,z);var b=new Vec3(X,Y,Z);String pa=point(a),pb=point(b);unique.putIfAbsent(pa.compareTo(pb)<0?pa+"/"+pb:pb+"/"+pa,new Segment(a,b));});
            return java.util.List.copyOf(unique.values());
        },java.util.List::size))consumer.consume(segment.a.x,segment.a.y,segment.a.z,segment.b.x,segment.b.y,segment.b.z);
    }
    private static String point(Vec3 p){return Math.round(p.x*1e10)+","+Math.round(p.y*1e10)+","+Math.round(p.z*1e10);}
    static void emit(BlockDesign d,DesignGeometry.Surface s,net.minecraft.world.phys.shapes.Shapes.DoubleLineConsumer consumer){
        var p=SlopeGeometry.polygon(d,s);for(int i=0;i<p.size();i++){var a=DesignOrientation.rotate(d.orientation(),p.get(i));var b=DesignOrientation.rotate(d.orientation(),p.get((i+1)%p.size()));consumer.consume(a.x,a.y,a.z,b.x,b.y,b.z);}
    }
    @Override public BlockHitResult clip(Vec3 start,Vec3 end,BlockPos pos){return SlopeGeometry.ray(design,start,end,pos);}
}
