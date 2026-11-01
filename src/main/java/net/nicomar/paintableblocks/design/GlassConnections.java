package net.nicomar.paintableblocks.design;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.nicomar.paintableblocks.paint.PaintCatalog;

/** Pure visual geometry. Borders follow the union's perimeter, never the editable cell grid. */
public final class GlassConnections {
    public static final double BORDER_WIDTH=1.0/16;
    private static final double EPS=1e-9;
    public record Border(Vec3 start,Vec3 along,Vec3 inward,double length){}
    /** connected=false keeps the original vanilla projection; a null border is connected interior. */
    public record Fragment(DesignGeometry.Surface surface,List<Vec3> polygon,boolean connected,Border border,boolean doubleSided){
        public Fragment(DesignGeometry.Surface surface,List<Vec3> polygon,boolean connected,Border border){this(surface,polygon,connected,border,true);}
    }
    private record Patch(DesignGeometry.Surface surface,List<Vec3> polygon,Vec3 normal,int paint,boolean own){}
    private record Edge(Vec3 a,Vec3 along,Vec3 inward,Vec3 normal,double length,int paint){}
    private record Point(long x,long y,long z){
        static Point of(Vec3 p){return new Point(Math.round(p.x*1e8),Math.round(p.y*1e8),Math.round(p.z*1e8));}
    }
    private record Line(int paint,Point normal,Point direction,Point moment){
        static Line of(Edge e){
            Vec3 d=e.along();
            if(d.x< -EPS||Math.abs(d.x)<EPS&&(d.y< -EPS||Math.abs(d.y)<EPS&&d.z<0))d=d.scale(-1);
            return new Line(e.paint(),Point.of(e.normal()),Point.of(d),Point.of(e.a().cross(d)));
        }
    }
    private record Interval(double from,double to){}
    private record HalfPlane(Vec3 normal,double offset){}
    private GlassConnections(){}

    public static boolean needed(BlockDesign design){return design.paints().values().stream().anyMatch(PaintCatalog::isConnectedGlass);}
    public static boolean hasGlass(BlockDesign design){return design.paints().values().stream().anyMatch(PaintCatalog::isGlass);}
    /** Edge/corner neighbors matter for inclined planes crossing two block boundaries at once. */
    public static Map<BlockPos,BlockDesign> surroundings(Function<BlockPos,BlockDesign> lookup,BlockPos origin){
        Map<BlockPos,BlockDesign> out=new HashMap<>();
        for(int z=-1;z<=1;z++)for(int y=-1;y<=1;y++)for(int x=-1;x<=1;x++){
            if(x==0&&y==0&&z==0)continue;
            var d=lookup.apply(origin.offset(x,y,z));
            if(d!=null)out.put(new BlockPos(x,y,z),d);
        }
        return Map.copyOf(out);
    }

