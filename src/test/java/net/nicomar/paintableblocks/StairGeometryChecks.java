package net.nicomar.paintableblocks;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.*;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.operation.*;

public final class StairGeometryChecks {
    public static void run(){
        var r=VolumeRegion.block(BlockPos.ZERO);var base=BlockDesign.full(1).paint(0,Direction.SOUTH,1);
        var stairs=StairGeometry.form(r,BlockPos.ZERO,base,0,false,false,0,16,false,false,0,true,false,0,0,-1,4);
        if(stairs.resolution()!=16||stairs.slope()!=null||!stairs.occupied(0,3,8)||stairs.occupied(0,4,8)||!stairs.occupied(15,15,8))throw new AssertionError("Four exact treads");
        var reverse=StairGeometry.form(r,BlockPos.ZERO,base,2,false,false,16,0,false,false,0,true,false,0,0,-1,4);
        if(!reverse.equals(stairs))throw new AssertionError("Swapping endpoints preserves the staircase");
        if(stairs.paint(stairs.index(0,0,15),Direction.SOUTH)!=1)throw new AssertionError("Original face paint preserved");
        var json=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,stairs).getOrThrow();if(!BlockDesign.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().equals(stairs))throw new AssertionError("Ordinary design persistence");
        var band=StairGeometry.form(r,BlockPos.ZERO,base,0,false,false,0,16,false,false,0,true,false,0,4,12,4);
        if(!band.occupied(0,15,0)||band.occupied(0,15,8))throw new AssertionError("Complete lateral strips preserved");
        var level=StairGeometry.form(r,BlockPos.ZERO,base,0,false,false,16,16,false,false,0,true,false,0,0,-1,4);
        if(!level.equals(base))throw new AssertionError("No conversion without material removal");
        try{StairGeometry.form(r,BlockPos.ZERO,base.withSlope(new SlopePlane(-1,1,0,0)),0,false,false,0,16,false,false,0,true,false,0,0,-1,4);throw new AssertionError("Oblique source accepted");}catch(IllegalArgumentException expected){}
    }
}
