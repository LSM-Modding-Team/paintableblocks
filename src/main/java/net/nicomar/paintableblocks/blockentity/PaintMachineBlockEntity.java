package net.nicomar.paintableblocks.blockentity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.block.PaintMachineBlock;
import net.nicomar.paintableblocks.production.*;
import net.nicomar.paintableblocks.registry.ModContent;

public final class PaintMachineBlockEntity extends PortableProductionEntity implements WorldlyContainer {
    public PaintMachineBlockEntity(BlockPos pos,BlockState state){super(net.nicomar.paintableblocks.registry.ProductionContent.MACHINE_ENTITY.get(),pos,state);}
    public PaintingBalance.Production tier(){return ((PaintMachineBlock)getBlockState().getBlock()).tier();}
    @Override public int getContainerSize(){return 5;}
    @Override public boolean accepts(ProductionState state){return ((net.nicomar.paintableblocks.block.PortableProductionBlock)getBlockState().getBlock()).accepts(state);}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return valid()&&slot==0&&ProductionInputs.paint(stack)>0;}
    @Override public int[] getSlotsForFace(Direction face){return face==Direction.DOWN?new int[]{1,2,3,4}:new int[]{0};}
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction face){return face!=Direction.DOWN&&canPlaceItem(slot,stack);}
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction face){return valid()&&face==Direction.DOWN&&slot>0;}
    public static void tick(Level level,BlockPos pos,BlockState state,PaintMachineBlockEntity be){if(!level.isClientSide()&&be.valid())be.process();}
    @Override public void setChanged(){
        if(slots!=null&&workingPaint!=0&&ProductionInputs.paint(slots[0])!=workingPaint)progress=workingPaint=0;
        super.setChanged();
    }
    private void process(){
        int id=ProductionInputs.paint(slots[0]);if(id==0){if(progress!=0||workingPaint!=0){progress=workingPaint=0;setChanged();}return;}
        if(workingPaint!=id){workingPaint=id;progress=0;setChanged();}
        var result=PaintMachineOperations.outputs(List.of(slots[1],slots[2],slots[3],slots[4]),slots[0],tier());if(result==null)return;
        if(++progress<PaintingBalance.MACHINE_TICKS){setChanged();return;}
        // The input and every output are committed together on the authoritative tick thread.
        slots[0].shrink(1);for(int i=0;i<4;i++)slots[i+1]=result.get(i);progress=workingPaint=0;setChanged();
    }
}
