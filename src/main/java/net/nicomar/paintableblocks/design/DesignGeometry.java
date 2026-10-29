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
    public record Surface(int cell,Direction face,boolean slope) {
        public Surface(int cell,Direction face){this(cell,face,false);}
        public int key(){return slope?BlockDesign.SLOPE_KEY+cell:cell*6+face.ordinal();}
    }
    public record Box(int x0,int y0,int z0,int x1,int y1,int z1) {}
    private DesignGeometry() {}
    private static final java.util.LinkedHashMap<BlockDesign.GeometryKey,List<Surface>> SURFACES=new java.util.LinkedHashMap<>(16,.75F,true);
    private static int cachedSurfaces;
    public static List<Surface> surfaces(BlockDesign design){
        var key=design.geometryKey();synchronized(SURFACES){var cached=SURFACES.get(key);if(cached!=null)return cached;}
        List<Surface> out=new ArrayList<>();int n=design.resolution();
        for(int c=0;c<n*n*n;c++)if(design.occupied(c))for(Direction face:Direction.values())if(design.exposed(c,face))out.add(new Surface(c,face));
        if(design.slope()!=null)for(int c=0;c<n*n*n;c++)if(design.occupied(c)){
            var surface=new Surface(c,design.slope().dominant(),true);if(SlopeGeometry.area(SlopeGeometry.polygon(design,surface))>1e-12)out.add(surface);
        }
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
        if(d.slope()!=null)return slopeShape(d);
        double n=d.resolution();VoxelShape out=Shapes.empty();
        for(Box b:boxes(d))out=Shapes.joinUnoptimized(out,Shapes.box(b.x0()/n,b.y0()/n,b.z0()/n,b.x1()/n,b.y1()/n,b.z1()/n),BooleanOp.OR);
        return out.optimize();
    }
    private static VoxelShape slopeShape(BlockDesign d){
        VoxelShape out=Shapes.empty();int inverse=DesignOrientation.inverse(d.orientation());
        // Every collision voxel is 1/16 regardless of the paintable resolution.
        for(int z=0;z<16;z++)for(int y=0;y<16;y++)for(int x=0;x<16;x++){
            var p=DesignOrientation.rotate(inverse,new Vec3((x+.5)/16,(y+.5)/16,(z+.5)/16));int n=d.resolution();
            if(d.occupied(Math.min(n-1,(int)(p.x*n)),Math.min(n-1,(int)(p.y*n)),Math.min(n-1,(int)(p.z*n)))&&d.slope().value(p)<=1e-10)
                out=Shapes.joinUnoptimized(out,Shapes.box(x/16.0,y/16.0,z/16.0,(x+1)/16.0,(y+1)/16.0,(z+1)/16.0),BooleanOp.OR);
        }return out.optimize();
    }
    public static VoxelShape supportShape(BlockDesign d){
        if(d.slope()==null)return shape(d);
        // Only full axis-aligned outer faces support attachments; never the stepped incline.
        VoxelShape out=Shapes.empty();
        for(Direction face:Direction.values()){
            final Direction f=face;var polygons=surfaces(d).stream().filter(s->!s.slope()&&s.face()==f).map(s->SlopeGeometry.polygon(d,s)).filter(p->!p.isEmpty()&&p.stream().allMatch(v->Math.abs((f.getAxis()==Direction.Axis.X?v.x:f.getAxis()==Direction.Axis.Y?v.y:v.z)-(f.getAxisDirection()==Direction.AxisDirection.POSITIVE?1:0))<1e-9)).toList();
            double area=polygons.stream().mapToDouble(SlopeGeometry::area).sum();
            if(Math.abs(area-1)<1e-8){double[] min={0,0,0},max={1,1,1};int ax=SlopeGeometry.axis(f);if(f.getAxisDirection()==Direction.AxisDirection.POSITIVE)min[ax]=15.0/16;else max[ax]=1.0/16;out=Shapes.joinUnoptimized(out,Shapes.box(min[0],min[1],min[2],max[0],max[1],max[2]),BooleanOp.OR);}
        }return out.optimize();
    }
    /** Hit must already have been ray-traced against the remaining shape; never targets hidden faces. */
    public static Optional<Surface> target(BlockDesign local,Vec3 point,Direction normal){
        if(local.slope()!=null)return SlopeGeometry.target(local,point);
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
