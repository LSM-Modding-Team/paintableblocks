package net.nicomar.paintableblocks;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.*;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.operation.*;

public final class CreativePaintShellChecks {
    public static void run(){
        var full=BlockDesign.full(2);
        var half=new VolumeRegion(0,0,0,8,16,16);
        var plans=CreativePaintShell.plans(p->p.equals(BlockPos.ZERO)?full:null,half,1);
        var result=plans.get(BlockPos.ZERO).after();
        if(result.paint(0,Direction.EAST)!=1||result.exposed(0,Direction.EAST))throw new AssertionError("Selection boundary stores a concealed face");
        if(result.paint(1,Direction.WEST)!=0||result.paint(0,Direction.UP)!=0)throw new AssertionError("Interior faces remain untouched");
        var json=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,result).getOrThrow();
        if(!BlockDesign.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().equals(result))throw new AssertionError("Concealed boundary paint survives saving");
        var whole=CreativePaintShell.plans(p->p.equals(BlockPos.ZERO)?full:null,VolumeRegion.block(BlockPos.ZERO),1).get(BlockPos.ZERO).after();
        if(whole.paints().size()!=24||whole.paint(0,Direction.EAST)!=0)throw new AssertionError("Only the six outer boundaries are painted");
        var region=new VolumeRegion(0,0,0,2048*16,16,16);
        if(!CreativePaintShell.plans(p->null,region,1).isEmpty())throw new AssertionError("Empty positions remain empty");
        try{CreativePaintShell.plans(p->null,new VolumeRegion(0,0,0,2049*16,16,16),1);throw new AssertionError("Position limit ignored");}catch(IllegalArgumentException expected){}
    }
}
