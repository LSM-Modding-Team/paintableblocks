package net.nicomar.paintableblocks.inventory;

import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.paint.PaintContent;
import net.nicomar.paintableblocks.paint.PaintStorage;

public final class PaintTransfers {
    public record Exchange(PaintContent brush,PaintStorage storage,int moved){}
    private PaintTransfers(){}
    public static Exchange load(PaintContent brush,int capacity,PaintStorage storage,int id,boolean creative){
        if(!brush.fits(capacity)||id<=0||storage.units(id)==0||(brush.units()>0&&brush.paint()!=id))return new Exchange(brush,storage,0);
        if(creative)return new Exchange(new PaintContent(id,brush.units()),storage,0);
        int amount=Math.min(capacity-brush.units(),storage.units(id));
        return new Exchange(new PaintContent(id,brush.units()+amount),storage.add(id,-amount),amount);
    }
    public static Exchange giveBack(PaintContent brush,PaintStorage storage,PaintingBalance.Bucket tier){
        if(!brush.valid()||brush.units()==0)return new Exchange(brush,storage,0);
        int amount=Math.min(brush.units(),storage.room(tier,brush.paint()));
        return new Exchange(brush.withUnits(brush.units()-amount),storage.add(brush.paint(),amount),amount);
    }
    public static int itemCountThatFits(PaintStorage storage,PaintingBalance.Bucket tier,int id,int count){
        return Math.max(0,Math.min(count,storage.room(tier,id)/PaintingBalance.UNITS_PER_PAINT_ITEM));
    }
    /** Bounded main-inventory insertion, without Inventory.add's creative overflow deletion. */
    public static int giveItems(net.minecraft.world.entity.player.Inventory inventory,net.minecraft.world.item.ItemStack incoming){
        int original=incoming.getCount();
        for(int pass=0;pass<2;pass++)for(int slot=0;slot<36&&!incoming.isEmpty();slot++){
            var current=inventory.getItem(slot);
            if(pass==0&&!current.isEmpty()&&net.minecraft.world.item.ItemStack.isSameItemSameComponents(current,incoming)){
                int amount=Math.min(incoming.getCount(),Math.max(0,current.getMaxStackSize()-current.getCount()));current.grow(amount);incoming.shrink(amount);
            }else if(pass==1&&current.isEmpty()){
                int amount=Math.min(incoming.getCount(),incoming.getMaxStackSize());inventory.setItem(slot,incoming.copyWithCount(amount));incoming.shrink(amount);
            }
        }
        if(original!=incoming.getCount())inventory.setChanged();return original-incoming.getCount();
    }
}
