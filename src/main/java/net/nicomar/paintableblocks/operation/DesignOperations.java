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
            var faces=DesignGeometry.surfaces(d).stream().filter(f->d.paint(f.cell(),f.face())!=0).toList();
            return new Plan(d,d.withoutPaint(),List.of(),faces,0);
        }
        if(!d.exposed(hit.cell(),hit.face()))throw new IllegalArgumentException("Target is not exposed");
        if(action==Action.SCRAPE){
            var faces=DesignGeometry.surfaces(d).stream().filter(f->f.face()==hit.face()
                    &&coordinate(d,f.cell(),f.face().getAxis())==coordinate(d,hit.cell(),hit.face().getAxis())&&d.paint(f.cell(),f.face())!=0).toList();
            TreeMap<Integer,Integer> changes=new TreeMap<>();for(var f:faces)changes.put(f.cell()*6+f.face().ordinal(),0);
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
        if(first.face()!=last.face()||coordinate(d,first.cell(),first.face().getAxis())!=coordinate(d,last.cell(),last.face().getAxis()))throw new IllegalArgumentException("Area is not coplanar");
        if(!d.exposed(first.cell(),first.face())||!d.exposed(last.cell(),last.face()))throw new IllegalArgumentException("Area corner is not exposed");
        int minX=Math.min(d.x(first.cell()),d.x(last.cell())),maxX=Math.max(d.x(first.cell()),d.x(last.cell()));
        int minY=Math.min(d.y(first.cell()),d.y(last.cell())),maxY=Math.max(d.y(first.cell()),d.y(last.cell()));
        int minZ=Math.min(d.z(first.cell()),d.z(last.cell())),maxZ=Math.max(d.z(first.cell()),d.z(last.cell()));
        return DesignGeometry.surfaces(d).stream().filter(f->f.face()==first.face()&&d.x(f.cell())>=minX&&d.x(f.cell())<=maxX
                &&d.y(f.cell())>=minY&&d.y(f.cell())<=maxY&&d.z(f.cell())>=minZ&&d.z(f.cell())<=maxZ).toList();
    }
    public static Plan paint(BlockDesign d,List<DesignGeometry.Surface> surfaces,int id){
        if(!PaintCatalog.validId(id))throw new IllegalArgumentException("Invalid paint identity");
        TreeMap<Integer,Integer> changes=new TreeMap<>();List<DesignGeometry.Surface> affected=new ArrayList<>();
        for(var f:surfaces){
            if(!d.exposed(f.cell(),f.face()))throw new IllegalArgumentException("Hidden paint surface");
            int key=f.cell()*6+f.face().ordinal();
            if(d.paint(f.cell(),f.face())!=id && changes.put(key,id)==null)affected.add(f);
        }
        return new Plan(d,d.paintFaces(changes),List.of(),affected,Math.multiplyExact(changes.size(),PaintingBalance.surfaceUnits(d.resolution())));
    }
}
