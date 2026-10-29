package net.nicomar.paintableblocks;

import com.mojang.serialization.JsonOps;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.operation.*;

/** Behavioral regression harness. Prepared for an explicitly authorized build; not run by static tools. */
public final class SlopeVolumeContractChecks {
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
    private static void reject(Runnable action){try{action.run();throw new AssertionError("Expected rejection");}catch(IllegalArgumentException|IllegalStateException expected){}}
    public static void run(){
        var basic=new SlopePlane(-1,1,0,0);
        var invalid=new com.google.gson.JsonObject();invalid.addProperty("a",0);invalid.addProperty("b",0);invalid.addProperty("c",0);invalid.addProperty("offset",0);
        check(SlopePlane.CODEC.parse(JsonOps.INSTANCE,invalid).error().isPresent(),"Invalid plane returns codec error instead of escaping quarantine");
        for(int n:List.of(1,2,4,8,16)){
            var d=BlockDesign.full(n).withSlope(basic);check(!d.complete()&&!d.empty(),"Slope is cut even at 1x1");
            var surfaces=DesignGeometry.surfaces(d);double area=surfaces.stream().filter(DesignGeometry.Surface::slope).mapToDouble(s->SlopeGeometry.area(SlopeGeometry.polygon(d,s))).sum();
            check(Math.abs(area-Math.sqrt(2))<1e-8,"Inclined area is real, not its projection");
            var cap=surfaces.stream().filter(DesignGeometry.Surface::slope).findFirst().orElseThrow();
            check(DesignOperations.paint(d,List.of(cap),1).units()==SlopeGeometry.units(d,cap),"Per-fragment area consumption");
            var painted=DesignOperations.paint(d,List.of(cap),1).after();check(painted.paint(cap)==1,"Slope paint is distinct from six axial faces");
            check(DesignOperations.paint(painted,List.of(cap),1).units()==0,"Identical repaint costs zero");
            check(DesignOperations.tool(painted,cap,DesignOperations.Action.SCRAPE,Direction.UP,false).after().paints().isEmpty(),"Scraper clears slope plane");
            check(DesignOperations.area(d,cap,cap).contains(cap),"Area includes a triangular corner fragment");
            if(n==1)check(!DesignOperations.tool(d,cap,DesignOperations.Action.HAMMER,Direction.UP,false).changes(),"Hammer cannot carve resolution one");
            reject(()->d.converted(4));reject(()->d.withSlope(new SlopePlane(1,1,0,1)));check(d.withSlope(basic).equals(d),"Reapplying same plane is stable");
            var encoded=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,painted).getOrThrow();check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow().equals(painted),"Slope and paint codec round trip");
            for(int rotation=0;rotation<24;rotation++){
                var rotated=painted.withOrientation(rotation);var physical=rotated.physical();check(physical.paints().size()==1,"Paint survives physical rotation");
                check(basic.rotated(rotation).rotated(DesignOrientation.inverse(rotation)).equals(basic),"Integer plane rotation round trip");
                double rotatedArea=DesignGeometry.surfaces(physical).stream().filter(DesignGeometry.Surface::slope).mapToDouble(s->SlopeGeometry.area(SlopeGeometry.polygon(physical,s))).sum();check(Math.abs(rotatedArea-Math.sqrt(2))<1e-8,"All proper rotations preserve slope geometry");
            }
            var hit=new DesignOutlineShape(d).clip(new Vec3(.25,2,.5),new Vec3(.25,-1,.5),BlockPos.ZERO);check(hit!=null&&Math.abs(hit.getLocation().y-.25)<1e-8,"Ray hits real incline rather than collision steps");
            var target=DesignGeometry.target(d,hit.getLocation(),hit.getDirection()).orElseThrow();check(target.slope(),"Ray selects inclined paint fragment");
        }
        var original=BlockDesign.full(4).paint(0,Direction.DOWN,337);var old=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,original).getOrThrow().getAsJsonObject();old.addProperty("version",1);old.remove("slope");check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,old).getOrThrow().equals(original),"Version one migrates without paint loss");
        var max=VolumeRegion.block(BlockPos.ZERO).union(VolumeRegion.block(new BlockPos(255,255,255)));check(max.valid(256)&&max.size()==16777216,"Inclusive 256 block boundary");check(!max.union(VolumeRegion.block(new BlockPos(256,0,0))).valid(256),"257 blocks rejected");
        var small=VolumeRegion.cell(BlockPos.ZERO,BlockDesign.full(16),0);check(small.hammer(BlockPos.ZERO,BlockDesign.full(16)).cells().size()==1,"Sixteenth selection only removes its one cell");check(small.hammer(BlockPos.ZERO,BlockDesign.full(2)).cells().isEmpty(),"Mixed grids use centers, not any overlap");
        for(int direction=0;direction<4;direction++)for(boolean upper:List.of(false,true)){
            var region=VolumeRegion.block(new BlockPos(-3,2,-5)).union(VolumeRegion.block(new BlockPos(4,5,1)));double capArea=0;
            for(long i=0;i<region.size();i++){var pos=region.position(i);var d=BlockDesign.full(1).withSlope(region.plane(pos,BlockDesign.full(1),direction,upper));if(!d.empty())for(var surface:DesignGeometry.surfaces(d))if(surface.slope())capArea+=SlopeGeometry.area(SlopeGeometry.polygon(d,surface));}
            double length=direction%2==0?region.width():region.depth(),width=direction%2==0?region.depth():region.width();check(Math.abs(capArea-Math.hypot(length,region.height())*width)<1e-7,"Multi-block slope has continuous analytic area");
        }
        var whole=BlockDesign.full(1);var side=BlockDesign.full(2);var removed=new BitSet();for(int c=0;c<8;c++)if(side.x(c)==0)removed.set(c);side=side.remove(removed);
        var partial=new ArrayList<BlockDesign>(Collections.nCopies(6,null));partial.set(Direction.SOUTH.ordinal(),side);var exterior=NeighborOcclusion.visible(whole,partial);var south=new DesignGeometry.Surface(0,Direction.SOUTH);
        check(Math.abs(exterior.get(south.key()).stream().mapToDouble(SlopeGeometry::area).sum()-.5)<1e-9,"Partial neighbor contact retains exactly the uncovered half");
        var ramp=BlockDesign.full(2).withSlope(basic);List<BlockDesign> adjacent=new ArrayList<>(Collections.nCopies(6,null));adjacent.set(Direction.SOUTH.ordinal(),BlockDesign.full(16).withSlope(basic));var hidden=NeighborOcclusion.hidden(ramp,adjacent);
        check(DesignGeometry.surfaces(ramp).stream().filter(s->s.face()==Direction.SOUTH&&!s.slope()).allMatch(s->hidden.contains(s.key())),"Mixed-resolution contiguous ramp hides its interior side");
    }
}