    public static List<Fragment> fragments(BlockDesign design,Map<Integer,List<List<Vec3>>> exterior,Map<BlockPos,BlockDesign> surroundings){
        var own=SurfaceGroups.merge(design,exterior);
        if(!needed(design))return contacts(design,own.stream().map(p->new Fragment(p.surface(),p.polygon(),false,null)).toList(),surroundings);
        List<Patch> patches=new ArrayList<>();
        append(patches,design,own,BlockPos.ZERO,true);
        Map<BlockPos,BlockDesign> world=new HashMap<>(surroundings);world.put(BlockPos.ZERO,design);
        for(var entry:surroundings.entrySet()){
            var other=entry.getValue();if(!needed(other))continue;
            var neighbors=NeighborOcclusion.neighbors(world::get,entry.getKey());
            append(patches,other,SurfaceGroups.merge(other,NeighborOcclusion.visible(other,neighbors)),entry.getKey(),false);
        }
        Map<Line,List<Edge>> lines=new HashMap<>();
        for(var patch:patches)for(var edge:edges(patch))lines.computeIfAbsent(Line.of(edge),k->new ArrayList<>()).add(edge);
        int inverse=DesignOrientation.inverse(design.orientation());
        List<Fragment> result=new ArrayList<>();
        for(var patch:own)if(!PaintCatalog.isConnectedGlass(design.paint(patch.surface())))result.add(new Fragment(patch.surface(),patch.polygon(),false,null));
        for(var patch:patches){
            if(!patch.own())continue;
            List<List<Vec3>> interior=new ArrayList<>();interior.add(patch.polygon());
            for(var edge:edges(patch))for(var interval:uncovered(edge,lines.get(Line.of(edge)))){
                Vec3 start=edge.a().add(edge.along().scale(interval.from()));
                double length=interval.to()-interval.from();
                var border=new Border(local(inverse,start),vector(inverse,edge.along()),vector(inverse,edge.inward()),length);
                var planes=List.of(new HalfPlane(edge.along(),edge.along().dot(start)),
                        new HalfPlane(edge.along().scale(-1),-edge.along().dot(start)-length),
                        new HalfPlane(edge.inward(),edge.inward().dot(start)),
                        new HalfPlane(edge.inward().scale(-1),-edge.inward().dot(start)-BORDER_WIDTH));
                List<List<Vec3>> remaining=new ArrayList<>();
                for(var polygon:interior){
                    var inside=polygon;
                    for(var plane:planes){
                        var outside=cut(inside,plane,false);if(SlopeGeometry.area(outside)>EPS)remaining.add(outside);
                        inside=cut(inside,plane,true);if(inside.isEmpty())break;
                    }
                    if(SlopeGeometry.area(inside)>EPS)result.add(new Fragment(patch.surface(),local(inverse,inside),true,border));
                }
                interior=remaining;
            }
            for(var polygon:interior)if(SlopeGeometry.area(polygon)>EPS)result.add(new Fragment(patch.surface(),local(inverse,polygon),true,null));
        }
        return contacts(design,result,surroundings);
    }
    /** Contact fronts already provide the two opposite windings. Duplicating their backs causes z-fighting. */
    private static List<Fragment> contacts(BlockDesign design,List<Fragment> fragments,Map<BlockPos,BlockDesign> surroundings){
        if(surroundings.isEmpty()||!hasGlass(design))return List.copyOf(fragments);
        List<Fragment> result=new ArrayList<>();int inverse=DesignOrientation.inverse(design.orientation());
        for(var fragment:fragments){
            var surface=fragment.surface();Direction face=DesignOrientation.direction(design.orientation(),surface.face());
            var neighbor=surroundings.get(new BlockPos(face.getStepX(),face.getStepY(),face.getStepZ()));
            if(surface.slope()||!PaintCatalog.isGlass(design.paint(surface))||neighbor==null){result.add(fragment);continue;}
            int axis=SlopeGeometry.axis(face);double plane=face.getAxisDirection()==Direction.AxisDirection.POSITIVE?1:0;
            var polygon=fragment.polygon().stream().map(p->DesignOrientation.rotate(design.orientation(),p)).toList();
            if(polygon.stream().anyMatch(p->Math.abs(coordinate(p,axis)-plane)>1e-8)){result.add(fragment);continue;}
            var boundary=BoundaryGeometry.faces(neighbor).get(face.getOpposite().ordinal());
            if(boundary.full()&&boundary.patches().stream().allMatch(p->PaintCatalog.isGlass(neighbor.paints().getOrDefault(p.surface(),0)))){
                result.add(new Fragment(surface,fragment.polygon(),fragment.connected(),fragment.border(),false));continue;
            }
            List<List<Vec3>> remaining=new ArrayList<>();remaining.add(polygon);
            for(var patch:boundary.patches()){
                if(!PaintCatalog.isGlass(neighbor.paints().getOrDefault(patch.surface(),0)))continue;
                var mask=patch.polygon().stream().map(p->p.add(face.getStepX(),face.getStepY(),face.getStepZ())).toList();
                List<List<Vec3>> next=new ArrayList<>();
                for(var part:remaining){
                    var contact=NeighborOcclusion.intersection(part,mask,axis);
                    if(SlopeGeometry.area(contact)>EPS)result.add(new Fragment(surface,local(inverse,contact),fragment.connected(),fragment.border(),false));
                    next.addAll(NeighborOcclusion.subtract(part,mask,axis));
                }
                remaining=next;if(remaining.isEmpty())break;
            }
            for(var part:remaining)result.add(new Fragment(surface,local(inverse,part),fragment.connected(),fragment.border(),true));
        }
        return List.copyOf(result);
    }
    private static double coordinate(Vec3 p,int axis){return axis==0?p.x:axis==1?p.y:p.z;}
    private static void append(List<Patch> out,BlockDesign d,List<SurfaceGroups.Patch> patches,BlockPos offset,boolean own){
        var translation=new Vec3(offset.getX(),offset.getY(),offset.getZ());
        for(var p:patches){
            int paint=d.paint(p.surface());if(!PaintCatalog.isConnectedGlass(paint))continue;
            out.add(new Patch(p.surface(),p.polygon().stream().map(v->DesignOrientation.rotate(d.orientation(),v).add(translation)).toList(),
                    vector(d.orientation(),SlopeGeometry.normal(d,p.surface())).normalize(),paint,own));
        }
    }
    private static List<Edge> edges(Patch patch){
        List<Edge> out=new ArrayList<>();var polygon=patch.polygon();
        for(int i=0;i<polygon.size();i++){
            Vec3 a=polygon.get(i),delta=polygon.get((i+1)%polygon.size()).subtract(a);double length=delta.length();
            if(length<EPS)continue;Vec3 along=delta.scale(1/length);
            out.add(new Edge(a,along,patch.normal().cross(along).normalize(),patch.normal(),length,patch.paint()));
        }
        return out;
    }
    private static List<Interval> uncovered(Edge edge,List<Edge> candidates){
        List<Interval> intervals=new ArrayList<>();intervals.add(new Interval(0,edge.length()));
        for(var other:candidates){
            // Opposite winding is essential: overlapping or angle-changing faces do not join.
            if(edge.along().dot(other.along())> -1+1e-8)continue;
            double a=other.a().subtract(edge.a()).dot(edge.along()),b=a+other.length()*other.along().dot(edge.along());
            double lo=Math.min(a,b),hi=Math.max(a,b);List<Interval> next=new ArrayList<>();
            for(var interval:intervals){
                if(hi<=interval.from()+EPS||lo>=interval.to()-EPS){next.add(interval);continue;}
                if(lo>interval.from()+EPS)next.add(new Interval(interval.from(),lo));
                if(hi<interval.to()-EPS)next.add(new Interval(hi,interval.to()));
            }
            intervals=next;if(intervals.isEmpty())break;
        }
        return intervals;
    }
    private static List<Vec3> cut(List<Vec3> polygon,HalfPlane plane,boolean positive){
        if(polygon.isEmpty())return List.of();
        List<Vec3> out=new ArrayList<>();Vec3 previous=polygon.getLast();double sign=positive?1:-1,dp=(plane.normal().dot(previous)-plane.offset())*sign;
        for(var point:polygon){
            double dn=(plane.normal().dot(point)-plane.offset())*sign;
            if((dp>=0)!=(dn>=0))add(out,previous.lerp(point,dp/(dp-dn)));
            if(dn>=0)add(out,point);previous=point;dp=dn;
        }
        if(out.size()>1&&out.getFirst().distanceToSqr(out.getLast())<1e-18)out.removeLast();
        return out.size()<3?List.of():List.copyOf(out);
    }
    private static void add(List<Vec3> out,Vec3 p){if(out.isEmpty()||out.getLast().distanceToSqr(p)>1e-18)out.add(p);}
    private static Vec3 vector(int orientation,Vec3 v){return DesignOrientation.rotate(orientation,v).subtract(DesignOrientation.rotate(orientation,Vec3.ZERO));}
    private static Vec3 local(int inverse,Vec3 v){return DesignOrientation.rotate(inverse,v);}
    private static List<Vec3> local(int inverse,List<Vec3> p){return p.stream().map(v->local(inverse,v)).toList();}
}
