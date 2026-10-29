package net.nicomar.paintableblocks.client.selection;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.block.PaintableBlock;
import net.nicomar.paintableblocks.network.ModNetworking;

/** Compact per-section recovery bits; never discards a loaded design that still needs recovery. */
public final class ClientDesignUpdates {
    private static final class Bucket {final BitSet bits=new BitSet(4096);int next;long sent=-100,added;}
    private static final Map<BlockPos,Bucket> MISSING=new LinkedHashMap<>();
    private static net.minecraft.resources.Identifier dimension;private static int roundRobin;
    public static void register(RegisterClientPayloadHandlersEvent event){event.register(ModNetworking.DesignSync.TYPE,(v,ctx)->{
        var c=Minecraft.getInstance();if(c.level==null||!v.dimension().equals(c.level.dimension().identifier())||!c.level.hasChunkAt(v.pos()))return;reset(c.level.dimension().identifier());
        if(c.level.getBlockEntity(v.pos()) instanceof PaintableBlockEntity be&&be.applySync(v)){clear(v.pos());return;}
        var section=new BlockPos(v.pos().getX()>>4,v.pos().getY()>>4,v.pos().getZ()>>4);var bucket=MISSING.computeIfAbsent(section,k->new Bucket());bucket.bits.set(index(v.pos()));bucket.added=c.level.getGameTime();
    });}
    private static int index(BlockPos p){return (p.getX()&15)+16*((p.getY()&15)+16*(p.getZ()&15));}
    private static void clear(BlockPos p){var section=new BlockPos(p.getX()>>4,p.getY()>>4,p.getZ()>>4);var b=MISSING.get(section);if(b!=null){b.bits.clear(index(p));if(b.bits.isEmpty())MISSING.remove(section);}}
    private static void reset(net.minecraft.resources.Identifier next){if(!Objects.equals(dimension,next)){MISSING.clear();dimension=next;roundRobin=0;}}
    public static void install(){NeoForge.EVENT_BUS.addListener(ClientDesignUpdates::tick);}
    private static void tick(ClientTickEvent.Post event){
        var c=Minecraft.getInstance();if(c.level==null||c.player==null){reset(null);net.nicomar.paintableblocks.performance.VisualInvalidation.flush(null);return;}
        reset(c.level.dimension().identifier());net.nicomar.paintableblocks.performance.VisualInvalidation.flush(c.level);if(MISSING.isEmpty())return;long now=c.level.getGameTime();
        var entries=List.copyOf(MISSING.entrySet());
        for(int attempt=0;attempt<Math.min(32,entries.size());attempt++){
            var e=entries.get(Math.floorMod(roundRobin++,entries.size()));var section=e.getKey();var b=e.getValue();var origin=new BlockPos(section.getX()*16,section.getY()*16,section.getZ()*16);
            if(!c.level.hasChunkAt(origin)){MISSING.remove(section);continue;}if(now-b.sent<Math.max(1,20/Math.max(1,b.bits.cardinality())))continue;
            int bit=b.bits.nextSetBit(b.next);if(bit<0)bit=b.bits.nextSetBit(0);if(bit<0){MISSING.remove(section);continue;}b.next=(bit+1)%4096;
            var pos=origin.offset(bit%16,bit/16%16,bit/256);
            if(!(c.level.getBlockState(pos).getBlock() instanceof PaintableBlock)){if(now-b.added>40){b.bits.clear(bit);if(b.bits.isEmpty())MISSING.remove(section);}continue;}
            ClientPacketDistributor.sendToServer(new ModNetworking.DesignRequest(dimension,pos));b.sent=now;return;
        }
    }
    private ClientDesignUpdates(){}
}
