package net.nicomar.paintableblocks.client.selection;

import java.util.*;
import it.unimi.dsi.fastutil.doubles.*;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.*;

/** Prepared line buffer with a bounded display cost. It never participates in selection or server decisions. */
final class PreviewLines extends VoxelShape {
    private record Line(double x,double y,double z,double X,double Y,double Z){}
    private static final DoubleList COORDS=DoubleLists.unmodifiable(new DoubleArrayList(new double[]{0,1}));
    private final List<Line> lines;final boolean simplified;
    private PreviewLines(List<Line> lines,boolean simplified){super(grid());this.lines=lines;this.simplified=simplified;}
    private static BitSetDiscreteVoxelShape grid(){var grid=new BitSetDiscreteVoxelShape(1,1,1);grid.fill(0,0,0);return grid;}
    static PreviewLines capture(VoxelShape source){List<Line> lines=new ArrayList<>();int[] total={0};source.forAllEdges((x,y,z,X,Y,Z)->{if(total[0]++<1024)lines.add(new Line(x,y,z,X,Y,Z));});return new PreviewLines(List.copyOf(lines),total[0]>1024);}
    static PreviewLines grid(Direction face,double coordinate){
        int axis=face.getAxis()==Direction.Axis.X?0:face.getAxis()==Direction.Axis.Y?1:2,u=(axis+1)%3,v=(axis+2)%3;double plane=coordinate+(face.getAxisDirection()==Direction.AxisDirection.POSITIVE?.0005:-.0005);
        List<Line> out=new ArrayList<>();
        for(int i=0;i<=16;i++)for(int swap=0;swap<2;swap++){double[] a={0,0,0},b={0,0,0};a[axis]=plane;b[axis]=plane;int fixed=swap==0?u:v,other=swap==0?v:u;a[fixed]=i/16.0;b[fixed]=i/16.0;b[other]=1;out.add(new Line(a[0],a[1],a[2],b[0],b[1],b[2]));}
        return new PreviewLines(List.copyOf(out),false);
    }
    static PreviewLines direction(net.nicomar.paintableblocks.network.ModNetworking.VolumeState s,net.minecraft.core.BlockPos anchor){
        boolean x=s.direction()%2==0,positive=s.direction()<2;
        double low=(x?s.x0():s.z0())/16.0,high=(x?s.x1():s.z1())/16.0;
        double a=positive?low:high,b=positive?high:low;
        if(s.combined()){a+=(positive?1:-1)*(s.fullAtStart()?s.fullLength():s.otherLength())/16.0;b-=(positive?1:-1)*(s.fullAtStart()?s.otherLength():s.fullLength())/16.0;}
        double dependent=(s.vertical()?(x?s.z0():s.x0()):s.y0())/16.0;
        double start=dependent+s.start()/16.0,end=dependent+(s.end()<0?(s.vertical()?(x?s.z1()-s.z0():s.x1()-s.x0()):s.y1()-s.y0()):s.end())/16.0;
        double middleY=(s.y0()+s.y1())/32.0,middleX=(s.x0()+s.x1())/32.0,middleZ=(s.z0()+s.z1())/32.0;
        if(s.lateralHigh()>=0){double mid=(s.lateralLow()+s.lateralHigh())/32.0;if(s.vertical())middleY=s.y0()/16.0+mid;else if(x)middleZ=s.z0()/16.0+mid;else middleX=s.x0()/16.0+mid;}
        var p=new net.minecraft.world.phys.Vec3(x?a:s.vertical()?start:middleX,s.vertical()?middleY:start,x?(s.vertical()?start:middleZ):a).subtract(anchor.getX(),anchor.getY(),anchor.getZ());
        var q=new net.minecraft.world.phys.Vec3(x?b:s.vertical()?end:middleX,s.vertical()?middleY:end,x?(s.vertical()?end:middleZ):b).subtract(anchor.getX(),anchor.getY(),anchor.getZ());
        var along=q.subtract(p);if(along.lengthSqr()<1e-12)return new PreviewLines(List.of(),false);
        var unit=along.normalize();var side=unit.cross(s.vertical()?new net.minecraft.world.phys.Vec3(0,1,0):new net.minecraft.world.phys.Vec3(x?0:1,0,x?1:0)).normalize().scale(Math.min(.15,along.length()/5));
        var back=q.subtract(unit.scale(Math.min(.3,along.length()/3)));
        var left=back.add(side);var right=back.subtract(side);
        return new PreviewLines(List.of(new Line(p.x,p.y,p.z,q.x,q.y,q.z),new Line(q.x,q.y,q.z,left.x,left.y,left.z),new Line(q.x,q.y,q.z,right.x,right.y,right.z)),false);
    }
    int count(){return lines.size();}
    PreviewLines limited(int count){return count>=lines.size()?this:new PreviewLines(lines.subList(0,Math.max(0,count)),true);}
    @Override public DoubleList getCoords(Direction.Axis axis){return COORDS;}
    @Override public boolean isEmpty(){return lines.isEmpty();}
    @Override public void forAllEdges(Shapes.DoubleLineConsumer c){for(var l:lines)c.consume(l.x,l.y,l.z,l.X,l.Y,l.Z);}
}
