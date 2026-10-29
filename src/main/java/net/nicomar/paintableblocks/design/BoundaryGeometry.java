package net.nicomar.paintableblocks.design;

import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.nicomar.paintableblocks.performance.WorkCache;

/** Prepared world-oriented frontiers. Interior geometry and paint do not participate in contact keys. */
public final class BoundaryGeometry {
    public record Patch(int surface,List<Vec3> polygon,double u0,double v0,double u1,double v1){}
    public static final class Coverage {
        private final List<List<Vec3>> polygons;private final int hash;
        private Coverage(List<List<Vec3>> polygons){this.polygons=polygons;hash=polygons.hashCode();}
        @Override public int hashCode(){return hash;}
        @Override public boolean equals(Object o){return this==o||o instanceof Coverage c&&hash==c.hash&&polygons.equals(c.polygons);}
    }
    public static final Coverage EMPTY=new Coverage(List.of());
    private static final java.util.concurrent.ConcurrentMap<Direction,Coverage> FULL=new java.util.concurrent.ConcurrentHashMap<>();
    private static Coverage fullStamp(List<Patch> patches){var p=patches.getFirst().polygon();var normal=p.get(1).subtract(p.get(0)).cross(p.get(2).subtract(p.get(0))).normalize();var direction=Direction.getNearest((int)Math.round(normal.x*512),(int)Math.round(normal.y*512),(int)Math.round(normal.z*512),Direction.UP);return FULL.computeIfAbsent(direction,d->new Coverage(List.of(SlopeGeometry.polygon(BlockDesign.full(1),new DesignGeometry.Surface(0,d)))));}
    public static final class Face {
        private final List<Patch> patches;private final List<List<Vec3>> coverage;private final boolean full;private final int hash;private final Coverage stamp;
        Face(List<Patch> patches,List<List<Vec3>> coverage,boolean full){this.patches=patches;this.coverage=coverage;this.full=full;hash=patches.hashCode();stamp=coverage.isEmpty()?EMPTY:full?fullStamp(patches):new Coverage(coverage);}
        public List<Patch> patches(){return patches;}public List<List<Vec3>> coverage(){return coverage;}public boolean full(){return full;}public Coverage stamp(){return stamp;}
        @Override public int hashCode(){return hash;}
        @Override public boolean equals(Object o){return this==o||o instanceof Face f&&hash==f.hash&&patches.equals(f.patches);}
    }
    private record Key(BlockDesign.GeometryKey geometry,int orientation){}
    private static final WorkCache<Key,List<Face>> CACHE=new WorkCache<>("frontiers",256,262144);
    private BoundaryGeometry(){}
    public static List<Face> faces(BlockDesign d){return CACHE.get(new Key(d.geometryKey(),d.orientation()),()->build(d),faces->{int count=0;for(var f:faces)for(var p:f.patches)count+=p.polygon.size()+1;return count;});}
    private static List<Face> build(BlockDesign d){
        List<List<Patch>> byDirection=new ArrayList<>();for(int i=0;i<6;i++)byDirection.add(new ArrayList<>());
        for(var s:DesignGeometry.surfaces(d))if(!s.slope()){
            var f=DesignOrientation.direction(d.orientation(),s.face());int axis=SlopeGeometry.axis(f),u=(axis+1)%3,v=(axis+2)%3;double plane=f.getAxisDirection()==Direction.AxisDirection.POSITIVE?1:0;
            var p=SlopeGeometry.polygon(d,s).stream().map(x->DesignOrientation.rotate(d.orientation(),x)).toList();
            double u0=Double.POSITIVE_INFINITY,v0=u0,u1=Double.NEGATIVE_INFINITY,v1=u1;boolean boundary=true;
            for(var x:p){if(Math.abs(coord(x,axis)-plane)>1e-8){boundary=false;break;}u0=Math.min(u0,coord(x,u));u1=Math.max(u1,coord(x,u));v0=Math.min(v0,coord(x,v));v1=Math.max(v1,coord(x,v));}
            if(boundary)byDirection.get(f.ordinal()).add(new Patch(s.key(),p,u0,v0,u1,v1));
        }
        List<Face> out=new ArrayList<>();for(var patches:byDirection){double area=0;for(var patch:patches)area+=SlopeGeometry.area(patch.polygon);out.add(new Face(List.copyOf(patches),patches.stream().map(Patch::polygon).toList(),Math.abs(area-1)<1e-8));}return List.copyOf(out);
    }
    private static double coord(Vec3 p,int axis){return axis==0?p.x:axis==1?p.y:p.z;}
}
