package net.nicomar.paintableblocks.design;

import java.util.*;
import net.minecraft.world.phys.Vec3;

/** Visual-only unions. Logical cells, paint identity, layer footprints and texture coordinates stay intact. */
public final class SurfaceGroups {
    public record Patch(DesignGeometry.Surface surface,List<Vec3> polygon){}
    private record Key(int face,boolean slope,int paint,long plane){}
    private SurfaceGroups(){}
    public static List<Patch> merge(BlockDesign d,Map<Integer,List<List<Vec3>>> exterior){
        Map<Key,List<Patch>> groups=new LinkedHashMap<>();
        for(var surface:DesignGeometry.surfaces(d)){
            var fragments=exterior.get(surface.key());if(fragments==null)fragments=List.of(SlopeGeometry.polygon(d,surface));
            for(var polygon:fragments){if(polygon.size()<3)continue;double plane=SlopeGeometry.normal(d,surface).dot(polygon.getFirst());var key=new Key(surface.face().ordinal(),surface.slope(),d.paint(surface),Double.doubleToLongBits(plane));groups.computeIfAbsent(key,k->new ArrayList<>()).add(new Patch(surface,polygon));}
        }
        List<Patch> out=new ArrayList<>();
        for(var group:groups.values()){
            // Pair only polygons sharing an edge, with a convex union of exactly the original area.
            var pending=new ArrayList<>(group);boolean changed=true;
            while(changed){changed=false;Map<Edge,Integer> edges=new HashMap<>();
                outer:for(int i=0;i<pending.size();i++){var polygon=pending.get(i).polygon();for(int e=0;e<polygon.size();e++){
                    var edge=Edge.of(polygon.get(e),polygon.get((e+1)%polygon.size()));var previous=edges.putIfAbsent(edge,i);if(previous==null||previous==i)continue;
                    var merged=union(pending.get(previous).polygon(),polygon,SlopeGeometry.normal(d,pending.get(i).surface()));
                    if(merged!=null){pending.set(previous,new Patch(pending.get(previous).surface(),merged));pending.remove(i);changed=true;break outer;}
                }}
            }out.addAll(pending);
        }return List.copyOf(out);
    }
    private record Point(long x,long y,long z) implements Comparable<Point>{
        static Point of(Vec3 p){return new Point(Math.round(p.x*1e10),Math.round(p.y*1e10),Math.round(p.z*1e10));}
        @Override public int compareTo(Point p){int c=Long.compare(x,p.x);if(c==0)c=Long.compare(y,p.y);if(c==0)c=Long.compare(z,p.z);return c;}
    }
    private record Edge(Point a,Point b){static Edge of(Vec3 a,Vec3 b){var x=Point.of(a);var y=Point.of(b);return x.compareTo(y)<0?new Edge(x,y):new Edge(y,x);}}
    private static List<Vec3> union(List<Vec3> a,List<Vec3> b,Vec3 normal){
        int axis=SlopeGeometry.axis(net.minecraft.core.Direction.getNearest((int)Math.round(normal.x*512),(int)Math.round(normal.y*512),(int)Math.round(normal.z*512),net.minecraft.core.Direction.UP)),u=(axis+1)%3,v=(axis+2)%3;
        var points=new ArrayList<Vec3>();for(var p:a)points.add(p);for(var p:b)if(points.stream().noneMatch(q->q.distanceToSqr(p)<1e-18))points.add(p);
        points.sort(Comparator.comparingDouble((Vec3 p)->coord(p,u)).thenComparingDouble(p->coord(p,v)));
        List<Vec3> hull=new ArrayList<>();for(var p:points){while(hull.size()>=2&&cross(hull.get(hull.size()-2),hull.getLast(),p,u,v)<=1e-12)hull.removeLast();hull.add(p);}int lower=hull.size();
        for(int i=points.size()-2;i>=0;i--){var p=points.get(i);while(hull.size()>lower&&cross(hull.get(hull.size()-2),hull.getLast(),p,u,v)<=1e-12)hull.removeLast();hull.add(p);}if(hull.size()>1)hull.removeLast();
        if(Math.abs(SlopeGeometry.area(hull)-SlopeGeometry.area(a)-SlopeGeometry.area(b))>1e-9)return null;
        if(hull.size()>=3&&hull.get(1).subtract(hull.get(0)).cross(hull.get(2).subtract(hull.get(0))).dot(normal)<0)Collections.reverse(hull);return List.copyOf(hull);
    }
    private static double coord(Vec3 p,int axis){return axis==0?p.x:axis==1?p.y:p.z;}
    private static double cross(Vec3 a,Vec3 b,Vec3 c,int u,int v){return (coord(b,u)-coord(a,u))*(coord(c,v)-coord(a,v))-(coord(b,v)-coord(a,v))*(coord(c,u)-coord(a,u));}
}
