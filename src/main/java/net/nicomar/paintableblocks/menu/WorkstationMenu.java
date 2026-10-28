package net.nicomar.paintableblocks.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.blockentity.WorkstationBlockEntity;
import net.nicomar.paintableblocks.design.BlockDesign;
import net.nicomar.paintableblocks.inventory.PaintTransfers;
import net.nicomar.paintableblocks.production.ProductionInputs;
import net.nicomar.paintableblocks.registry.ModContent;

public final class WorkstationMenu extends ProductionMenu {
    public WorkstationMenu(int id,Inventory inventory,BlockPos pos){super(net.nicomar.paintableblocks.registry.ProductionContent.WORKSTATION_MENU.get(),id,inventory,pos,4,true);}
    public PaintingBalance.Station tier(){return entity instanceof WorkstationBlockEntity be?be.tier():PaintingBalance.Station.WORKBENCH;}
    private int deposit(ItemStack stack,Player player){
        if(!(entity instanceof WorkstationBlockEntity be))return 0;
        if(!player.level().isClientSide())return be.deposit(stack);
        int index=ProductionInputs.storable(stack);if(index<0)return 0;int total=0;for(int i=0;i<5;i++)total+=count(i);int amount=Math.min(stack.getCount(),tier().capacity-total);
        if(amount>0){data.set(index,count(index)+amount);stack.shrink(amount);}return amount;
    }
    @Override protected boolean moveInput(ItemStack stack,Player player){
        if(ProductionInputs.storable(stack)>=0){deposit(stack,player);return true;}
        if(ProductionInputs.center(stack))return moveItemStackTo(stack,0,1,false);
        if(ProductionInputs.material(stack)!=null)return moveItemStackTo(stack,1,2,false);
        if(WorkstationBlockEntity.convertible(stack))return moveItemStackTo(stack,2,3,false);return false;
    }
    public boolean action(Player player,int action,int variant,int expectedResolution,int expectedMaterial){
        if(player.level().isClientSide()||player.containerMenu!=this||!stillValid(player)||!(entity instanceof WorkstationBlockEntity be))return false;
        if(action==0){if(!PaintingBalance.RESOLUTIONS.contains(variant))return false;selectedResolution=variant;broadcastChanges();return true;}
        if(expectedResolution!=selectedResolution)return false;
        boolean done=true;
        if(action==1){var material=ProductionInputs.material(be.getItem(1));if(material==null||material.ordinal()!=expectedMaterial){broadcastChanges();return false;}done=be.manufacture(selectedResolution);}
        else if(action==2)done=be.convertItem(selectedResolution);
        else if(action==3){for(int i=0;i<36;i++)be.deposit(player.getInventory().getItem(i));}
        else if(variant>=0&&variant<5&&(action==4||action==5)){
            int amount=Math.min(action==4?1:64,be.counts().get(variant));if(amount>0){var out=ModContent.stack(BlockDesign.full(PaintingBalance.RESOLUTIONS.get(variant)));out.setCount(amount);int added=PaintTransfers.giveItems(player.getInventory(),out);be.subtract(variant,added);done=added>0;}else done=false;
        }else if(variant>=0&&variant<5&&(action==6||action==7))be.convert(variant,selectedResolution,action==6?1:64);
        else return false;
        if(!done&&player instanceof net.minecraft.server.level.ServerPlayer server)server.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.paintableblocks.production_blocked"));
        player.getInventory().setChanged();broadcastChanges();return true;
    }
}
