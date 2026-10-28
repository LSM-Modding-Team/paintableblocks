package net.nicomar.paintableblocks.menu;

import net.nicomar.paintableblocks.inventory.PaintTransfers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nicomar.paintableblocks.blockentity.PaintBucketBlockEntity;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.interaction.ServerInteractions;
import net.nicomar.paintableblocks.registry.ModContent;

/** No transient material slots: transfers commit directly to the palette and player inventory. */
public final class PaintBucketMenu extends AbstractContainerMenu {
    public static final int SELECT_PAINT=1024,WITHDRAW_ONE=2048,WITHDRAW_STACK=4096;
    private final BlockPos pos;
    private final PaintBucketBlockEntity bucket;
    private final ContainerData data;
    public PaintBucketMenu(int id,Inventory inventory,BlockPos pos){
        super(ModContent.BUCKET_MENU.get(),id);this.pos=pos.immutable();
        bucket=inventory.player.level().getBlockEntity(pos) instanceof PaintBucketBlockEntity b?b:null;
        if(inventory.player.level().isClientSide())data=new SimpleContainerData(25);
        else data=new ContainerData(){
            @Override public int getCount(){return 25;}
            @Override public int get(int index){
                if(bucket==null||!bucket.valid())return 0;if(index==24)return ServerInteractions.selected(inventory.player,bucket);
                int slot=index/3,part=index%3;var entries=bucket.storage().entries();if(slot>=entries.size())return 0;
                var entry=entries.get(slot);return part==0?entry.paint():part==1?entry.units()&65535:entry.units()>>>16;
            }
            @Override public void set(int index,int value){}
        };
        addDataSlots(data);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,9+row*9+col,70+col*18,136+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,70+col*18,194));
    }
    public int paint(int slot){return data.get(slot*3)&65535;}
    public int units(int slot){return (data.get(slot*3+1)&65535)|((data.get(slot*3+2)&65535)<<16);}
    public int selected(){return data.get(24)&65535;}
    @Override public boolean stillValid(Player p){return bucket!=null&&bucket.valid()&&!bucket.isRemoved()&&p.level().hasChunkAt(pos)&&p.level().getBlockEntity(pos)==bucket&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&(!p.level().isClientSide()?ServerInteractions.allowed(p,pos):true);}
    private static int depositCount(net.nicomar.paintableblocks.paint.PaintStorage storage,PaintingBalance.Bucket tier,ItemStack stack){
        if(!stack.is(ModContent.PAINT.get()))return 0;Integer id=stack.get(ModContent.PAINT_ID.get());if(id==null)return 0;
        var plain=new ItemStack(ModContent.PAINT.get());plain.set(ModContent.PAINT_ID.get(),id);
        if(!ItemStack.isSameItemSameComponents(stack,plain))return 0;
        return PaintTransfers.itemCountThatFits(storage,tier,id,stack.getCount());
    }
    public static int deposit(PaintBucketBlockEntity bucket,ItemStack stack){
        if(!bucket.valid())return 0;int count=depositCount(bucket.storage(),bucket.tier(),stack);if(count==0)return 0;
        int id=stack.get(ModContent.PAINT_ID.get());bucket.setStorage(bucket.storage().add(id,count*PaintingBalance.UNITS_PER_PAINT_ITEM));stack.shrink(count);return count;
    }
    private int predictDeposit(ItemStack stack){
        try{return predictDepositValid(stack);}catch(IllegalArgumentException partialSync){return 0;}
    }
    private int predictDepositValid(ItemStack stack){
        if(bucket==null)return 0;var entries=new java.util.ArrayList<net.nicomar.paintableblocks.paint.PaintStorage.Entry>();
        for(int slot=0;slot<8;slot++)if(paint(slot)>0&&units(slot)>0)entries.add(new net.nicomar.paintableblocks.paint.PaintStorage.Entry(paint(slot),units(slot)));
        var storage=new net.nicomar.paintableblocks.paint.PaintStorage(entries);int count=depositCount(storage,bucket.tier(),stack);if(count==0)return 0;
        storage=storage.add(stack.get(ModContent.PAINT_ID.get()),count*PaintingBalance.UNITS_PER_PAINT_ITEM);stack.shrink(count);
        for(int slot=0;slot<8;slot++){var entry=slot<storage.entries().size()?storage.entries().get(slot):null;data.set(slot*3,entry==null?0:entry.paint());data.set(slot*3+1,entry==null?0:entry.units()&65535);data.set(slot*3+2,entry==null?0:entry.units()>>>16);}
        return count;
    }
    @Override public boolean clickMenuButton(Player p,int button){
        if(p.level().isClientSide()||p.containerMenu!=this||!stillValid(p))return false;
        if(button>SELECT_PAINT&&button<=SELECT_PAINT+PaintingBalance.PAINTS){
            int id=button-SELECT_PAINT;if(bucket.storage().units(id)==0)return false;ServerInteractions.select(p,bucket,id);
        }else if(button==8){for(int slot=0;slot<36;slot++)deposit(bucket,p.getInventory().getItem(slot));}
        else if(button>WITHDRAW_ONE&&button<=WITHDRAW_ONE+PaintingBalance.PAINTS||button>WITHDRAW_STACK&&button<=WITHDRAW_STACK+PaintingBalance.PAINTS){
            boolean bulk=button>WITHDRAW_STACK;int id=button-(bulk?WITHDRAW_STACK:WITHDRAW_ONE);
            // The button identifies the paint displayed when clicked, never a mutable palette row.
            if(bucket.storage().units(id)==0||ServerInteractions.selected(p,bucket)!=id)return false;
            int count=Math.min(bulk?64:1,bucket.storage().units(id)/PaintingBalance.UNITS_PER_PAINT_ITEM);
            if(count>0){var stack=new ItemStack(ModContent.PAINT.get(),count);stack.set(ModContent.PAINT_ID.get(),id);PaintTransfers.giveItems(p.getInventory(),stack);
                int added=count-stack.getCount();if(added>0)bucket.setStorage(bucket.storage().add(id,-added*PaintingBalance.UNITS_PER_PAINT_ITEM));}
        }else return false;
        p.getInventory().setChanged();broadcastChanges();return true;
    }
    @Override public ItemStack quickMoveStack(Player p,int slot){
        if(!stillValid(p)||slot<0||slot>=slots.size())return ItemStack.EMPTY;
        var source=slots.get(slot);if(!source.hasItem())return ItemStack.EMPTY;var stack=source.getItem();var old=stack.copy();
        if(stack.is(ModContent.PAINT.get())){
            if(p.level().isClientSide())predictDeposit(stack);else deposit(bucket,stack);
            if(stack.isEmpty())source.set(ItemStack.EMPTY);else source.setChanged();
            return stack.getCount()!=old.getCount()?old:ItemStack.EMPTY;
        }
        if(!moveItemStackTo(stack,slot<27?27:0,slot<27?36:27,false))return ItemStack.EMPTY;
        if(stack.isEmpty())source.set(ItemStack.EMPTY);else source.setChanged();return old;
    }
}
