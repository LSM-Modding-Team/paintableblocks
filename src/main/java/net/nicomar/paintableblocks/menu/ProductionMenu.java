package net.nicomar.paintableblocks.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.nicomar.paintableblocks.blockentity.PortableProductionEntity;
import net.nicomar.paintableblocks.interaction.ServerInteractions;

public abstract class ProductionMenu extends AbstractContainerMenu {
    protected final PortableProductionEntity entity;
    protected final Container inventory;
    protected final ContainerData data;
    protected final int ownSlots;
    protected int selectedResolution=1;
    protected ProductionMenu(MenuType<?> type,int id,Inventory playerInventory,BlockPos pos,int ownSlots,boolean workstation){
        super(type,id);this.ownSlots=ownSlots;var level=playerInventory.player.level();
        entity=level.getBlockEntity(pos) instanceof PortableProductionEntity be?be:null;
        inventory=level.isClientSide()?new SimpleContainer(ownSlots):entity==null?new SimpleContainer(ownSlots):entity;
        data=level.isClientSide()?new SimpleContainerData(8):new ContainerData(){
            @Override public int getCount(){return 8;}
            @Override public int get(int index){if(entity==null||!entity.valid())return 0;return index<5?entity.counts().get(index):index==5?entity.progress():index==6?entity.workingPaint():selectedResolution;}
            @Override public void set(int index,int value){}
        };addDataSlots(data);
        for(int index=0;index<ownSlots;index++){final int slot=index;int x=workstation?8+index*18:index==0?8:74+index*18;
            addSlot(new Slot(inventory,index,x,18){@Override public boolean mayPlace(ItemStack stack){return entity!=null&&entity.canPlaceItem(slot,stack);}});}
        int y=workstation?150:136;
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(playerInventory,9+row*9+col,70+col*18,y+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(playerInventory,col,70+col*18,y+58));
    }
    public int count(int index){return data.get(index);}
    public int progress(){return data.get(5);}
    public int paint(){return data.get(6);}
    public int resolution(){return data.get(7)==0?1:data.get(7);}
    @Override public boolean stillValid(Player p){return entity!=null&&entity.valid()&&!entity.isRemoved()&&p.level().hasChunkAt(entity.getBlockPos())&&p.level().getBlockEntity(entity.getBlockPos())==entity&&(p.level().isClientSide()||ServerInteractions.allowed(p,entity.getBlockPos()));}
    protected abstract boolean moveInput(ItemStack stack,Player player);
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(!stillValid(player)||index<0||index>=slots.size())return ItemStack.EMPTY;var source=slots.get(index);if(!source.hasItem())return ItemStack.EMPTY;var stack=source.getItem();var original=stack.copy();
        if(index<ownSlots){if(!moveItemStackTo(stack,ownSlots,ownSlots+36,true))return ItemStack.EMPTY;}
        else if(!moveInput(stack,player)){
            if(index<ownSlots+27){if(!moveItemStackTo(stack,ownSlots+27,ownSlots+36,false))return ItemStack.EMPTY;}
            else if(!moveItemStackTo(stack,ownSlots,ownSlots+27,false))return ItemStack.EMPTY;
        }
        if(stack.isEmpty())source.set(ItemStack.EMPTY);else source.setChanged();return stack.getCount()!=original.getCount()?original:ItemStack.EMPTY;
    }
}
