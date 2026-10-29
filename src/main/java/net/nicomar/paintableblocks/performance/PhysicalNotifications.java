package net.nicomar.paintableblocks.performance;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Deduplicates general neighbor notifications. Collision and support changes are applied immediately by the entity. */
public final class PhysicalNotifications {
    private static final Map<ServerLevel,LinkedHashSet<BlockPos>> QUEUES=new HashMap<>();
    private static int next;
    public static void add(ServerLevel level,BlockPos pos){QUEUES.computeIfAbsent(level,l->new LinkedHashSet<>()).add(pos.immutable());}
    public static void register(){NeoForge.EVENT_BUS.addListener(PhysicalNotifications::tick);NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e)->QUEUES.clear());}
    private static void tick(ServerTickEvent.Post event){
        long end=System.nanoTime()+2_000_000;int budget=2048;
        var levels=List.copyOf(QUEUES.keySet());int idle=0;
        while(!levels.isEmpty()&&budget>0&&System.nanoTime()<end){
            var level=levels.get(Math.floorMod(next++,levels.size()));var queue=QUEUES.get(level);if(queue==null||queue.isEmpty()){QUEUES.remove(level);if(++idle>=levels.size())break;continue;}idle=0;
            var iterator=queue.iterator();var pos=iterator.next();iterator.remove();if(queue.isEmpty())QUEUES.remove(level);budget--;
            if(level.hasChunkAt(pos)&&level.getBlockState(pos).getBlock() instanceof net.nicomar.paintableblocks.block.PaintableBlock)level.updateNeighborsAt(pos,level.getBlockState(pos).getBlock(),null);
        }
    }
    private PhysicalNotifications(){}
}
