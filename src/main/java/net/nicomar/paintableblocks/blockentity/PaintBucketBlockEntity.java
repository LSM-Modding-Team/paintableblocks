package net.nicomar.paintableblocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.block.PaintBucketBlock;
import net.nicomar.paintableblocks.paint.PaintStorage;
import net.nicomar.paintableblocks.registry.ModContent;

public final class PaintBucketBlockEntity extends BlockEntity {
    private PaintStorage storage=PaintStorage.EMPTY;
    private com.mojang.serialization.Dynamic<?> rejected;
    private boolean valid=true;
    public PaintBucketBlockEntity(BlockPos pos,BlockState state){super(ModContent.BUCKET_ENTITY.get(),pos,state);}
    public PaintingBalance.Bucket tier(){return ((PaintBucketBlock)getBlockState().getBlock()).tier();}
    public boolean valid(){return valid;}
    public PaintStorage storage(){return storage;}
    public void setStorage(PaintStorage next){
        if(level!=null&&level.isClientSide())throw new IllegalStateException("Server only");
        if(!valid||!next.fits(tier()))throw new IllegalArgumentException("Invalid bucket storage");
        storage=next;setChanged();
        if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_ALL);
    }
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);if(valid)out.store("paints",PaintStorage.CODEC,storage);else if(rejected!=null)out.store("paints",com.mojang.serialization.Codec.PASSTHROUGH,rejected);}
    @Override protected void loadAdditional(ValueInput in){
        super.loadAdditional(in);var parsed=in.read("paints",PaintStorage.CODEC);valid=parsed.isPresent()&&parsed.get().fits(tier());
        storage=valid?parsed.get():PaintStorage.EMPTY;rejected=valid?null:in.read("paints",com.mojang.serialization.Codec.PASSTHROUGH).orElse(null);
    }
    @Override protected void applyImplicitComponents(DataComponentGetter components){super.applyImplicitComponents(components);var s=components.get(ModContent.BUCKET_PAINTS.get());valid=s!=null&&s.fits(tier());storage=valid?s:PaintStorage.EMPTY;}
    @Override protected void collectImplicitComponents(DataComponentMap.Builder b){super.collectImplicitComponents(b);if(valid)b.set(ModContent.BUCKET_PAINTS.get(),storage);}
    @Override public void removeComponentsFromTag(ValueOutput out){super.removeComponentsFromTag(out);out.discard("paints");}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    public ItemStack toItem(){if(!valid)return ItemStack.EMPTY;var out=new ItemStack(getBlockState().getBlock().asItem());out.applyComponents(collectComponents());out.set(ModContent.BUCKET_PAINTS.get(),storage);return out;}
}
