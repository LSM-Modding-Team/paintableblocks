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
    private static void fractional(){
        var region=new VolumeRegion(-12,4,-8,28,16,12);
        for(int n:List.of(1,2,4,8,16))for(int direction=0;direction<4;direction++)for(boolean upper:List.of(false,true)){
            double area=0;
            for(long i=0;i<region.size();i++){
                var pos=region.position(i);var d=BlockDesign.full(n).withSlope(region.creationPlane(pos,BlockDesign.full(n),direction,upper));
                check(d.resolution()==n,"Dimension precision never changes editing subdivision");
                for(var surface:DesignGeometry.surfaces(d)){
                    var polygon=SlopeGeometry.polygon(d,surface);
                    for(var v:polygon)check(region.contains(v.add(pos.getX(),pos.getY(),pos.getZ())),"No surface exceeds fractional bounds");
                    if(surface.slope())area+=SlopeGeometry.area(polygon);
                }
                var encoded=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,d).getOrThrow();
                check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow().equals(d),"Fractional bounds persist");
                if(d.slope()!=null)for(int rotation=0;rotation<24;rotation++)
                    check(d.slope().rotated(rotation).rotated(DesignOrientation.inverse(rotation)).equals(d.slope()),"Bounds rotate without drift");
            }
            double length=direction%2==0?2.5:1.25,width=direction%2==0?1.25:2.5;
            check(Math.abs(area-Math.hypot(length,.75)*width)<1e-7,"Fractional ramp has exact continuous area");
        }
    }
    private static void profiles(){
        var region=VolumeRegion.block(BlockPos.ZERO);
        for(int n:List.of(1,2,4,8,16))for(int direction=0;direction<4;direction++)for(boolean vertical:List.of(false,true))for(boolean upper:List.of(false,true)){
            var full=BlockDesign.full(n);
            for(int[] levels:List.of(new int[]{8,16},new int[]{16,8},new int[]{8,8},new int[]{0,16})){
                var p=region.profilePlane(BlockPos.ZERO,full,direction,upper,vertical,levels[0],levels[1],true);
                var d=full.withSlope(p);
                var encoded=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,d).getOrThrow();
                check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow().equals(d),"Profile survives persistence");
                check(vertical?p.b()==0:p.b()!=0,"Vertical profile is extruded along Y");
                for(int rotation=0;rotation<24;rotation++)check(p.rotated(rotation).rotated(DesignOrientation.inverse(rotation)).equals(p),"Profile supports all rotations");
            }
        }
        var full=BlockDesign.full(4);
        var ramp=full.withSlope(region.profilePlane(BlockPos.ZERO,full,0,false,false,8,16,true));
        var hit=new DesignOutlineShape(ramp).clip(new Vec3(.25,2,.5),new Vec3(.25,-1,.5),BlockPos.ZERO);
        check(hit!=null&&Math.abs(hit.getLocation().y-.625)<1e-8,"Raised ramp starts at half height without translating its base");
        check(!DesignGeometry.shape(ramp).getFaceShape(Direction.DOWN).isEmpty(),"Raised ramp retains a solid bottom");
        var wall=full.withSlope(region.profilePlane(BlockPos.ZERO,full,0,false,true,8,16,true));
        var side=new DesignOutlineShape(wall).clip(new Vec3(.25,.75,2),new Vec3(.25,.75,-1),BlockPos.ZERO);
        check(side!=null&&Math.abs(side.getLocation().z-.625)<1e-8,"Vertical profile creates a diagonal wall");
        var slab=full.withSlope(region.profilePlane(BlockPos.ZERO,full,0,false,false,8,8,true));
        var flat=new DesignOutlineShape(slab).clip(new Vec3(.5,2,.5),new Vec3(.5,-1,.5),BlockPos.ZERO);
        check(flat!=null&&Math.abs(flat.getLocation().y-.5)<1e-8,"Equal endpoints create a flat cap");
        reject(()->region.profilePlane(BlockPos.ZERO,full,0,false,false,17,16,true));
    }
    private static void combinations(){
        var region=VolumeRegion.block(BlockPos.ZERO);var full=BlockDesign.full(1);
        var piece=full.withSlope(region.combinedPlane(BlockPos.ZERO,full,0,false,false,0,16,true,true,8,false));
        var ramp=new DesignOutlineShape(piece).clip(new Vec3(.25,2,.5),new Vec3(.25,-1,.5),BlockPos.ZERO);
        var plateau=new DesignOutlineShape(piece).clip(new Vec3(.75,2,.5),new Vec3(.75,-1,.5),BlockPos.ZERO);
        check(ramp!=null&&Math.abs(ramp.getLocation().y-.5)<1e-8,"Half incline reaches the junction continuously");
        check(plateau!=null&&Math.abs(plateau.getLocation().y-1)<1e-8,"Complete section and incline coexist inside one resolution-one piece");
        for(int direction=0;direction<4;direction++)for(boolean vertical:List.of(false,true))for(boolean upper:List.of(false,true))for(boolean first:List.of(false,true))for(int length:new int[]{0,4,8,12,16}){
            var p=region.combinedPlane(BlockPos.ZERO,full,direction,upper,vertical,0,0,true,true,length,first);
            var d=full.withSlope(p);check(!d.empty(),"Combined profile retains material");
            var encoded=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,d).getOrThrow();check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow().equals(d),"Combined geometry persists without a new format");
            for(int rotation=0;rotation<24;rotation++)check(p.rotated(rotation).rotated(DesignOrientation.inverse(rotation)).equals(p),"Combined profile rotates with its complete section");
            if(length==16)check(d.count()==1,"Entire complete section remains a full piece");
        }
        var legacy=BlockDesign.full(2).withSlope(new SlopePlane(-1,1,0,0));
        var json=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,legacy).getOrThrow().getAsJsonObject();
        json.addProperty("version",2);json.getAsJsonObject("slope").remove("bounds");
        check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().equals(legacy),"Version two slopes without bounds upgrade without shape loss");
        reject(()->region.combinedPlane(BlockPos.ZERO,full,0,false,false,0,16,true,true,17,false));
    }
    private static void bothEnds(){
        var r=VolumeRegion.block(BlockPos.ZERO);var base=BlockDesign.full(16);
        var p=r.doublePlane(BlockPos.ZERO,base,0,false,false,0,16,true,true,4,true,true,4);
        var d=r.doubleSeed(BlockPos.ZERO,base).withSlope(p);
        for(double x:new double[]{.125,.875}){
            var hit=new DesignOutlineShape(d).clip(new Vec3(x,2,.5),new Vec3(x,-1,.5),BlockPos.ZERO);
            check(hit!=null&&Math.abs(hit.getLocation().y-1)<1e-8,"Both ends retain full height");
        }
        var middle=new DesignOutlineShape(d).clip(new Vec3(.5,2,.5),new Vec3(.5,-1,.5),BlockPos.ZERO);
        check(middle!=null&&Math.abs(middle.getLocation().y-.5)<1e-8,"Incline stays inside its two joins");
        var step=new DesignOutlineShape(d).clip(new Vec3(.5,.75,.5),new Vec3(0,.75,.5),BlockPos.ZERO);
        check(step!=null&&Math.abs(step.getLocation().x-.25)<1e-8,"Join wall is exposed and selectable");
        var json=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,d).getOrThrow();
        check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().equals(d),"Cut window persists with paints and occupancy");
        for(int orientation=0;orientation<24;orientation++)check(p.rotated(orientation).rotated(DesignOrientation.inverse(orientation)).equals(p),"Both joins rotate without drift");
        var old=BlockDesign.full(4).withSlope(new SlopePlane(-1,1,0,0));
        var legacy=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,old).getOrThrow().getAsJsonObject();legacy.addProperty("version",3);legacy.getAsJsonObject("slope").remove("window");
        check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,legacy).getOrThrow().equals(old),"Existing format three worlds retain their original slope");
        reject(()->r.doublePlane(BlockPos.ZERO,base,0,false,false,0,16,true,true,12,true,true,12));
    }
    private static void normalization(){
        check(new SlopePlane(6,9,0,12).equals(new SlopePlane(2,3,0,4)),"Normalizing a plane divides the offset too");
        check(new SlopePlane(-24,12,0,-18).equals(new SlopePlane(-4,2,0,-3)),"Negative offsets retain their plane position");
        reject(()->new SlopePlane(Integer.MIN_VALUE,1,0,0));
        for(int origin:new int[]{-1000000,-17,0,101,1000000}){
            var r=VolumeRegion.block(new BlockPos(origin,64,-3)).union(VolumeRegion.block(new BlockPos(origin+5,68,-3)));
            for(int block=0;block<6;block++){
                var pos=new BlockPos(origin+block,64,-3);var p=r.profilePlane(pos,BlockDesign.full(1),0,false,false,0,80,false);
                double localX=.375,localY=5*(block+localX)/6;
                check(Math.abs(p.value(new Vec3(localX,localY,.5)))<1e-8,"Every block uses the same continuous global plane");
                var rotated=p.rotated(7).rotated(DesignOrientation.inverse(7));check(rotated.equals(p),"Plane position is stable across rotations");
            }
        }
        var stored=BlockDesign.full(4).withSlope(new SlopePlane(-1,1,0,0));var json=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,stored).getOrThrow();
        check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().equals(stored),"Existing canonical saved geometry is unchanged");
    }
    private static void independentJoin(){
        var region=VolumeRegion.block(BlockPos.ZERO);var base=BlockDesign.full(16);
        var plane=region.doublePlane(BlockPos.ZERO,base,0,false,false,0,16,true,true,4,true,true,0);
        var d=region.doubleSeed(BlockPos.ZERO,base).withSlope(plane);var outline=new DesignOutlineShape(d);
        var cap=outline.clip(new Vec3(.125,2,.5),new Vec3(.125,-1,.5),BlockPos.ZERO);
        var ramp=outline.clip(new Vec3(.5,2,.5),new Vec3(.5,-1,.5),BlockPos.ZERO);
        check(cap!=null&&Math.abs(cap.getLocation().y-1)<1e-8,"Full initial zone stays complete");
        check(ramp!=null&&Math.abs(ramp.getLocation().y-1.0/3)<1e-8,"Independent ramp ascends after the full initial zone instead of being forced down");
        var wall=outline.clip(new Vec3(.5,.8,.5),new Vec3(0,.8,.5),BlockPos.ZERO);
        check(wall!=null&&Math.abs(wall.getLocation().x-.25)<1e-8,"Independent join has the vertical wall requested");
    }
    private static void coarseWorldInteraction(){
        var r=VolumeRegion.block(BlockPos.ZERO);
        for(int n:List.of(1,2,4,8,16)){
            var base=BlockDesign.full(n);var p=r.doublePlane(BlockPos.ZERO,base,0,false,false,0,16,true,true,4,true,true,4);
            var d=r.doubleSeed(BlockPos.ZERO,base).withSlope(p);
            check(d.resolution()==n,"World interaction never converts the logical grid");
            var top=new DesignOutlineShape(d).clip(new Vec3(.125,2,.5),new Vec3(.125,-1,.5),BlockPos.ZERO);
            var ramp=new DesignOutlineShape(d).clip(new Vec3(.5,2,.5),new Vec3(.5,-1,.5),BlockPos.ZERO);
            var wall=new DesignOutlineShape(d).clip(new Vec3(.5,.75,.5),new Vec3(0,.75,.5),BlockPos.ZERO);
            check(top!=null&&Math.abs(top.getLocation().y-1)<1e-8,"Coarse grid keeps full end");
            check(ramp!=null&&Math.abs(ramp.getLocation().y-.5)<1e-8,"Coarse grid keeps exact incline");
            check(wall!=null&&Math.abs(wall.getLocation().x-.25)<1e-8,"Join may cross a coarse cell");
            var json=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,d).getOrThrow();check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().equals(d),"Coarse fragments survive saving");
        }
        var point=new Vec3(-.125,.5,1);
        var cell=VolumeRegion.aim(new BlockPos(-1,0,0),point,Direction.SOUTH);
        check(cell.x1()-cell.x0()==1&&cell.y1()-cell.y0()==1&&cell.z1()-cell.z0()==1,"Interaction always selects a sixteenth even at negative coordinates");
    }
    private static void lateralBands(){
        var region=VolumeRegion.block(BlockPos.ZERO);
        for(int n:List.of(1,2,4,8,16)){
            var base=BlockDesign.full(n);var original=region.doublePlane(BlockPos.ZERO,base,0,false,false,0,16,true,true,0,true,true,0);
            var plane=region.lateralPlane(BlockPos.ZERO,base,original,0,false,4,12);var d=base.withSlope(plane);
            check(d.resolution()==n,"Lateral bands preserve logical subdivision");
            for(double z:new double[]{.125,.5,.875}){
                var hit=new DesignOutlineShape(d).clip(new Vec3(.5,2,z),new Vec3(.5,-1,z),BlockPos.ZERO);
                check(hit!=null&&Math.abs(hit.getLocation().y-(z==.5?.5:1))<1e-8,"Only the central lateral band is inclined");
            }
            var wall=new DesignOutlineShape(d).clip(new Vec3(.5,.75,.5),new Vec3(.5,.75,0),BlockPos.ZERO);
            check(wall!=null&&Math.abs(wall.getLocation().z-.25)<1e-8,"Lateral join wall is ray traceable");
            var json=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,d).getOrThrow();check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().equals(d),"Lateral window survives save/load");
            for(int rotation=0;rotation<24;rotation++){
                var point=new Vec3(.5,.75,.5);var rotated=plane.rotated(rotation);
                check(rotated.cuts(DesignOrientation.rotate(rotation,point))==plane.cuts(point),"Band follows piece rotation");
            }
            var legacy=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,base.withSlope(original)).getOrThrow().getAsJsonObject();legacy.addProperty("version",4);
            check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,legacy).getOrThrow().equals(base.withSlope(original)),"Format four keeps its geometry");
        }
    }
    public static void run(){
        lateralBands();
        coarseWorldInteraction();
        independentJoin();
        normalization();
        bothEnds();
        combinations();
        fractional();profiles();
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
        var original=BlockDesign.full(4).paint(0,Direction.DOWN,337);var old=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,original).getOrThrow().getAsJsonObject();old.addProperty("version",1);old.remove("slope");check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,old).getOrThrow().equals(original),"Version one worlds retain their designs");
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
