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
    public static int deposit(PaintBucketBlockEntity bucket,ItemStack stack){
        if(!bucket.valid()||!stack.is(ModContent.PAINT.get()))return 0;Integer id=stack.get(ModContent.PAINT_ID.get());if(id==null)return 0;
        int count=PaintTransfers.itemCountThatFits(bucket.storage(),bucket.tier(),id,stack.getCount());if(count==0)return 0;
        // Only plain paint stacks are converted; custom components must not silently disappear.
        ItemStack plain=new ItemStack(ModContent.PAINT.get());plain.set(ModContent.PAINT_ID.get(),id);
        if(!ItemStack.isSameItemSameComponents(stack,plain))return 0;
        bucket.setStorage(bucket.storage().add(id,count*PaintingBalance.UNITS_PER_PAINT_ITEM));stack.shrink(count);return count;
    }
    @Override public boolean clickMenuButton(Player p,int button){
        if(p.level().isClientSide()||p.containerMenu!=this||!stillValid(p))return false;
        if(button>=0&&button<8){int id=paint(button);if(id>0)ServerInteractions.select(p,bucket,id);}
        else if(button==8){for(int slot=0;slot<36;slot++)deposit(bucket,p.getInventory().getItem(slot));}
        else if(button==9||button==10){
            int id=ServerInteractions.selected(p,bucket);int count=Math.min(button==9?1:64,bucket.storage().units(id)/PaintingBalance.UNITS_PER_PAINT_ITEM);
            if(count>0){var stack=new ItemStack(ModContent.PAINT.get(),count);stack.set(ModContent.PAINT_ID.get(),id);PaintTransfers.giveItems(p.getInventory(),stack);
                int added=count-stack.getCount();if(added>0)bucket.setStorage(bucket.storage().add(id,-added*PaintingBalance.UNITS_PER_PAINT_ITEM));}
        }else return false;
        p.getInventory().setChanged();broadcastChanges();return true;
    }
    @Override public ItemStack quickMoveStack(Player p,int slot){
        if(!stillValid(p)||slot<0||slot>=slots.size())return ItemStack.EMPTY;
        var source=slots.get(slot);if(!source.hasItem())return ItemStack.EMPTY;var stack=source.getItem();var old=stack.copy();
        if(!p.level().isClientSide()&&stack.is(ModContent.PAINT.get())){deposit(bucket,stack);source.setChanged();return stack.getCount()!=old.getCount()?old:ItemStack.EMPTY;}
        if(!moveItemStackTo(stack,slot<27?27:0,slot<27?36:27,false))return ItemStack.EMPTY;
        if(stack.isEmpty())source.set(ItemStack.EMPTY);else source.setChanged();return old;
    }
}
