package net.nicomar.paintableblocks.design;

import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;

/** Convex polygons clipped by one canonical plane. Shared by rendering, ray tracing and paint costs. */
public final class SlopeGeometry {
    private SlopeGeometry(){}
    private static double coord(Vec3 p,int i){return i==0?p.x:i==1?p.y:p.z;}
    public static int axis(Direction face){return face.getAxis()==Direction.Axis.X?0:face.getAxis()==Direction.Axis.Y?1:2;}
    public static Vec3 normal(BlockDesign d,DesignGeometry.Surface s){return s.slope()?d.slope().normal():new Vec3(s.face().getStepX(),s.face().getStepY(),s.face().getStepZ());}
    private record PolygonKey(BlockDesign.GeometryKey geometry,int surface){}
    private static final net.nicomar.paintableblocks.performance.WorkCache<PolygonKey,List<List<Vec3>>> FRAGMENTS=new net.nicomar.paintableblocks.performance.WorkCache<>("surface-fragments",32768,262144);
    public static List<List<Vec3>> polygons(BlockDesign d,DesignGeometry.Surface s){return FRAGMENTS.get(new PolygonKey(d.geometryKey(),s.key()),()->buildFragments(d,s),p->p.stream().mapToInt(List::size).sum()+1);}
    public static List<Vec3> polygon(BlockDesign d,DesignGeometry.Surface s){var all=polygons(d,s);return all.isEmpty()?List.of():all.getFirst();}
    public static double surfaceArea(BlockDesign d,DesignGeometry.Surface s){return polygons(d,s).stream().mapToDouble(SlopeGeometry::area).sum();}
    private static List<List<Vec3>> buildFragments(BlockDesign d,DesignGeometry.Surface s){
        int c=s.cell(),n=d.resolution();double[] lo={d.x(c)/(double)n,d.y(c)/(double)n,d.z(c)/(double)n},hi={lo[0]+1.0/n,lo[1]+1.0/n,lo[2]+1.0/n};
        if(d.slope()!=null){var a=d.slope().point(lo[0],lo[1],lo[2]);var b=d.slope().point(hi[0],hi[1],hi[2]);lo=new double[]{a.x,a.y,a.z};hi=new double[]{b.x,b.y,b.z};}
        var points=new TreeSet<Double>();int axis=d.slope()==null?-1:d.slope().window().get(0);
        if(axis>=0){points.add(lo[axis]);points.add(hi[axis]);for(int i=1;i<=2;i++){double v=d.slope().window().get(i)/16.0;if(v>lo[axis]+1e-10&&v<hi[axis]-1e-10)points.add(v);}}
        else {var p=buildPiece(d,s,lo,hi);return area(p)>1e-12?List.of(List.copyOf(p)):List.of();}
        var cuts=new ArrayList<>(points);var out=new ArrayList<List<Vec3>>();
        for(int i=0;i+1<cuts.size();i++){var low=lo.clone();var high=hi.clone();low[axis]=cuts.get(i);high[axis]=cuts.get(i+1);var p=buildPiece(d,s,low,high);if(area(p)>1e-12)out.add(List.copyOf(p));}return List.copyOf(out);
    }
    private static List<Vec3> buildPiece(BlockDesign d,DesignGeometry.Surface s,double[] lo,double[] hi){
        boolean cutting=d.slope()==null||d.slope().cuts(new Vec3((lo[0]+hi[0])/2,(lo[1]+hi[1])/2,(lo[2]+hi[2])/2));
        if(!s.slope()){
            int ax=axis(s.face()),u=(ax+1)%3,v=(ax+2)%3;List<Vec3> p=new ArrayList<>();
            for(int i:new int[]{0,1,3,2}){double[] q=lo.clone();q[ax]=s.face().getAxisDirection()==Direction.AxisDirection.POSITIVE?hi[ax]:lo[ax];q[u]=(i&1)==0?lo[u]:hi[u];q[v]=(i&2)==0?lo[v]:hi[v];p.add(new Vec3(q[0],q[1],q[2]));}
            if(s.face().getAxisDirection()==Direction.AxisDirection.NEGATIVE)Collections.reverse(p);
            if(d.slope()!=null&&d.slope().window().get(0)>=0){
                var sample=centroid(p).add(s.face().getStepX()*1e-7,s.face().getStepY()*1e-7,s.face().getStepZ()*1e-7);
                var grid=d.slope().grid(sample);int n=d.resolution();
                if(grid.x>=0&&grid.y>=0&&grid.z>=0&&grid.x<1&&grid.y<1&&grid.z<1&&d.occupied((int)(grid.x*n),(int)(grid.y*n),(int)(grid.z*n))){
                    if(cutting||!d.slope().cuts(sample))return List.of();
                    var plane=d.slope();return clip(p,new SlopePlane(-plane.a(),-plane.b(),-plane.c(),-plane.offset()));
                }
            }
            return d.slope()==null||!cutting?p:clip(p,d.slope());
        }
        if(d.slope()==null||!cutting)return List.of();
        var plane=d.slope();List<Vec3> vertices=new ArrayList<>();Vec3[] corners=new Vec3[8];double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
        for(int i=0;i<8;i++){corners[i]=new Vec3((i&1)==0?lo[0]:hi[0],(i&2)==0?lo[1]:hi[1],(i&4)==0?lo[2]:hi[2]);double value=plane.value(corners[i]);min=Math.min(min,value);max=Math.max(max,value);}
        if(min>=-1e-10||max<=1e-10)return List.of();
        for(int i=0;i<8;i++)for(int bit:new int[]{1,2,4})if((i&bit)==0){var a=corners[i];var b=corners[i|bit];double va=plane.value(a),vb=plane.value(b);
            if(Math.abs(va)<1e-10)add(vertices,a);if(Math.abs(vb)<1e-10)add(vertices,b);if(va*vb<0)add(vertices,a.lerp(b,va/(va-vb)));}
        if(vertices.size()<3)return List.of();
        Vec3 center=centroid(vertices),normal=plane.normal().normalize();Vec3 u=normal.cross(Math.abs(normal.y)<.9?new Vec3(0,1,0):new Vec3(1,0,0)).normalize(),v=normal.cross(u);
        vertices.sort(Comparator.comparingDouble(p->Math.atan2(p.subtract(center).dot(v),p.subtract(center).dot(u))));return List.copyOf(vertices);
    }
    private static void add(List<Vec3> v,Vec3 p){for(var a:v)if(a.distanceToSqr(p)<1e-18)return;v.add(p);}
    public static List<Vec3> clip(List<Vec3> polygon,SlopePlane plane){
        if(polygon.isEmpty())return polygon;List<Vec3> out=new ArrayList<>();Vec3 a=polygon.getLast();double va=plane.value(a);
        for(Vec3 b:polygon){double vb=plane.value(b);if((va<=1e-10)!=(vb<=1e-10))add(out,a.lerp(b,va/(va-vb)));if(vb<=1e-10)add(out,b);a=b;va=vb;}return out;
    }
    public static double area(List<Vec3> p){double area=0;if(p.size()<3)return 0;for(int i=1;i+1<p.size();i++)area+=p.get(i).subtract(p.getFirst()).cross(p.get(i+1).subtract(p.getFirst())).length()/2;return area;}
    public static Vec3 centroid(List<Vec3> p){Vec3 out=Vec3.ZERO;for(var v:p)out=out.add(v);return p.isEmpty()?out:out.scale(1.0/p.size());}
    public static int units(BlockDesign d,DesignGeometry.Surface s){return Math.max(1,(int)Math.ceil(surfaceArea(d,s)*256-1e-9));}
    public static boolean coplanar(BlockDesign d,DesignGeometry.Surface a,DesignGeometry.Surface b){
        if(a.slope()||b.slope())return a.slope()&&b.slope();
        if(a.face()!=b.face())return false;int axis=axis(a.face());return coord(centroid(polygon(d,a)),axis)==coord(centroid(polygon(d,b)),axis);
    }
    public static boolean contains(List<Vec3> polygon,Vec3 p,Vec3 normal){
        if(polygon.size()<3)return false;for(int i=0;i<polygon.size();i++){Vec3 a=polygon.get(i),b=polygon.get((i+1)%polygon.size());if(b.subtract(a).cross(p.subtract(a)).dot(normal)<-1e-8)return false;}return true;
    }
    public static Optional<DesignGeometry.Surface> target(BlockDesign d,Vec3 worldPoint){
        var point=DesignOrientation.rotate(DesignOrientation.inverse(d.orientation()),worldPoint);
        int n=d.resolution();var grid=d.slope()==null?point:d.slope().grid(point);int x=(int)Math.floor(grid.x*n),y=(int)Math.floor(grid.y*n),z=(int)Math.floor(grid.z*n);
        for(int iz=Math.max(0,z-1);iz<=Math.min(n-1,z+1);iz++)for(int iy=Math.max(0,y-1);iy<=Math.min(n-1,y+1);iy++)for(int ix=Math.max(0,x-1);ix<=Math.min(n-1,x+1);ix++){
            int cell=d.index(ix,iy,iz);if(!d.occupied(cell))continue;
            var candidates=DesignGeometry.cellSurfaces(d,cell);
            for(int pass=0;pass<2;pass++)for(var s:candidates){if(s.slope()!=(pass==0))continue;for(var polygon:polygons(d,s)){if(polygon.size()<3)continue;var normal=normal(d,s);if(Math.abs(normal.dot(point.subtract(polygon.getFirst())))<1e-6&&contains(polygon,point,normal))return Optional.of(s);}}
        }return Optional.empty();
    }
    public static BlockHitResult ray(BlockDesign d,Vec3 start,Vec3 end,BlockPos pos){
        int inverse=DesignOrientation.inverse(d.orientation());Vec3 a=DesignOrientation.rotate(inverse,start.subtract(pos.getX(),pos.getY(),pos.getZ())),b=DesignOrientation.rotate(inverse,end.subtract(pos.getX(),pos.getY(),pos.getZ())),delta=b.subtract(a);double nearest=Double.POSITIVE_INFINITY;DesignGeometry.Surface picked=null;
        double enter=0,exit=1;
        for(int axis=0;axis<3;axis++){double origin=coord(a,axis),step=coord(delta,axis);if(Math.abs(step)<1e-14){if(origin<0||origin>1)return null;continue;}double t0=-origin/step,t1=(1-origin)/step;enter=Math.max(enter,Math.min(t0,t1));exit=Math.min(exit,Math.max(t0,t1));}
        if(enter>exit)return null;int n=d.resolution();Vec3 initial=a.add(delta.scale(Math.min(exit,enter+1e-9)));if(d.slope()!=null)initial=d.slope().grid(initial);int[] cell={(int)Math.floor(initial.x*n),(int)Math.floor(initial.y*n),(int)Math.floor(initial.z*n)};
        for(int i=0;i<3;i++)cell[i]=Math.clamp(cell[i],0,n-1);
        for(int iteration=0;iteration<3*n+4;iteration++){
            for(var s:DesignGeometry.cellSurfaces(d,d.index(cell[0],cell[1],cell[2]))){for(var polygon:polygons(d,s)){if(polygon.size()<3)continue;var normal=normal(d,s);double den=normal.dot(delta);if(den>=-1e-12)continue;double t=normal.dot(polygon.getFirst().subtract(a))/den;if(t>=enter-1e-8&&t<=exit+1e-9&&t<nearest&&contains(polygon,a.add(delta.scale(t)),normal)){nearest=t;picked=s;}}}
            double[] crossing=new double[3];double next=Double.POSITIVE_INFINITY;
            for(int axis=0;axis<3;axis++){double step=coord(delta,axis);crossing[axis]=Math.abs(step)<1e-14?Double.POSITIVE_INFINITY:((d.slope()==null?(cell[axis]+(step>0?1:0))/(double)n:d.slope().coordinate(axis,(cell[axis]+(step>0?1:0))/(double)n))-coord(a,axis))/step;next=Math.min(next,crossing[axis]);}
            if(nearest<=next+1e-9||next>=exit-1e-9)break;
            for(int axis=0;axis<3;axis++)if(crossing[axis]<=next+1e-10)cell[axis]+=coord(delta,axis)>0?1:-1;
            if(cell[0]<0||cell[1]<0||cell[2]<0||cell[0]>=n||cell[1]>=n||cell[2]>=n)break;
        }
        if(picked==null)return null;return new BlockHitResult(start.lerp(end,Math.max(0,nearest)),DesignOrientation.direction(d.orientation(),picked.face()),pos,false);
    }
}
