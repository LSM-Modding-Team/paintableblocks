package net.nicomar.paintableblocks.performance;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Section-coalesced client dirtiness, without a client dependency on the common side. */
public final class VisualInvalidation {
    private static final Map<Level,LinkedHashSet<BlockPos>> QUEUES=new WeakHashMap<>();
    public static void add(Level level,BlockPos pos){if(level==null||!level.isClientSide())return;synchronized(QUEUES){QUEUES.computeIfAbsent(level,k->new LinkedHashSet<>()).add(new BlockPos(pos.getX()>>4,pos.getY()>>4,pos.getZ()>>4));}}
    public static void flush(Level level){
        List<BlockPos> batch=new ArrayList<>();synchronized(QUEUES){QUEUES.keySet().removeIf(l->l!=level);var queue=QUEUES.get(level);if(queue==null)return;var iterator=queue.iterator();while(iterator.hasNext()&&batch.size()<64){batch.add(iterator.next());iterator.remove();}}
        for(var section:batch){var pos=new BlockPos(section.getX()*16+8,section.getY()*16+8,section.getZ()*16+8);if(level.hasChunkAt(pos)){var state=level.getBlockState(pos);level.setBlocksDirty(pos,state,state);}}
    }
    private VisualInvalidation(){}
}
