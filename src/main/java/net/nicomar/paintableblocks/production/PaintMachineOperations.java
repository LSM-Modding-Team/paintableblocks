package net.nicomar.paintableblocks.production;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.registry.ModContent;

/** All outputs, including the empty powder-snow bucket, must fit before consuming the source. */
public final class PaintMachineOperations {
    private PaintMachineOperations(){}
    public static List<ItemStack> outputs(List<ItemStack> current,ItemStack source,PaintingBalance.Production tier){
        int id=ProductionInputs.paint(source);if(id==0)return null;
        var next=new ArrayList<ItemStack>();for(var stack:current)next.add(stack.copy());
        var paint=new ItemStack(ModContent.PAINT.get(),tier.itemsPerBlock);paint.set(ModContent.PAINT_ID.get(),id);
        if(!insert(next,paint))return null;
        var remainder=source.is(Items.POWDER_SNOW_BUCKET)?new ItemStack(Items.BUCKET):source.getItem().getCraftingRemainder()==null?ItemStack.EMPTY:source.getItem().getCraftingRemainder().create();
        return remainder.isEmpty()||insert(next,remainder)?List.copyOf(next):null;
    }
    private static boolean insert(List<ItemStack> slots,ItemStack incoming){
        for(int pass=0;pass<2;pass++)for(int i=0;i<slots.size()&&!incoming.isEmpty();i++){
            var slot=slots.get(i);if(pass==0&&!slot.isEmpty()&&ItemStack.isSameItemSameComponents(slot,incoming)){int n=Math.min(incoming.getCount(),slot.getMaxStackSize()-slot.getCount());if(n>0){slot.grow(n);incoming.shrink(n);}}
            else if(pass==1&&slot.isEmpty()){int n=Math.min(incoming.getCount(),incoming.getMaxStackSize());slots.set(i,incoming.copyWithCount(n));incoming.shrink(n);}
        }return incoming.isEmpty();
    }
}
