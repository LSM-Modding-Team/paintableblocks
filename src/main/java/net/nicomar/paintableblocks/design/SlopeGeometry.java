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
    private static final net.nicomar.paintableblocks.performance.WorkCache<PolygonKey,List<Vec3>> POLYGONS=new net.nicomar.paintableblocks.performance.WorkCache<>("polygons",65536,262144);
    public static List<Vec3> polygon(BlockDesign d,DesignGeometry.Surface s){return POLYGONS.get(new PolygonKey(d.geometryKey(),s.key()),()->List.copyOf(buildPolygon(d,s)),p->p.size()+1);}
    private static List<Vec3> buildPolygon(BlockDesign d,DesignGeometry.Surface s){
        int c=s.cell(),n=d.resolution();double[] lo={d.x(c)/(double)n,d.y(c)/(double)n,d.z(c)/(double)n},hi={lo[0]+1.0/n,lo[1]+1.0/n,lo[2]+1.0/n};
        if(!s.slope()){
            int ax=axis(s.face()),u=(ax+1)%3,v=(ax+2)%3;List<Vec3> p=new ArrayList<>();
            for(int i:new int[]{0,1,3,2}){double[] q=lo.clone();q[ax]=s.face().getAxisDirection()==Direction.AxisDirection.POSITIVE?hi[ax]:lo[ax];q[u]=(i&1)==0?lo[u]:hi[u];q[v]=(i&2)==0?lo[v]:hi[v];p.add(new Vec3(q[0],q[1],q[2]));}
            if(s.face().getAxisDirection()==Direction.AxisDirection.NEGATIVE)Collections.reverse(p);
            return d.slope()==null?p:clip(p,d.slope());
        }
        if(d.slope()==null)return List.of();
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
    public static int units(BlockDesign d,DesignGeometry.Surface s){return Math.max(1,(int)Math.ceil(area(polygon(d,s))*256-1e-9));}
    public static boolean coplanar(BlockDesign d,DesignGeometry.Surface a,DesignGeometry.Surface b){
        if(a.slope()||b.slope())return a.slope()&&b.slope();
        if(a.face()!=b.face())return false;int axis=axis(a.face());return coord(centroid(polygon(d,a)),axis)==coord(centroid(polygon(d,b)),axis);
    }
    public static boolean contains(List<Vec3> polygon,Vec3 p,Vec3 normal){
        if(polygon.size()<3)return false;for(int i=0;i<polygon.size();i++){Vec3 a=polygon.get(i),b=polygon.get((i+1)%polygon.size());if(b.subtract(a).cross(p.subtract(a)).dot(normal)<-1e-8)return false;}return true;
    }
    public static Optional<DesignGeometry.Surface> target(BlockDesign d,Vec3 worldPoint){
        var point=DesignOrientation.rotate(DesignOrientation.inverse(d.orientation()),worldPoint);
        int n=d.resolution();int x=(int)Math.floor(point.x*n),y=(int)Math.floor(point.y*n),z=(int)Math.floor(point.z*n);
        for(int iz=Math.max(0,z-1);iz<=Math.min(n-1,z+1);iz++)for(int iy=Math.max(0,y-1);iy<=Math.min(n-1,y+1);iy++)for(int ix=Math.max(0,x-1);ix<=Math.min(n-1,x+1);ix++){
            int cell=d.index(ix,iy,iz);if(!d.occupied(cell))continue;
            var candidates=DesignGeometry.cellSurfaces(d,cell);
            for(int pass=0;pass<2;pass++)for(var s:candidates){if(s.slope()!=(pass==0))continue;var polygon=polygon(d,s);if(polygon.size()<3)continue;var normal=normal(d,s);if(Math.abs(normal.dot(point.subtract(polygon.getFirst())))<1e-6&&contains(polygon,point,normal))return Optional.of(s);}
        }return Optional.empty();
    }
    public static BlockHitResult ray(BlockDesign d,Vec3 start,Vec3 end,BlockPos pos){
        int inverse=DesignOrientation.inverse(d.orientation());Vec3 a=DesignOrientation.rotate(inverse,start.subtract(pos.getX(),pos.getY(),pos.getZ())),b=DesignOrientation.rotate(inverse,end.subtract(pos.getX(),pos.getY(),pos.getZ())),delta=b.subtract(a);double nearest=Double.POSITIVE_INFINITY;DesignGeometry.Surface picked=null;
        double enter=0,exit=1;
        for(int axis=0;axis<3;axis++){double origin=coord(a,axis),step=coord(delta,axis);if(Math.abs(step)<1e-14){if(origin<0||origin>1)return null;continue;}double t0=-origin/step,t1=(1-origin)/step;enter=Math.max(enter,Math.min(t0,t1));exit=Math.min(exit,Math.max(t0,t1));}
        if(enter>exit)return null;int n=d.resolution();Vec3 initial=a.add(delta.scale(Math.min(exit,enter+1e-9)));int[] cell={(int)Math.floor(initial.x*n),(int)Math.floor(initial.y*n),(int)Math.floor(initial.z*n)};
        for(int i=0;i<3;i++)cell[i]=Math.clamp(cell[i],0,n-1);
        for(int iteration=0;iteration<3*n+4;iteration++){
            for(var s:DesignGeometry.cellSurfaces(d,d.index(cell[0],cell[1],cell[2]))){var polygon=polygon(d,s);if(polygon.size()<3)continue;var normal=normal(d,s);double den=normal.dot(delta);if(den>=-1e-12)continue;double t=normal.dot(polygon.getFirst().subtract(a))/den;if(t>=enter-1e-8&&t<=exit+1e-9&&t<nearest&&contains(polygon,a.add(delta.scale(t)),normal)){nearest=t;picked=s;}}
            double[] crossing=new double[3];double next=Double.POSITIVE_INFINITY;
            for(int axis=0;axis<3;axis++){double step=coord(delta,axis);crossing[axis]=Math.abs(step)<1e-14?Double.POSITIVE_INFINITY:((cell[axis]+(step>0?1:0))/(double)n-coord(a,axis))/step;next=Math.min(next,crossing[axis]);}
            if(nearest<=next+1e-9||next>=exit-1e-9)break;
            for(int axis=0;axis<3;axis++)if(crossing[axis]<=next+1e-10)cell[axis]+=coord(delta,axis)>0?1:-1;
            if(cell[0]<0||cell[1]<0||cell[2]<0||cell[0]>=n||cell[1]>=n||cell[2]>=n)break;
        }
        if(picked==null)return null;return new BlockHitResult(start.lerp(end,Math.max(0,nearest)),DesignOrientation.direction(d.orientation(),picked.face()),pos,false);
    }
}
