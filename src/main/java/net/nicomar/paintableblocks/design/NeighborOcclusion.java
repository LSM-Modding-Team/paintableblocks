package net.nicomar.paintableblocks.design;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Surface coverage across independent neighboring pieces, including mixed subdivisions. */
public final class NeighborOcclusion {
    private NeighborOcclusion(){}
    private record Stamp(BlockDesign.GeometryKey geometry,int orientation){}
    private record Key(Stamp design,List<Stamp> neighbors){}
    private static final LinkedHashMap<Key,Map<Integer,List<List<Vec3>>>> CACHE=new LinkedHashMap<>(16,.75F,true);
    private static int cachedVertices;
    private static Stamp stamp(BlockDesign d){return d==null?new Stamp(null,0):new Stamp(d.geometryKey(),d.orientation());}
    private static int vertices(Map<Integer,List<List<Vec3>>> value){return value.values().stream().flatMap(List::stream).mapToInt(List::size).sum();}
    public static List<BlockDesign> neighbors(java.util.function.Function<BlockPos,BlockDesign> lookup,BlockPos pos){var out=new ArrayList<BlockDesign>();for(var direction:Direction.values())out.add(lookup.apply(pos.relative(direction)));return java.util.Collections.unmodifiableList(out);}
    public static Set<Integer> hidden(BlockDesign design,List<BlockDesign> neighbors){
        var hidden=new HashSet<Integer>();
        for(var surface:DesignGeometry.surfaces(design)){
            if(surface.slope())continue;var face=DesignOrientation.direction(design.orientation(),surface.face());int axis=SlopeGeometry.axis(face);double boundary=face.getAxisDirection()==Direction.AxisDirection.POSITIVE?1:0;
            var polygon=SlopeGeometry.polygon(design,surface).stream().map(v->DesignOrientation.rotate(design.orientation(),v)).toList();
            if(polygon.stream().anyMatch(v->Math.abs(coordinate(v,axis)-boundary)>1e-8))continue;
            var neighbor=neighbors.get(face.ordinal());if(neighbor==null)continue;double covered=0;
            for(var other:DesignGeometry.surfaces(neighbor)){
                if(other.slope()||DesignOrientation.direction(neighbor.orientation(),other.face())!=face.getOpposite())continue;
                var otherPolygon=SlopeGeometry.polygon(neighbor,other).stream().map(v->DesignOrientation.rotate(neighbor.orientation(),v).add(face.getStepX(),face.getStepY(),face.getStepZ())).toList();
                if(otherPolygon.stream().anyMatch(v->Math.abs(coordinate(v,axis)-boundary)>1e-8))continue;
                covered+=SlopeGeometry.area(intersection(polygon,otherPolygon,axis));
            }
            if(covered>=SlopeGeometry.area(polygon)-1e-9)hidden.add(surface.key());
        }return Set.copyOf(hidden);
    }
    /** Override boundary polygons with their actual exterior difference, including partially covered contacts. */
    public static Map<Integer,List<List<Vec3>>> visible(BlockDesign design,List<BlockDesign> neighbors){
        var key=new Key(stamp(design),neighbors.stream().map(NeighborOcclusion::stamp).toList());
        synchronized(CACHE){var cached=CACHE.get(key);if(cached!=null)return cached;}
        var result=visibleUncached(design,neighbors);int count=vertices(result);
        if(count<=65536)synchronized(CACHE){var raced=CACHE.get(key);if(raced!=null)return raced;while(!CACHE.isEmpty()&&(CACHE.size()>=64||cachedVertices+count>65536)){var iterator=CACHE.entrySet().iterator();cachedVertices-=vertices(iterator.next().getValue());iterator.remove();}CACHE.put(key,result);cachedVertices+=count;}
        return result;
    }
    private static Map<Integer,List<List<Vec3>>> visibleUncached(BlockDesign design,List<BlockDesign> neighbors){
        Map<Integer,List<List<Vec3>>> result=new HashMap<>();
        for(var surface:DesignGeometry.surfaces(design)){
            if(surface.slope())continue;var face=DesignOrientation.direction(design.orientation(),surface.face());var neighbor=neighbors.get(face.ordinal());if(neighbor==null)continue;
            int axis=SlopeGeometry.axis(face);double boundary=face.getAxisDirection()==Direction.AxisDirection.POSITIVE?1:0;
            var original=SlopeGeometry.polygon(design,surface).stream().map(v->DesignOrientation.rotate(design.orientation(),v)).toList();
            if(original.stream().anyMatch(v->Math.abs(coordinate(v,axis)-boundary)>1e-8))continue;
            List<List<Vec3>> fragments=new ArrayList<>();fragments.add(original);
            for(var other:DesignGeometry.surfaces(neighbor)){
                if(other.slope()||DesignOrientation.direction(neighbor.orientation(),other.face())!=face.getOpposite())continue;
                var mask=SlopeGeometry.polygon(neighbor,other).stream().map(v->DesignOrientation.rotate(neighbor.orientation(),v).add(face.getStepX(),face.getStepY(),face.getStepZ())).toList();
                if(mask.stream().anyMatch(v->Math.abs(coordinate(v,axis)-boundary)>1e-8))continue;
                List<List<Vec3>> next=new ArrayList<>();for(var fragment:fragments)next.addAll(subtract(fragment,mask,axis));fragments=next;if(fragments.isEmpty())break;
            }
            double remaining=fragments.stream().mapToDouble(SlopeGeometry::area).sum();
            if(remaining<SlopeGeometry.area(original)-1e-9){int inverse=DesignOrientation.inverse(design.orientation());result.put(surface.key(),fragments.stream().map(p->p.stream().map(v->DesignOrientation.rotate(inverse,v)).toList()).toList());}
        }return Map.copyOf(result);
    }
    private static List<List<Vec3>> subtract(List<Vec3> subject,List<Vec3> mask,int axis){
        int bu=(axis+1)%3,bv=(axis+2)%3;
        if(disjoint(subject,mask,bu)||disjoint(subject,mask,bv)||SlopeGeometry.area(intersection(subject,mask,axis))<1e-12)return List.of(subject);
        int u=(axis+1)%3,v=(axis+2)%3;double signed=0;for(int i=0;i<mask.size();i++){var a=mask.get(i);var b=mask.get((i+1)%mask.size());signed+=coordinate(a,u)*coordinate(b,v)-coordinate(b,u)*coordinate(a,v);}double sign=signed>=0?1:-1;
        var inside=subject;List<List<Vec3>> result=new ArrayList<>();
        for(int i=0;i<mask.size()&&!inside.isEmpty();i++){
            var a=mask.get(i);var b=mask.get((i+1)%mask.size());var outside=cut(inside,a,b,u,v,-sign);if(SlopeGeometry.area(outside)>1e-12)result.add(outside);inside=cut(inside,a,b,u,v,sign);
        }return result;
    }
    private static boolean disjoint(List<Vec3> a,List<Vec3> b,int axis){
        double amin=Double.POSITIVE_INFINITY,amax=Double.NEGATIVE_INFINITY,bmin=Double.POSITIVE_INFINITY,bmax=Double.NEGATIVE_INFINITY;
        for(var p:a){double v=coordinate(p,axis);amin=Math.min(amin,v);amax=Math.max(amax,v);}for(var p:b){double v=coordinate(p,axis);bmin=Math.min(bmin,v);bmax=Math.max(bmax,v);}return amax<=bmin+1e-10||bmax<=amin+1e-10;
    }
    private static List<Vec3> cut(List<Vec3> p,Vec3 a,Vec3 b,int u,int v,double sign){
        List<Vec3> out=new ArrayList<>();if(p.isEmpty())return out;var prev=p.getLast();double dp=side(prev,a,b,u,v)*sign;
        for(var next:p){double dn=side(next,a,b,u,v)*sign;if((dp>=-1e-10)!=(dn>=-1e-10))out.add(prev.lerp(next,dp/(dp-dn)));if(dn>=-1e-10)out.add(next);prev=next;dp=dn;}return out;
    }
    private static double coordinate(Vec3 p,int axis){return axis==0?p.x:axis==1?p.y:p.z;}
    private static List<Vec3> intersection(List<Vec3> subject,List<Vec3> mask,int axis){
        int u=(axis+1)%3,v=(axis+2)%3;double signed=0;for(int i=0;i<mask.size();i++){var a=mask.get(i);var b=mask.get((i+1)%mask.size());signed+=coordinate(a,u)*coordinate(b,v)-coordinate(b,u)*coordinate(a,v);}double sign=signed>=0?1:-1;
        var polygon=subject;
        for(int edge=0;edge<mask.size();edge++){
            if(polygon.isEmpty())break;var a=mask.get(edge);var b=mask.get((edge+1)%mask.size());var out=new ArrayList<Vec3>();var prev=polygon.getLast();double dp=side(prev,a,b,u,v)*sign;
            for(var next:polygon){double dn=side(next,a,b,u,v)*sign;if((dp>=-1e-10)!=(dn>=-1e-10))out.add(prev.lerp(next,dp/(dp-dn)));if(dn>=-1e-10)out.add(next);prev=next;dp=dn;}polygon=out;
        }return polygon;
    }
    private static double side(Vec3 p,Vec3 a,Vec3 b,int u,int v){return (coordinate(b,u)-coordinate(a,u))*(coordinate(p,v)-coordinate(a,v))-(coordinate(b,v)-coordinate(a,v))*(coordinate(p,u)-coordinate(a,u));}
}
