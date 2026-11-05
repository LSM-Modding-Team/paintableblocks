package net.nicomar.paintableblocks.operation;

import java.util.*;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.nicomar.paintableblocks.design.*;

/** Pure, bounded world-space rectangle selection; never loads chunks or edits the world. */
public final class WorldPaintArea {
    private WorldPaintArea(){}
    public static Map<BlockPos,DesignOperations.Plan> plans(Function<BlockPos,BlockDesign> lookup,BlockPos first,DesignGeometry.Surface a,BlockPos last,DesignGeometry.Surface b,int paint){
        var da=lookup.apply(first);var db=lookup.apply(last);if(da==null||db==null)throw new IllegalArgumentException("Missing endpoint");
        var normal=DesignOrientation.rotate(da.orientation(),new Vec3(.5,.5,.5).add(SlopeGeometry.normal(da,a))).subtract(new Vec3(.5,.5,.5)).normalize();
        var other=DesignOrientation.rotate(db.orientation(),new Vec3(.5,.5,.5).add(SlopeGeometry.normal(db,b))).subtract(new Vec3(.5,.5,.5)).normalize();
        if(normal.dot(other)<1-1e-8)throw new IllegalArgumentException("Different plane normals");
        var pa=world(da,a,first);var pb=world(db,b,last);if(pa.isEmpty()||pb.isEmpty())throw new IllegalArgumentException("Empty endpoint");
        double plane=normal.dot(pa.getFirst());if(pa.stream().anyMatch(v->Math.abs(normal.dot(v)-plane)>1e-7)||pb.stream().anyMatch(v->Math.abs(normal.dot(v)-plane)>1e-7))throw new IllegalArgumentException("Different planes");
        Vec3 u=normal.cross(Math.abs(normal.y)<.9?new Vec3(0,1,0):new Vec3(1,0,0)).normalize(),v=normal.cross(u);
        double u0=Double.POSITIVE_INFINITY,u1=-u0,v0=u0,v1=-u0;
        var endpoints=new ArrayList<>(pa);endpoints.addAll(pb);for(var p:endpoints){double x=p.dot(u),y=p.dot(v);u0=Math.min(u0,x);u1=Math.max(u1,x);v0=Math.min(v0,y);v1=Math.max(v1,y);}
        int x0=Math.min(first.getX(),last.getX()),y0=Math.min(first.getY(),last.getY()),z0=Math.min(first.getZ(),last.getZ()),x1=Math.max(first.getX(),last.getX()),y1=Math.max(first.getY(),last.getY()),z1=Math.max(first.getZ(),last.getZ());
        if((long)(x1-x0+1)*(y1-y0+1)*(z1-z0+1)>256)throw new IllegalArgumentException("Area too large");
        Map<BlockPos,DesignOperations.Plan> out=new LinkedHashMap<>();int visited=0;
        for(int z=z0;z<=z1;z++)for(int y=y0;y<=y1;y++)for(int x=x0;x<=x1;x++){
            var pos=new BlockPos(x,y,z);var d=lookup.apply(pos);if(d==null)continue;var faces=new ArrayList<DesignGeometry.Surface>();
            for(var s:DesignGeometry.surfaces(d)){
                if(++visited>32768)throw new IllegalArgumentException("Area too detailed");
                var n=DesignOrientation.rotate(d.orientation(),new Vec3(.5,.5,.5).add(SlopeGeometry.normal(d,s))).subtract(new Vec3(.5,.5,.5)).normalize();if(n.dot(normal)<1-1e-8)continue;
                var polygon=world(d,s,pos);if(polygon.isEmpty()||polygon.stream().anyMatch(p->Math.abs(p.dot(normal)-plane)>1e-7))continue;
                // Select entire logical faces only: boundaries adapt to each block's real subdivision.
                boolean inside=true;for(var p:polygon)if(p.dot(u)<u0-1e-7||p.dot(u)>u1+1e-7||p.dot(v)<v0-1e-7||p.dot(v)>v1+1e-7){inside=false;break;}if(inside)faces.add(s);
            }
            if(!faces.isEmpty())out.put(pos,DesignOperations.paint(d,faces,paint));
        }return Collections.unmodifiableMap(out);
    }
    private static List<Vec3> world(BlockDesign d,DesignGeometry.Surface s,BlockPos pos){return SlopeGeometry.polygons(d,s).stream().flatMap(List::stream).map(p->DesignOrientation.rotate(d.orientation(),p).add(pos.getX(),pos.getY(),pos.getZ())).toList();}
}
