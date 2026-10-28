package net.nicomar.paintableblocks.blockentity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.nicomar.paintableblocks.production.*;
import net.nicomar.paintableblocks.registry.ModContent;

/** All drops use one portable item; removal never scatters these inventory slots. */
public abstract class PortableProductionEntity extends BlockEntity implements Container {
    protected final ItemStack[] slots={ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY};
    protected List<Integer> counts=List.of(0,0,0,0,0);
    protected int progress,workingPaint;
    private boolean valid=true;
    private Dynamic<?> rejected;
    protected PortableProductionEntity(BlockEntityType<?> type,BlockPos pos,BlockState state){super(type,pos,state);}
    public boolean valid(){return valid;}
    public abstract boolean accepts(ProductionState state);
    public List<Integer> counts(){return counts;}
    public int progress(){return progress;}
    public int workingPaint(){return workingPaint;}
    public ProductionState snapshot(){
        var items=new ArrayList<ProductionState.Stored>();for(int i=0;i<getContainerSize();i++)if(!slots[i].isEmpty())items.add(new ProductionState.Stored(i,new ItemStackTemplate(slots[i].typeHolder(),slots[i].getCount(),slots[i].getComponentsPatch())));
        return new ProductionState(counts,items,progress,workingPaint);
    }
    private void restore(ProductionState contents){
        valid=contents!=null&&contents.valid()&&accepts(contents);if(!valid)return;
        java.util.Arrays.fill(slots,ItemStack.EMPTY);counts=contents.blocks();progress=contents.progress();workingPaint=contents.paint();
        for(var entry:contents.items())slots[entry.slot()]=entry.stack().create();rejected=null;
    }
    @Override public boolean isEmpty(){return valid&&snapshot().empty();}
    @Override public ItemStack getItem(int index){return valid&&index>=0&&index<getContainerSize()?slots[index]:ItemStack.EMPTY;}
    @Override public ItemStack removeItem(int index,int amount){if(!valid||index<0||index>=getContainerSize()||amount<=0)return ItemStack.EMPTY;var out=slots[index].split(amount);setChanged();return out;}
    @Override public ItemStack removeItemNoUpdate(int index){if(!valid||index<0||index>=getContainerSize())return ItemStack.EMPTY;var out=slots[index];slots[index]=ItemStack.EMPTY;return out;}
    @Override public void setItem(int index,ItemStack stack){if(!valid||index<0||index>=getContainerSize())return;slots[index]=stack;setChanged();}
    @Override public void clearContent(){if(!valid)return;java.util.Arrays.fill(slots,ItemStack.EMPTY);counts=List.of(0,0,0,0,0);progress=workingPaint=0;setChanged();}
    @Override public boolean stillValid(Player p){return valid&&!isRemoved()&&level!=null&&level.hasChunkAt(worldPosition)&&level.getBlockEntity(worldPosition)==this&&net.nicomar.paintableblocks.interaction.ServerInteractions.allowed(p,worldPosition);}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);if(valid)out.store("production",ProductionState.CODEC,snapshot());else if(rejected!=null)out.store("production",Codec.PASSTHROUGH,rejected);}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);restore(in.read("production",ProductionState.CODEC).orElse(null));if(!valid)rejected=in.read("production",Codec.PASSTHROUGH).orElse(null);}
    @Override protected void applyImplicitComponents(DataComponentGetter components){super.applyImplicitComponents(components);restore(components.get(net.nicomar.paintableblocks.registry.ProductionContent.PRODUCTION_CONTENTS.get()));}
    @Override protected void collectImplicitComponents(DataComponentMap.Builder out){super.collectImplicitComponents(out);if(valid)out.set(net.nicomar.paintableblocks.registry.ProductionContent.PRODUCTION_CONTENTS.get(),snapshot());}
    @Override public void removeComponentsFromTag(ValueOutput out){super.removeComponentsFromTag(out);out.discard("production");}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup){return saveWithoutMetadata(lookup);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    public ItemStack toItem(){if(!valid)return ItemStack.EMPTY;var out=new ItemStack(getBlockState().getBlock().asItem());out.applyComponents(collectComponents());out.set(net.nicomar.paintableblocks.registry.ProductionContent.PRODUCTION_CONTENTS.get(),snapshot());return out;}
}
