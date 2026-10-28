package net.nicomar.paintableblocks.production;

import java.util.List;
import java.util.ArrayList;
import net.nicomar.paintableblocks.balance.PaintingBalance;

/** Pure count plans: production checks whole-batch capacity and conversion preserves totals. */
public final class WorkstationOperations {
    private WorkstationOperations(){}
    public static List<Integer> produce(List<Integer> counts,PaintingBalance.Station tier,int resolution,PaintingBalance.Material material){
        int slot=PaintingBalance.RESOLUTIONS.indexOf(resolution);if(slot<0||material==null)throw new IllegalArgumentException("Invalid production");
        int yield=tier.yield(material);if(counts.stream().mapToInt(Integer::intValue).sum()+yield>tier.capacity)return counts;
        var next=new ArrayList<>(counts);next.set(slot,Math.addExact(next.get(slot),yield));return List.copyOf(next);
    }
    public static List<Integer> convert(List<Integer> counts,int from,int to,int amount){
        if(from<0||from>=5||to<0||to>=5||from==to||amount<=0)return counts;int move=Math.min(amount,counts.get(from));
        var next=new ArrayList<>(counts);next.set(from,next.get(from)-move);next.set(to,Math.addExact(next.get(to),move));return List.copyOf(next);
    }
}
