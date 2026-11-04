package net.nicomar.paintableblocks.interaction;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.item.SlopeFormerItem;

/** One bounded in-memory undo per player. Does not grant items, refund durability, or load chunks. */
final class SlopeUndo {
    record Entry(BlockPos pos,BlockState before,CompoundTag tag,BlockState after,CompoundTag expected,net.nicomar.paintableblocks.design.BlockDesign original){}
    private static final class History{final Identifier dimension;final List<Entry> entries;int weight;History(Identifier d,List<Entry> e){dimension=d;entries=e;}}
    private static final Map<ServerPlayer,History> HISTORY=new WeakHashMap<>();
    private static final Set<ServerPlayer> STARTING=Collections.newSetFromMap(new WeakHashMap<ServerPlayer,Boolean>());
    static void begin(ServerPlayer p){STARTING.add(p);}
    static void record(ServerPlayer p,BlockPos pos,BlockState before,CompoundTag tag,net.nicomar.paintableblocks.design.BlockDesign original){
        if(STARTING.remove(p))HISTORY.put(p,new History(p.level().dimension().identifier(),new ArrayList<>()));
        var h=HISTORY.get(p);if(h==null||h.entries.size()>=8192)return;
        var be=p.level().getBlockEntity(pos);var expected=be==null?null:be.saveWithoutMetadata(p.level().registryAccess());h.weight+=(tag==null?0:tag.toString().length())+(expected==null?0:expected.toString().length());if(h.weight>8_388_608){HISTORY.remove(p);return;}h.entries.add(new Entry(pos.immutable(),before,tag,p.level().getBlockState(pos),expected,original));
    }
    private static boolean matches(ServerPlayer p,Entry e){
        if(!p.level().hasChunkAt(e.pos)||!p.level().getBlockState(e.pos).equals(e.after))return false;
        var be=p.level().getBlockEntity(e.pos);return Objects.equals(e.expected,be==null?null:be.saveWithoutMetadata(p.level().registryAccess()));
    }
    private static boolean unobstructed(ServerPlayer p,Entry e){
        if(e.tag==null||e.original.empty())return true;
        var shape=net.nicomar.paintableblocks.design.DesignGeometry.shape(e.original).move(e.pos.getX(),e.pos.getY(),e.pos.getZ());if(shape.isEmpty())return true;
        for(var entity:p.level().getEntityCollisions(null,shape.bounds()))if(net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(shape,entity,net.minecraft.world.phys.shapes.BooleanOp.AND))return false;
        return true;
    }
    static boolean undo(ServerPlayer p){
        var h=HISTORY.get(p);InteractionHand hand=p.getMainHandItem().getItem() instanceof SlopeFormerItem?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;
        if(h==null||h.entries.isEmpty()||!h.dimension.equals(p.level().dimension().identifier())||!(p.getItemInHand(hand).getItem() instanceof SlopeFormerItem))return false;
        for(var e:h.entries)if(!matches(p,e)||!unobstructed(p,e)||!VolumeEditing.allowed(p,e.pos,hand))return false;
        // Use the placement protection hook for restoration, including recreation of an erased piece.
        for(var e:h.entries){if(e.before.isAir()&&net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.block.BreakBlockEvent(p.level(),e.pos,p.level().getBlockState(e.pos),p)).isCanceled())return false;var snapshot=net.neoforged.neoforge.common.util.BlockSnapshot.create(p.level().dimension(),p.level(),e.pos);var event=new net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent(snapshot,p.level().getBlockState(e.pos.below()),p){@Override public BlockState getPlacedBlock(){return e.before;}@Override public BlockState getState(){return e.before;}};if(net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event).isCanceled())return false;}
        for(var e:h.entries)if(!matches(p,e)||!unobstructed(p,e))return false;
        HISTORY.remove(p);
        for(var e:h.entries){
            p.level().setBlock(e.pos,e.before,Block.UPDATE_ALL);
            var be=p.level().getBlockEntity(e.pos);
            if(be!=null&&e.tag!=null){be.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,p.level().registryAccess(),e.tag));be.setChanged();p.level().sendBlockUpdated(e.pos,e.before,e.before,Block.UPDATE_CLIENTS);if(be instanceof PaintableBlockEntity paintable)net.nicomar.paintableblocks.network.DesignUpdates.nativeUpdate(paintable);}
        }
        return true;
    }
}
