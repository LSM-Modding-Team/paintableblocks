package net.nicomar.paintableblocks.design;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Surface coverage across independent neighboring pieces, including mixed subdivisions. */
public final class NeighborOcclusion {
    private NeighborOcclusion(){}
    private record Key(BoundaryGeometry.Face own,BoundaryGeometry.Coverage other,int direction,int orientation){}
    private record WholeKey(BlockDesign.GeometryKey geometry,int orientation,List<BoundaryGeometry.Coverage> neighbors){}
    private static final net.nicomar.paintableblocks.performance.WorkCache<WholeKey,Map<Integer,List<List<Vec3>>>> WHOLE=new net.nicomar.paintableblocks.performance.WorkCache<>("visibility",256,262144);
    private static final class SurfaceMap extends AbstractMap<Integer,List<List<Vec3>>> {
        private final Map<Integer,List<List<Vec3>>> data;private final int hash;
        SurfaceMap(Map<Integer,List<List<Vec3>>> data){this.data=Map.copyOf(data);hash=this.data.hashCode();}
        @Override public Set<Entry<Integer,List<List<Vec3>>>> entrySet(){return data.entrySet();}
        @Override public List<List<Vec3>> get(Object key){return data.get(key);}
        @Override public int hashCode(){return hash;}
    }
    private static final net.nicomar.paintableblocks.performance.WorkCache<Key,Map<Integer,List<List<Vec3>>>> CACHE=new net.nicomar.paintableblocks.performance.WorkCache<>("contacts",512,262144);
    public static List<BlockDesign> neighbors(java.util.function.Function<BlockPos,BlockDesign> lookup,BlockPos pos){var out=new ArrayList<BlockDesign>();for(var direction:Direction.values())out.add(lookup.apply(pos.relative(direction)));return java.util.Collections.unmodifiableList(out);}
    public static Set<Integer> hidden(BlockDesign design,List<BlockDesign> neighbors){var result=new HashSet<Integer>();for(var e:visible(design,neighbors).entrySet())if(e.getValue().isEmpty())result.add(e.getKey());return Set.copyOf(result);}
    public static Map<Integer,List<List<Vec3>>> visible(BlockDesign design,List<BlockDesign> neighbors){
        List<BoundaryGeometry.Coverage> stamps=new ArrayList<>();for(var direction:Direction.values()){var d=neighbors.get(direction.ordinal());stamps.add(d==null?BoundaryGeometry.EMPTY:BoundaryGeometry.faces(d).get(direction.getOpposite().ordinal()).stamp());}
        return WHOLE.get(new WholeKey(design.geometryKey(),design.orientation(),List.copyOf(stamps)),()->visibleUncached(design,neighbors),map->{int count=map.size();for(var list:map.values())for(var p:list)count+=p.size();return count;});
    }
    private static Map<Integer,List<List<Vec3>>> visibleUncached(BlockDesign design,List<BlockDesign> neighbors){
        Map<Integer,List<List<Vec3>>> result=new HashMap<>();var frontiers=BoundaryGeometry.faces(design);
        for(var face:Direction.values()){
            var neighbor=neighbors.get(face.ordinal());if(neighbor==null)continue;var own=frontiers.get(face.ordinal());if(own.patches().isEmpty())continue;
            var other=BoundaryGeometry.faces(neighbor).get(face.getOpposite().ordinal());if(other.patches().isEmpty())continue;
            if(other.full()){for(var patch:own.patches())result.put(patch.surface(),List.of());continue;}
            var key=new Key(own,other.stamp(),face.ordinal(),design.orientation());
            result.putAll(CACHE.get(key,()->contact(design,face,own,other),map->{int count=map.size();for(var list:map.values())for(var polygon:list)count+=polygon.size();return count;}));
        }return new SurfaceMap(result);
    }
    private static Map<Integer,List<List<Vec3>>> contact(BlockDesign design,Direction face,BoundaryGeometry.Face own,BoundaryGeometry.Face other){
        Map<Integer,List<List<Vec3>>> result=new HashMap<>();int axis=SlopeGeometry.axis(face),inverse=DesignOrientation.inverse(design.orientation());
        for(var original:own.patches()){
            List<List<Vec3>> fragments=new ArrayList<>();fragments.add(original.polygon());
            for(var patch:other.patches()){
                if(original.u1()<=patch.u0()+1e-10||patch.u1()<=original.u0()+1e-10||original.v1()<=patch.v0()+1e-10||patch.v1()<=original.v0()+1e-10)continue;
                var mask=patch.polygon().stream().map(p->p.add(face.getStepX(),face.getStepY(),face.getStepZ())).toList();
                List<List<Vec3>> next=new ArrayList<>();for(var fragment:fragments)next.addAll(subtract(fragment,mask,axis));fragments=next;if(fragments.isEmpty())break;
            }
            double area=0;for(var fragment:fragments)area+=SlopeGeometry.area(fragment);
            if(area<SlopeGeometry.area(original.polygon())-1e-9)result.put(original.surface(),fragments.stream().map(p->p.stream().map(v->DesignOrientation.rotate(inverse,v)).toList()).toList());
        }return new SurfaceMap(result);
    }
    private static List<List<Vec3>> subtract(List<Vec3> subject,List<Vec3> mask,int axis){
        int u=(axis+1)%3,v=(axis+2)%3;
        if(disjoint(subject,mask,u)||disjoint(subject,mask,v)||SlopeGeometry.area(intersection(subject,mask,axis))<1e-12)return List.of(subject);
        double signed=0;for(int i=0;i<mask.size();i++){var a=mask.get(i);var b=mask.get((i+1)%mask.size());signed+=coordinate(a,u)*coordinate(b,v)-coordinate(b,u)*coordinate(a,v);}double sign=signed>=0?1:-1;
        var inside=subject;List<List<Vec3>> result=new ArrayList<>();
        for(int i=0;i<mask.size()&&!inside.isEmpty();i++){var a=mask.get(i);var b=mask.get((i+1)%mask.size());var outside=cut(inside,a,b,u,v,-sign);if(SlopeGeometry.area(outside)>1e-12)result.add(outside);inside=cut(inside,a,b,u,v,sign);}return result;
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
