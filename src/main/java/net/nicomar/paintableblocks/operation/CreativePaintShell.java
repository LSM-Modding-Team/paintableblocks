package net.nicomar.paintableblocks.operation;

import java.util.*;
import java.util.function.Function;
import net.minecraft.core.*;
import net.minecraft.world.phys.Vec3;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.paint.PaintCatalog;

/** Paint existing faces coincident with a selection's six boundaries, including concealed cell faces. */
public final class CreativePaintShell {
    private CreativePaintShell(){}
    public static Map<BlockPos,DesignOperations.Plan> plans(Function<BlockPos,BlockDesign> lookup,VolumeRegion region,int paint){
        if(region.size()>2048||!PaintCatalog.validId(paint))throw new IllegalArgumentException("Creative shell limit");
        Map<BlockPos,DesignOperations.Plan> out=new LinkedHashMap<>();int examined=0;
        for(long i=0;i<region.size();i++){
            var pos=region.position(i);var d=lookup.apply(pos);if(d==null)continue;
            Map<Integer,Integer> changes=new TreeMap<>();List<DesignGeometry.Surface> faces=new ArrayList<>();
            int n=d.resolution();for(int c=0;c<n*n*n;c++)if(d.occupied(c)){
                var cell=VolumeRegion.cell(pos,d,c);
                if(cell.x0()<region.x0()||cell.x1()>region.x1()||cell.y0()<region.y0()||cell.y1()>region.y1()||cell.z0()<region.z0()||cell.z1()>region.z1())continue;
                boolean boundary=cell.x0()==region.x0()||cell.x1()==region.x1()||cell.y0()==region.y0()||cell.y1()==region.y1()||cell.z0()==region.z0()||cell.z1()==region.z1();
                if(!boundary)continue;
                if(++examined>262144)throw new IllegalArgumentException("Creative shell detail limit");
                BlockDesign isolated=d.withoutPaint();
                if(d.slope()!=null){var remove=new BitSet();remove.set(0,n*n*n);remove.clear(c);isolated=isolated.remove(remove);}
                for(Direction face:Direction.values()){
                    var surface=new DesignGeometry.Surface(c,face);var polygons=SlopeGeometry.polygons(isolated,surface);if(polygons.isEmpty())continue;
                    boolean onBoundary=false;
                    for(int axis=0;axis<3;axis++)for(int edge=0;edge<2;edge++){
                        double bound=(axis==0?(edge==0?region.x0():region.x1()):axis==1?(edge==0?region.y0():region.y1()):(edge==0?region.z0():region.z1()))/16.0;
                        boolean all=true;for(var polygon:polygons)for(var v:polygon){
                            var p=DesignOrientation.rotate(d.orientation(),v).add(pos.getX(),pos.getY(),pos.getZ());
                            double coord=axis==0?p.x:axis==1?p.y:p.z;
                            if(Math.abs(coord-bound)>1e-8||!region.contains(p)){all=false;break;}
                        }if(all)onBoundary=true;
                    }
                    if(onBoundary){changes.put(surface.key(),paint);faces.add(surface);}
                }
            }
            if(!changes.isEmpty())out.put(pos,new DesignOperations.Plan(d,d.paintStoredFaces(changes),List.of(),faces,0));
        }return Collections.unmodifiableMap(out);
    }
}
