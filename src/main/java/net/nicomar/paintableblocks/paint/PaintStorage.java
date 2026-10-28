package net.nicomar.paintableblocks.paint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.TreeMap;
import net.nicomar.paintableblocks.balance.PaintingBalance;

/** Immutable sorted palette; zero balances do not occupy a paint slot. */
public record PaintStorage(List<Entry> entries) {
    public record Entry(int paint,int units){
        static final Codec<Entry> CODEC=RecordCodecBuilder.create(i->i.group(
                Codec.intRange(1,PaintingBalance.PAINTS).fieldOf("paint").forGetter(Entry::paint),Codec.intRange(1,PaintingBalance.Bucket.NETHERITE.capacityPerPaint).fieldOf("units").forGetter(Entry::units)).apply(i,Entry::new));
    }
    public static final PaintStorage EMPTY=new PaintStorage(List.of());
    public static final Codec<PaintStorage> CODEC=Entry.CODEC.listOf(0,PaintingBalance.Bucket.NETHERITE.paints).flatXmap(list->{
        try{return DataResult.success(new PaintStorage(list));}catch(IllegalArgumentException e){return DataResult.error(e::getMessage);}
    },s->DataResult.success(s.entries));
    public PaintStorage {
        TreeMap<Integer,Entry> sorted=new TreeMap<>();
        for(Entry e:entries)if(!PaintCatalog.validId(e.paint)||e.units<=0||e.units>PaintingBalance.Bucket.NETHERITE.capacityPerPaint||sorted.put(e.paint,e)!=null)throw new IllegalArgumentException("Invalid paint palette");
        if(sorted.size()>PaintingBalance.Bucket.NETHERITE.paints)throw new IllegalArgumentException("Too many paints");
        entries=List.copyOf(sorted.values());
    }
    public int units(int paint){return entries.stream().filter(e->e.paint==paint).mapToInt(Entry::units).findFirst().orElse(0);}
    public boolean fits(PaintingBalance.Bucket tier){return entries.size()<=tier.paints&&entries.stream().allMatch(e->e.units<=tier.capacityPerPaint);}
    public int room(PaintingBalance.Bucket tier,int id){
        if(!fits(tier)||!PaintCatalog.validId(id)||(units(id)==0&&entries.size()>=tier.paints))return 0;
        return tier.capacityPerPaint-units(id);
    }
    public PaintStorage add(int id,int delta){
        if(!PaintCatalog.validId(id))throw new IllegalArgumentException("Invalid paint");
        int next=Math.addExact(units(id),delta);if(next<0||next>PaintingBalance.Bucket.NETHERITE.capacityPerPaint)throw new IllegalArgumentException("Invalid paint balance");
        var list=new java.util.ArrayList<>(entries.stream().filter(e->e.paint!=id).toList());if(next>0)list.add(new Entry(id,next));return new PaintStorage(list);
    }
}
