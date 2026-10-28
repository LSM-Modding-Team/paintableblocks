package net.nicomar.paintableblocks.design;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Exact occupancy geometry: greedy boxes reduce collision complexity without filling holes. */
public final class DesignGeometry {
    public record Surface(int cell,Direction face) {}
    public record Box(int x0,int y0,int z0,int x1,int y1,int z1) {}
    private DesignGeometry() {}
    private static final java.util.LinkedHashMap<BlockDesign.GeometryKey,List<Surface>> SURFACES=new java.util.LinkedHashMap<>(16,.75F,true);
    private static int cachedSurfaces;
    public static List<Surface> surfaces(BlockDesign design){
        var key=design.geometryKey();synchronized(SURFACES){var cached=SURFACES.get(key);if(cached!=null)return cached;}
        List<Surface> out=new ArrayList<>();int n=design.resolution();
        for(int c=0;c<n*n*n;c++)if(design.occupied(c))for(Direction face:Direction.values())if(design.exposed(c,face))out.add(new Surface(c,face));
        var result=List.copyOf(out);
        synchronized(SURFACES){
            var raced=SURFACES.get(key);if(raced!=null)return raced;
            while(!SURFACES.isEmpty()&&(SURFACES.size()>=64||cachedSurfaces+result.size()>65536)){var iterator=SURFACES.entrySet().iterator();cachedSurfaces-=iterator.next().getValue().size();iterator.remove();}
            if(result.size()<=65536){SURFACES.put(key,result);cachedSurfaces+=result.size();}
        }
        return result;
    }
    public static List<Box> boxes(BlockDesign local){
        BlockDesign d=local.withoutPaint().physical();int n=d.resolution();boolean[] used=new boolean[n*n*n];List<Box> out=new ArrayList<>();
        for(int z=0;z<n;z++)for(int y=0;y<n;y++)for(int x=0;x<n;x++){
            if(!free(d,used,x,y,z))continue;
            int x1=x+1;while(x1<n&&free(d,used,x1,y,z))x1++;
            int y1=y+1;outer:while(y1<n){for(int a=x;a<x1;a++)if(!free(d,used,a,y1,z))break outer;y1++;}
            int z1=z+1;outer:while(z1<n){for(int b=y;b<y1;b++)for(int a=x;a<x1;a++)if(!free(d,used,a,b,z1))break outer;z1++;}
            for(int c=z;c<z1;c++)for(int b=y;b<y1;b++)for(int a=x;a<x1;a++)used[d.index(a,b,c)]=true;
            out.add(new Box(x,y,z,x1,y1,z1));
        }
        return List.copyOf(out);
    }
    private static boolean free(BlockDesign d,boolean[] used,int x,int y,int z){return d.occupied(x,y,z)&&!used[d.index(x,y,z)];}
    public static VoxelShape shape(BlockDesign d){
        double n=d.resolution();VoxelShape out=Shapes.empty();
        for(Box b:boxes(d))out=Shapes.joinUnoptimized(out,Shapes.box(b.x0()/n,b.y0()/n,b.z0()/n,b.x1()/n,b.y1()/n,b.z1()/n),BooleanOp.OR);
        return out.optimize();
    }
    /** Hit must already have been ray-traced against the remaining shape; never targets hidden faces. */
    public static Optional<Surface> target(BlockDesign local,Vec3 point,Direction normal){
        int inverse=DesignOrientation.inverse(local.orientation());Vec3 p=DesignOrientation.rotate(inverse,point);
        Direction face=DesignOrientation.direction(inverse,normal);int n=local.resolution();double epsilon=1.0e-6;
        int x=(int)Math.floor((p.x-face.getStepX()*epsilon)*n);
        int y=(int)Math.floor((p.y-face.getStepY()*epsilon)*n);
        int z=(int)Math.floor((p.z-face.getStepZ()*epsilon)*n);
        if(p.x < -epsilon || p.y < -epsilon || p.z < -epsilon || p.x > 1+epsilon || p.y > 1+epsilon || p.z > 1+epsilon)return Optional.empty();
        x=Math.clamp(x,0,n-1);y=Math.clamp(y,0,n-1);z=Math.clamp(z,0,n-1);
        if(!local.occupied(x,y,z))return Optional.empty();
        int cell=local.index(x,y,z);return local.exposed(cell,face)?Optional.of(new Surface(cell,face)):Optional.empty();
    }
}
