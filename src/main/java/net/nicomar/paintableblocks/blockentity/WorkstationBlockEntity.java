package net.nicomar.paintableblocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.block.WorkstationBlock;
import net.nicomar.paintableblocks.production.*;
import net.nicomar.paintableblocks.design.BlockDesign;
import net.nicomar.paintableblocks.item.PaintableBlockItem;
import net.nicomar.paintableblocks.registry.ModContent;

public final class WorkstationBlockEntity extends PortableProductionEntity {
    public WorkstationBlockEntity(BlockPos pos,BlockState state){super(net.nicomar.paintableblocks.registry.ProductionContent.WORKSTATION_ENTITY.get(),pos,state);}
    public PaintingBalance.Station tier(){return ((WorkstationBlock)getBlockState().getBlock()).tier();}
    @Override public int getContainerSize(){return 4;}
    @Override public boolean accepts(ProductionState state){return ((net.nicomar.paintableblocks.block.PortableProductionBlock)getBlockState().getBlock()).accepts(state);}
    public static boolean convertible(ItemStack stack){var d=stack.get(ModContent.DESIGN.get());return stack.getItem() instanceof PaintableBlockItem item&&d!=null&&d.complete()&&!d.painted()&&d.resolution()==((net.nicomar.paintableblocks.block.PaintableBlock)item.getBlock()).resolution();}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return valid()&&(slot==0?ProductionInputs.center(stack):slot==1?ProductionInputs.material(stack)!=null:slot==2&&convertible(stack));}
    public boolean manufacture(int n){
        if(!valid()||!ProductionInputs.center(slots[0])||slots[1].getCount()<8)return false;var material=ProductionInputs.material(slots[1]);if(material==null)return false;
        var next=WorkstationOperations.produce(counts,tier(),n,material);if(next.equals(counts))return false;
        slots[0].shrink(1);slots[1].shrink(8);counts=next;setChanged();return true;
    }
    public int deposit(ItemStack stack){int index=ProductionInputs.storable(stack);if(!valid()||index<0)return 0;int amount=Math.min(stack.getCount(),tier().capacity-counts.stream().mapToInt(Integer::intValue).sum());if(amount<=0)return 0;
        var next=new java.util.ArrayList<>(counts);next.set(index,next.get(index)+amount);counts=java.util.List.copyOf(next);stack.shrink(amount);setChanged();return amount;
    }
    public void convert(int from,int target,int amount){if(!valid())return;counts=WorkstationOperations.convert(counts,from,PaintingBalance.RESOLUTIONS.indexOf(target),amount);setChanged();}
    public void subtract(int index,int amount){if(!valid()||amount<0||amount>counts.get(index))throw new IllegalArgumentException("Invalid withdrawal");var next=new java.util.ArrayList<>(counts);next.set(index,next.get(index)-amount);counts=java.util.List.copyOf(next);setChanged();}
    public boolean convertItem(int n){
        if(!valid()||!convertible(slots[2]))return false;var source=slots[2];var design=source.get(ModContent.DESIGN.get());var out=ModContent.stack(design.converted(n));out.applyComponents(source.getComponentsPatch());out.set(ModContent.DESIGN.get(),design.converted(n));
        if(!slots[3].isEmpty()&&(!ItemStack.isSameItemSameComponents(slots[3],out)||slots[3].getCount()>=slots[3].getMaxStackSize()))return false;
        if(slots[3].isEmpty())slots[3]=out;else slots[3].grow(1);source.shrink(1);setChanged();return true;
    }
}
