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
    int count(){return lines.size();}
    PreviewLines limited(int count){return count>=lines.size()?this:new PreviewLines(lines.subList(0,Math.max(0,count)),true);}
    @Override public DoubleList getCoords(Direction.Axis axis){return COORDS;}
    @Override public boolean isEmpty(){return lines.isEmpty();}
    @Override public void forAllEdges(Shapes.DoubleLineConsumer c){for(var l:lines)c.consume(l.x,l.y,l.z,l.X,l.Y,l.Z);}
}
