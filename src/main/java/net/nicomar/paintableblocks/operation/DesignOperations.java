package net.nicomar.paintableblocks.operation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.Direction;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.design.BlockDesign;
import net.nicomar.paintableblocks.design.DesignGeometry;
import net.nicomar.paintableblocks.design.DesignOrientation;
import net.nicomar.paintableblocks.design.SlopeGeometry;
import net.nicomar.paintableblocks.paint.PaintCatalog;

/** Pure plans, shared by the preview and authoritative actions. No access to players or levels. */
public final class DesignOperations {
    public enum Action { HAMMER, MALLET, CHISEL, SCRAPE, CLEAN, ROTATE }
    public record Plan(BlockDesign before,BlockDesign after,List<Integer> cells,List<DesignGeometry.Surface> faces,int units){
        public Plan { cells=List.copyOf(cells);faces=List.copyOf(faces); }
        public boolean changes(){return !before.equals(after);}
    }
    private DesignOperations(){}
    private static int coordinate(BlockDesign d,int cell,Direction.Axis axis){return axis==Direction.Axis.X?d.x(cell):axis==Direction.Axis.Y?d.y(cell):d.z(cell);}
    public static Plan tool(BlockDesign d,DesignGeometry.Surface hit,Action action,Direction worldFace,boolean reverse){
        if(action==Action.ROTATE){
            var after=d.withOrientation(DesignOrientation.compose(DesignOrientation.quarterTurn(worldFace,reverse),d.orientation()));
            return new Plan(d,after,List.of(),List.of(),0);
        }
        if(action==Action.CLEAN){
            var faces=DesignGeometry.surfaces(d).stream().filter(f->d.paint(f)!=0).toList();
            return new Plan(d,d.withoutPaint(),List.of(),faces,0);
        }
        if(!d.validSurfaceKey(hit.key()))throw new IllegalArgumentException("Target is not exposed");
        if(action==Action.SCRAPE){
            var faces=DesignGeometry.surfaces(d).stream().filter(f->SlopeGeometry.coplanar(d,f,hit)&&d.paint(f)!=0).toList();
            TreeMap<Integer,Integer> changes=new TreeMap<>();for(var f:faces)changes.put(f.key(),0);
            return new Plan(d,d.paintFaces(changes),List.of(),faces,0);
        }
        if(d.resolution()==1)return new Plan(d,d,List.of(),List.of(),0);
        BitSet cells=new BitSet();int n=d.resolution();Direction face=hit.face();
        if(action==Action.HAMMER)cells.set(hit.cell());
        if(action==Action.CHISEL)for(int c=0;c<n*n*n;c++)if(d.occupied(c)&&coordinate(d,c,face.getAxis())==coordinate(d,hit.cell(),face.getAxis()))cells.set(c);
        if(action==Action.MALLET){
            int axis=face.getAxis()==Direction.Axis.X?0:face.getAxis()==Direction.Axis.Y?1:2;
            int u=(axis+1)%3,v=(axis+2)%3;int sign=face.getAxisDirection()==Direction.AxisDirection.POSITIVE?-1:1;
            for(int a=0;a<n;a++)for(int b=0;b<n;b++)for(int step=0;step<n;step++){
                int[] xyz=new int[3];xyz[u]=a;xyz[v]=b;xyz[axis]=sign>0?step:n-1-step;
                int c=d.index(xyz[0],xyz[1],xyz[2]);if(d.occupied(c)){cells.set(c);break;}
            }
        }
        List<Integer> selected=cells.stream().boxed().toList();
        return new Plan(d,d.remove(cells),selected,List.of(),0);
    }
    public static List<DesignGeometry.Surface> area(BlockDesign d,DesignGeometry.Surface first,DesignGeometry.Surface last){
        if(!d.validSurfaceKey(first.key())||!d.validSurfaceKey(last.key())||!SlopeGeometry.coplanar(d,first,last))throw new IllegalArgumentException("Area is not coplanar");
        var normal=SlopeGeometry.normal(d,first).normalize();
        var u=first.slope()?normal.cross(Math.abs(normal.y)<.9?new net.minecraft.world.phys.Vec3(0,1,0):new net.minecraft.world.phys.Vec3(1,0,0)).normalize():new net.minecraft.world.phys.Vec3(first.face().getAxis()==Direction.Axis.X?0:1,0,first.face().getAxis()==Direction.Axis.X?1:0);
        var v=normal.cross(u);var a=SlopeGeometry.centroid(SlopeGeometry.polygon(d,first));var b=SlopeGeometry.centroid(SlopeGeometry.polygon(d,last));
        double minU=Math.min(a.dot(u),b.dot(u)),maxU=Math.max(a.dot(u),b.dot(u)),minV=Math.min(a.dot(v),b.dot(v)),maxV=Math.max(a.dot(v),b.dot(v));
        return DesignGeometry.surfaces(d).stream().filter(f->SlopeGeometry.coplanar(d,first,f)).filter(f->{var center=SlopeGeometry.centroid(SlopeGeometry.polygon(d,f));return center.dot(u)>=minU-1e-8&&center.dot(u)<=maxU+1e-8&&center.dot(v)>=minV-1e-8&&center.dot(v)<=maxV+1e-8;}).toList();
    }
    public static Plan paint(BlockDesign d,List<DesignGeometry.Surface> surfaces,int id){
        if(!PaintCatalog.validId(id))throw new IllegalArgumentException("Invalid paint identity");
        TreeMap<Integer,Integer> changes=new TreeMap<>();List<DesignGeometry.Surface> affected=new ArrayList<>();
        for(var f:surfaces){
            if(!d.validSurfaceKey(f.key()))throw new IllegalArgumentException("Hidden paint surface");
            int key=f.key();
            if(d.paint(f)!=id && changes.put(key,id)==null)affected.add(f);
        }
        return new Plan(d,d.paintFaces(changes),List.of(),affected,affected.stream().mapToInt(f->SlopeGeometry.units(d,f)).sum());
    }
}
