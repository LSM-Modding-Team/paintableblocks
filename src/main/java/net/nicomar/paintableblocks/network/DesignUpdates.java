package net.nicomar.paintableblocks.network;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.design.BlockDesign;

/** One update per block/tick. Persistent/chunk data remains complete; edits use revision-guarded deltas. */
public final class DesignUpdates {
    private record Pending(BlockDesign before,long revision){}
    private static final Map<PaintableBlockEntity,Pending> PENDING=new LinkedHashMap<>();
    private static int retainedWeight;
    private static int weight(Pending pending){return pending.before==null?0:pending.before.paints().size()+64;}
    private static final Map<ServerPlayer,Long> RESYNC=new WeakHashMap<>();
    public static void register(){NeoForge.EVENT_BUS.addListener(DesignUpdates::tick);NeoForge.EVENT_BUS.addListener(DesignUpdates::stop);}
    private static void stop(ServerStoppedEvent e){PENDING.clear();RESYNC.clear();retainedWeight=0;}
    public static void changed(PaintableBlockEntity be,BlockDesign before,long revision){
        if(PENDING.containsKey(be))return;var pending=new Pending(before,revision);if(retainedWeight+weight(pending)>131072)pending=new Pending(null,-1);PENDING.put(be,pending);retainedWeight+=weight(pending);
    }
    public static void nativeUpdate(PaintableBlockEntity be){var pending=PENDING.remove(be);if(pending!=null)retainedWeight-=weight(pending);}
    private static ModNetworking.DesignSync packet(PaintableBlockEntity be,Pending pending){
        if(pending==null||pending.before==null)return full(be);
        var before=pending.before;var after=be.design();var removed=new BitSet();for(int c=0;c<before.resolution()*before.resolution()*before.resolution();c++){if(!before.occupied(c)&&after.occupied(c))return full(be);if(before.occupied(c)&&!after.occupied(c))removed.set(c);}if(before.slope()!=null&&after.slope()==null)return full(be);
        Map<Integer,Integer> paints=new TreeMap<>();for(var e:before.paints().entrySet())if(after.paints().getOrDefault(e.getKey(),0).intValue()!=e.getValue())paints.put(e.getKey(),after.paints().getOrDefault(e.getKey(),0));for(var e:after.paints().entrySet())if(before.paints().getOrDefault(e.getKey(),0).intValue()!=e.getValue())paints.put(e.getKey(),e.getValue());
        if(before.resolution()!=after.resolution()||paints.size()>1024)return full(be);
        return new ModNetworking.DesignSync(be.getLevel().dimension().identifier(),be.getBlockPos(),be.editIdentity(),pending.revision,be.revision(),null,Arrays.stream(removed.toLongArray()).boxed().toList(),Map.copyOf(paints),after.orientation(),Objects.equals(before.slope(),after.slope())?null:after.slope());
    }
    private static ModNetworking.DesignSync full(PaintableBlockEntity be){return new ModNetworking.DesignSync(be.getLevel().dimension().identifier(),be.getBlockPos(),be.editIdentity(),-1,be.revision(),be.design(),List.of(),Map.of(),be.design().orientation(),null);}
    public static void resync(Player player,ModNetworking.DesignRequest request){
        if(!(player instanceof ServerPlayer p)||!request.dimension().equals(p.level().dimension().identifier())||!p.getChunkTrackingView().contains(ChunkPos.containing(request.pos()))||!p.level().hasChunkAt(request.pos()))return;
        long now=p.level().getGameTime();if(RESYNC.getOrDefault(p,Long.MIN_VALUE)==now)return;RESYNC.put(p,now);
        if(p.level().getBlockEntity(request.pos()) instanceof PaintableBlockEntity be&&be.valid())PacketDistributor.sendToPlayer(p,full(be));
    }
    private static void tick(ServerTickEvent.Post event){
        long end=System.nanoTime()+2_000_000;int budget=512;var iterator=PENDING.entrySet().iterator();while(iterator.hasNext()&&budget-->0&&System.nanoTime()<end){var e=iterator.next();iterator.remove();retainedWeight-=weight(e.getValue());var be=e.getKey();if(be.getLevel() instanceof ServerLevel level&&!be.isRemoved()&&be.valid()&&level.hasChunkAt(be.getBlockPos())&&level.getBlockEntity(be.getBlockPos())==be){var chunk=ChunkPos.containing(be.getBlockPos());if(!level.getChunkSource().chunkMap.getPlayers(chunk,false).isEmpty())PacketDistributor.sendToPlayersTrackingChunk(level,chunk,packet(be,e.getValue()));}}
    }
    private DesignUpdates(){}
}
