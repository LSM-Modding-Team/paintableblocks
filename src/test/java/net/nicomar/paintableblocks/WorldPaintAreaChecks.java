package net.nicomar.paintableblocks;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.operation.WorldPaintArea;

/** Pure geometry contracts; no world or rendering process required. */
public final class WorldPaintAreaChecks {
    public static void run(){
        var first=BlockPos.ZERO;var last=new BlockPos(2,0,0);
        var blocks=Map.of(first,BlockDesign.full(1),last,BlockDesign.full(2));
        var plans=WorldPaintArea.plans(blocks::get,first,new DesignGeometry.Surface(0,Direction.UP),last,new DesignGeometry.Surface(7,Direction.UP),339);
        if(plans.size()!=2||plans.values().stream().mapToInt(p->p.units()).sum()!=512)throw new AssertionError("Mixed grids paint two full faces without filling the gap");
        if(plans.get(last).after().resolution()!=2)throw new AssertionError("Area painting preserves subdivision");
        var repeated=Map.of(first,plans.get(first).after(),last,plans.get(last).after());
        if(WorldPaintArea.plans(repeated::get,first,new DesignGeometry.Surface(0,Direction.UP),last,new DesignGeometry.Surface(7,Direction.UP),339).values().stream().anyMatch(p->p.units()!=0))throw new AssertionError("Unchanged paint is free");
        try{WorldPaintArea.plans(blocks::get,first,new DesignGeometry.Surface(0,Direction.UP),last,new DesignGeometry.Surface(7,Direction.EAST),339);throw new AssertionError("Different planes accepted");}catch(IllegalArgumentException expected){}
    }
}
