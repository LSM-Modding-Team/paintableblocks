package net.nicomar.paintableblocks;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.paint.PaintCatalog;

/** Pure geometry contracts; prepared only, never run without explicit execution authorization. */
public final class GlassContractChecks {
    private static final int CLEAR=338,CONNECTED=339;
    private GlassContractChecks(){}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static BlockDesign coat(BlockDesign d,int id){
        Map<Integer,Integer> changes=new HashMap<>();for(var s:DesignGeometry.surfaces(d))changes.put(s.key(),id);return d.paintFaces(changes);
    }
    public static void run(){identities();light();contacts();borders();slopes();holes();}
    private static void identities(){
        for(int id=338;id<=373;id++){
            check(PaintCatalog.validId(id)&&PaintCatalog.isGlass(id),"Every glass identity is accepted");
            int alternate=PaintCatalog.alternateGlass(id);
            check(PaintCatalog.alternateGlass(alternate)==id&&PaintCatalog.isConnectedGlass(id)!=PaintCatalog.isConnectedGlass(alternate),"Finish conversion is an involution");
        }
        check(!PaintCatalog.isGlass(337)&&!PaintCatalog.isGlass(374),"Glass ID limits");
    }
    private static void light(){
        for(int n:PaintingBalance.RESOLUTIONS){
            var d=BlockDesign.full(n);check(d.glassLight()==null,"Existing unpainted lighting remains unchanged");
            for(int id:new int[]{338,339,340,341,370,371})check(Boolean.TRUE.equals(coat(d,id).glassLight()),"Clear/colored finishes transmit light when fully painted");
            for(int id:new int[]{372,373})check(Boolean.FALSE.equals(coat(d,id).glassLight()),"Tinted glass blocks light in both finishes");
            var transparent=coat(d,CONNECTED);var s=DesignGeometry.surfaces(d).getFirst();
            check(Boolean.FALSE.equals(transparent.paintFaces(Map.of(s.key(),0)).glassLight()),"An unpainted exposed surface blocks light");
            check(Boolean.FALSE.equals(transparent.paintFaces(Map.of(s.key(),1)).glassLight()),"An opaque exposed surface blocks light");
        }
        var ramp=coat(BlockDesign.full(4).withSlope(new SlopePlane(-1,1,0,0)),CONNECTED);
        check(Boolean.TRUE.equals(ramp.glassLight()),"Inclined surfaces participate in lighting");
        var inclined=DesignGeometry.surfaces(ramp).stream().filter(DesignGeometry.Surface::slope).findFirst().orElseThrow();
        check(Boolean.FALSE.equals(ramp.paintFaces(Map.of(inclined.key(),372)).glassLight()),"Tinted inclined surface blocks light");
    }
    private static List<BlockDesign> east(BlockDesign d){var out=new ArrayList<BlockDesign>(Collections.nCopies(6,null));out.set(Direction.EAST.ordinal(),d);return out;}
    private static void contacts(){
        var opaque=BlockDesign.full(1);var glass=coat(opaque,CONNECTED);int face=Direction.EAST.ordinal();
        check(!NeighborOcclusion.visible(opaque,east(glass)).containsKey(face),"Opaque backs remain visible through glass");
        check(NeighborOcclusion.hidden(glass,east(opaque)).contains(face),"Opaque neighbor hides the touching glass face");
        check(NeighborOcclusion.hidden(glass,east(glass)).contains(face),"Matching connected glass removes contact faces");
        check(!NeighborOcclusion.visible(glass,east(coat(opaque,341))).containsKey(face),"Different colors retain contact faces");
        check(!NeighborOcclusion.visible(glass,east(coat(opaque,CLEAR))).containsKey(face),"Different finishes retain contact faces");
        check(!NeighborOcclusion.visible(coat(opaque,CLEAR),east(coat(opaque,CLEAR))).containsKey(face),"Vanilla finish keeps vanilla contact borders");
        // Alternate equal geometry with different paints to exercise cache key invalidation.
        check(NeighborOcclusion.hidden(glass,east(glass)).contains(face),"Repainting a neighbor never reuses an incompatible cached contact");
        var contact=mesh(glass,Map.of(new BlockPos(1,0,0),coat(opaque,341))).stream().filter(f->f.surface().face()==Direction.EAST).toList();
        check(!contact.isEmpty()&&contact.stream().noneMatch(GlassConnections.Fragment::doubleSided),"Different glass contacts keep opposite fronts without duplicate coplanar backs");
        check(mesh(glass,Map.of()).stream().allMatch(GlassConnections.Fragment::doubleSided),"Exposed glass remains visible from both sides");
        var half=coat(BlockDesign.full(2),341);Map<Integer,Integer> changes=new HashMap<>();
        for(var s:DesignGeometry.surfaces(half))if(s.face()==Direction.WEST&&half.z(s.cell())==0)changes.put(s.key(),CONNECTED);
        half=half.paintFaces(changes);var fragments=NeighborOcclusion.visible(glass,east(half)).get(face);
        check(fragments!=null&&Math.abs(fragments.stream().mapToDouble(SlopeGeometry::area).sum()-.5)<1e-8,"Mixed-resolution contacts clip only the matching material area");
    }
    private static List<GlassConnections.Fragment> mesh(BlockDesign d,Map<BlockPos,BlockDesign> neighbors){
        var axial=NeighborOcclusion.neighbors(neighbors::get,BlockPos.ZERO);var exterior=NeighborOcclusion.visible(d,axial);
        var fragments=GlassConnections.fragments(d,exterior,neighbors);
        double expected=SurfaceGroups.merge(d,exterior).stream().mapToDouble(p->SlopeGeometry.area(p.polygon())).sum();
        check(Math.abs(expected-fragments.stream().mapToDouble(p->SlopeGeometry.area(p.polygon())).sum())<1e-8,"Border partition preserves the visible area exactly");
        return fragments;
    }
    private static boolean topEast(GlassConnections.Fragment f){return !f.surface().slope()&&f.surface().face()==Direction.UP&&f.border()!=null&&f.border().inward().x<-.9;}
    private static void borders(){
        var d=coat(BlockDesign.full(1),CONNECTED);
        check(mesh(d,Map.of()).stream().anyMatch(GlassContractChecks::topEast),"Isolated connected glass retains its perimeter");
        for(int n:PaintingBalance.RESOLUTIONS)for(int rotation=0;rotation<24;rotation++){
            var other=coat(BlockDesign.full(n),CONNECTED).withOrientation(rotation);
            check(mesh(d,Map.of(new BlockPos(1,0,0),other)).stream().noneMatch(GlassContractChecks::topEast),"Equal coplanar paint joins across subdivisions and orientations");
        }
        check(mesh(d,Map.of(new BlockPos(1,0,0),coat(BlockDesign.full(1),341))).stream().anyMatch(GlassContractChecks::topEast),"Different materials retain the frame");
        check(mesh(d,Map.of(new BlockPos(1,0,0),coat(BlockDesign.full(1),CLEAR))).stream().anyMatch(GlassContractChecks::topEast),"Vanilla finish cannot join connected finish");
        // World vanilla glass has no design/model data, so contributes no connection patch.
        check(mesh(d,Map.of()).stream().filter(GlassContractChecks::topEast).count()==1,"No connection through a non-paintable neighbor");
        var half=BlockDesign.full(2);Map<Integer,Integer> changes=new HashMap<>();
        for(var s:DesignGeometry.surfaces(half))if(s.face()==Direction.UP&&half.z(s.cell())==0)changes.put(s.key(),CONNECTED);
        half=half.paintFaces(changes);
        var borders=mesh(d,Map.of(new BlockPos(1,0,0),half)).stream().filter(GlassContractChecks::topEast).toList();
        check(borders.size()==1&&Math.abs(borders.getFirst().border().length()-.5)<1e-8,"Partial-edge neighbors remove only the shared interval");
        // Count coverage away from edges to catch coplanar overlay or missing interior polygons.
        var top=mesh(d,Map.of()).stream().filter(f->f.surface().face()==Direction.UP).toList();
        for(int x=0;x<32;x++)for(int z=0;z<32;z++){
            Vec3 p=new Vec3((x+.37)/32,1,(z+.41)/32);
            check(top.stream().filter(f->SlopeGeometry.contains(f.polygon(),p,new Vec3(0,1,0))).count()==1,"Border and interior have no overlapping area");
        }
    }
    private static void slopes(){
        var ramp=coat(BlockDesign.full(4).withSlope(new SlopePlane(-1,1,0,0)),CONNECTED);
        check(mesh(ramp,Map.of(new BlockPos(0,0,1),ramp)).stream().noneMatch(f->f.surface().slope()&&f.border()!=null&&f.border().inward().z<-.9),"Coplanar ramps join across blocks");
        check(mesh(ramp,Map.of(new BlockPos(1,1,0),ramp)).stream().noneMatch(f->f.surface().slope()&&f.border()!=null&&Math.abs(f.border().start().x-1)<1e-8&&Math.abs(f.border().start().y-1)<1e-8&&Math.abs(f.border().along().z)>.9),"Diagonal neighbors join an inclined plane");
        var reversed=coat(BlockDesign.full(4).withSlope(new SlopePlane(1,1,0,1)),CONNECTED);
        check(mesh(ramp,Map.of(new BlockPos(0,0,1),reversed)).stream().anyMatch(f->f.surface().slope()&&f.border()!=null&&f.border().inward().z<-.9),"Changes of slope preserve the perimeter");
    }
    private static void holes(){
        var d=coat(BlockDesign.full(4),CONNECTED);BitSet removed=new BitSet();removed.set(d.index(1,3,1));var cut=d.remove(removed);
        check(Boolean.FALSE.equals(cut.glassLight()),"Freshly cut surfaces are unpainted and block glass lighting");
        check(DesignGeometry.surfaces(cut).stream().anyMatch(s->cut.paint(s)==0),"Cuts do not copy glass to new surfaces");
        var fragments=mesh(coat(cut,CONNECTED),Map.of());
        check(fragments.stream().anyMatch(f->f.surface().face()==Direction.UP&&f.border()!=null&&f.border().start().x>.2&&f.border().start().x<.6&&f.border().start().z>.2&&f.border().start().z<.6),"Frames remain around holes");
    }
}
