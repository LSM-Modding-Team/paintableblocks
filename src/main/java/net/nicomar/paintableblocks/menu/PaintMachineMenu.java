package net.nicomar.paintableblocks.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.blockentity.PaintMachineBlockEntity;
import net.nicomar.paintableblocks.production.ProductionInputs;
import net.nicomar.paintableblocks.registry.ModContent;

public final class PaintMachineMenu extends ProductionMenu {
    public PaintMachineMenu(int id,Inventory inventory,BlockPos pos){super(net.nicomar.paintableblocks.registry.ProductionContent.MACHINE_MENU.get(),id,inventory,pos,5,false);}
    public int yield(){return entity instanceof PaintMachineBlockEntity be?be.tier().itemsPerBlock:0;}
    @Override protected boolean moveInput(ItemStack stack,Player player){return ProductionInputs.paint(stack)>0&&moveItemStackTo(stack,0,1,false);}
}
