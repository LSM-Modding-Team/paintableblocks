package net.nicomar.paintableblocks.production;

import net.nicomar.paintableblocks.balance.PaintingBalance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.HashSet;
import net.minecraft.world.item.ItemStackTemplate;

/** Immutable portable contents. ItemStackTemplate prevents live menu mutations of saved components. */
public record ProductionState(List<Integer> blocks,List<Stored> items,int progress,int paint) {
    public record Stored(int slot,ItemStackTemplate stack){
        public static final Codec<Stored> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(0,4).fieldOf("slot").forGetter(Stored::slot),ItemStackTemplate.CODEC.fieldOf("stack").forGetter(Stored::stack)).apply(i,Stored::new));
    }
    private static final Codec<ProductionState> RAW=RecordCodecBuilder.create(i->i.group(
        Codec.intRange(0,PaintingBalance.Station.ASSEMBLER.capacity).listOf(5,5).fieldOf("blocks").forGetter(ProductionState::blocks),Stored.CODEC.listOf(0,5).fieldOf("items").forGetter(ProductionState::items),
        Codec.intRange(0,PaintingBalance.MACHINE_TICKS).fieldOf("progress").forGetter(ProductionState::progress),Codec.intRange(0,PaintingBalance.PAINTS).fieldOf("paint").forGetter(ProductionState::paint)).apply(i,ProductionState::new));
    public static final Codec<ProductionState> CODEC=RAW.validate(s->s.valid()?DataResult.success(s):DataResult.error(()->"Invalid production contents"));
    public static final ProductionState EMPTY=new ProductionState(List.of(0,0,0,0,0),List.of(),0,0);
    public ProductionState{blocks=List.copyOf(blocks);items=List.copyOf(items);}
    public boolean valid(){
        if(blocks.size()!=5||blocks.stream().anyMatch(n->n<0||n>PaintingBalance.Station.ASSEMBLER.capacity)||progress<0||progress>PaintingBalance.MACHINE_TICKS||paint<0||paint>PaintingBalance.PAINTS||progress>0&&paint==0)return false;
        HashSet<Integer> seen=new HashSet<>();
        for(var entry:items){var stack=entry.stack.create();if(entry.slot<0||entry.slot>4||!seen.add(entry.slot)||stack.isEmpty()||stack.getCount()>stack.getMaxStackSize())return false;}
        return true;
    }
    public int total(){return blocks.stream().mapToInt(Integer::intValue).sum();}
    public boolean empty(){return total()==0&&items.isEmpty()&&progress==0&&paint==0;}
}
