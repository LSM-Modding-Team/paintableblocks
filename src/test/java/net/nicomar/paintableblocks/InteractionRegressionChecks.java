package net.nicomar.paintableblocks;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.BitSet;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.menu.PaintBucketMenu;
import net.nicomar.paintableblocks.network.ModNetworking;
import net.nicomar.paintableblocks.operation.DesignOperations;

/** Development-classpath contracts. Prepared only; no Java/build/game execution is authorized. */
public final class InteractionRegressionChecks {
    private static int checks;
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    public static void main(String[] args){protocol();placementScope();geometryReuse();conservedCopies();System.out.println("Interaction regression contracts: "+checks);}
    private static void protocol(){
        for(var hand:InteractionHand.values()){
            var message=new ModNetworking.Use(1L<<40,12345678910L,hand,hand==InteractionHand.MAIN_HAND?8:-1,Identifier.parse("minecraft:the_nether"),new BlockPos(-31,64,17),new Vec3(-30.125,64.5,17.0625),Direction.EAST,Identifier.parse("paintableblocks:netherite_brush"),337,UUID.randomUUID(),1L<<38,true,true,false);
            var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
            try{ModNetworking.Use.CODEC.encode(buffer,message);check(message.equals(ModNetworking.Use.CODEC.decode(buffer)),"Intent fields, hand and long revision survive the wire");check(buffer.readableBytes()==0,"Intent codec consumes all bytes");}finally{buffer.release();}
            var response=new ModNetworking.Selection(message.request(),message.selection(),true,message.dimension(),hand,message.slot(),message.item(),message.paint(),message.pos(),message.block(),message.revision(),4095,5);
            buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
            try{ModNetworking.Selection.CODEC.encode(buffer,response);check(response.equals(ModNetworking.Selection.CODEC.decode(buffer)),"Selection retains request, dimension, identity and revision");}finally{buffer.release();}
        }
        var command=new ModNetworking.BucketAction(1234,PaintBucketMenu.WITHDRAW_STACK+337);
        var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try{ModNetworking.BucketAction.CODEC.encode(buffer,command);check(command.equals(ModNetworking.BucketAction.CODEC.decode(buffer)),"Paint identity cannot be truncated to a native button byte");}finally{buffer.release();}
    }
    private static void placementScope(){
        var pos=new BlockPos(12,80,-15);var first=BlockDesign.full(4).withOrientation(7);var nested=BlockDesign.full(16).withOrientation(23);
        check(PlacementDesign.current(pos,4)==null,"No design outside placement");
        try(var outer=PlacementDesign.enter(pos,first)){
            check(PlacementDesign.current(pos,4).equals(first),"Native callbacks see the final placement design");
            check(PlacementDesign.current(pos.above(),4)==null&&PlacementDesign.current(pos,2)==null,"Placement never leaks to another block or resolution");
            try{try(var inner=PlacementDesign.enter(pos,nested)){check(PlacementDesign.current(pos,16).equals(nested),"Nested placement owns its design");throw new IllegalStateException("abort nested placement");}}catch(IllegalStateException expected){}
            check(PlacementDesign.current(pos,4).equals(first),"Aborted nested placement restores the outer context");
        }
        check(PlacementDesign.current(pos,4)==null,"Finished placement removes the scoped state");
    }
    private static void geometryReuse(){
        var original=BlockDesign.full(4);int cell=original.index(0,3,0);var painted=original.paint(cell,Direction.UP,337);
        check(original.geometryKey().equals(painted.geometryKey()),"Paint cannot change the geometry cache key");
        check(DesignGeometry.surfaces(original)==DesignGeometry.surfaces(painted),"Painting reuses the exposure list");
        var face=new DesignGeometry.Surface(cell,Direction.UP);check(DesignOperations.paint(painted,java.util.List.of(face),337).faces().isEmpty(),"An unchanged paint surface is not highlighted as affected");
        BitSet removed=new BitSet();removed.set(cell);var cut=painted.remove(removed);
        check(!cut.geometryKey().equals(painted.geometryKey()),"Carving invalidates cached exposure geometry");
        check(DesignGeometry.boxes(cut).equals(DesignGeometry.boxes(cut.withoutPaint())),"Collision geometry is independent of paint");
    }
    private static void conservedCopies(){
        var source=BlockDesign.full(4);BitSet removed=new BitSet();removed.set(source.index(1,3,1));source=source.remove(removed);
        source=source.paintFaces(Map.of(source.index(1,2,1)*6+Direction.UP.ordinal(),337,source.index(0,3,0)*6+Direction.UP.ordinal(),1));
        for(int orientation=0;orientation<24;orientation++){
            var design=source.withOrientation(orientation);var encoded=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,design).getOrThrow();var restored=BlockDesign.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow();
            check(design.equals(restored),"Copy/save roundtrip retains cuts, inner paints and orientation");
            for(var facing:java.util.List.of(Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST))for(boolean inverted:new boolean[]{false,true}){
                int placement=DesignOrientation.placement(facing,inverted);var placed=restored.withOrientation(DesignOrientation.compose(placement,restored.orientation()));
                check(placed.count()==source.count()&&placed.paints().equals(source.paints()),"Placement rotates the conserved design without discarding paint");
                check(DesignOrientation.compose(DesignOrientation.inverse(placement),placed.orientation())==orientation,"Inverse placement recovers all 24 saved orientations");
            }
        }
    }
}
