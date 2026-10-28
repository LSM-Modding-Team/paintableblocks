package net.nicomar.paintableblocks.blockentity;

import net.minecraft.core.BlockPos;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NbtOps;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import net.nicomar.paintableblocks.block.PaintableBlock;
import net.nicomar.paintableblocks.design.BlockDesign;
import net.nicomar.paintableblocks.design.DesignGeometry;
import net.nicomar.paintableblocks.registry.ModContent;

public final class PaintableBlockEntity extends BlockEntity {
    public static final ModelProperty<BlockDesign> DESIGN_PROPERTY=new ModelProperty<>();
    private BlockDesign design;
    private java.util.UUID editIdentity=java.util.UUID.randomUUID();
    private long revision;
    public java.util.UUID editIdentity(){return editIdentity;}
    public long revision(){return revision;}
    private Dynamic<?> rejected;
    private boolean valid=true;
    private VoxelShape shape;
    public PaintableBlockEntity(BlockPos pos,BlockState state){
        super(ModContent.PAINTABLE_ENTITY.get(),pos,state);
        int n=((PaintableBlock)state.getBlock()).resolution();
        var placing=net.nicomar.paintableblocks.design.PlacementDesign.current(pos,n);
        design=placing==null?BlockDesign.full(n):placing;
    }
    public boolean valid(){return valid;}
    public BlockDesign design(){return design;}
    public VoxelShape shape(){
        if(!valid)return Shapes.block();
        if(shape==null)shape=DesignGeometry.shape(design);return shape;
    }
    public void setDesign(BlockDesign next){
        if(level!=null && level.isClientSide())throw new IllegalStateException("Only the server changes block designs");
        BlockDesign before=design;boolean wasValid=valid;
        accept(next);revision++;boolean geometryChanged=!wasValid||!before.sameGeometry(design);
        if(level!=null && design.empty()){level.removeBlock(worldPosition,false);return;}
        boolean cut=!design.complete();
        if(level!=null && getBlockState().getValue(PaintableBlock.CUT)!=cut) {
            level.setBlock(worldPosition,getBlockState().setValue(PaintableBlock.CUT,cut),Block.UPDATE_ALL);
        }
        if(level!=null&&geometryChanged){
            // Same BlockState can have a different support/collision shape after any edit.
            var state=getBlockState();state.updateNeighbourShapes(level,worldPosition,Block.UPDATE_ALL);
            state.updateIndirectNeighbourShapes(level,worldPosition,Block.UPDATE_ALL);
        }
        setChanged();
        if(level!=null){level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_ALL);level.updateNeighborsAt(worldPosition,getBlockState().getBlock(),null);}
    }
    private void accept(BlockDesign next){
        var placing=net.nicomar.paintableblocks.design.PlacementDesign.current(worldPosition,((PaintableBlock)getBlockState().getBlock()).resolution());
        if(placing!=null)next=placing;
        if(next.resolution()!=((PaintableBlock)getBlockState().getBlock()).resolution())throw new IllegalArgumentException("Design resolution does not match block");
        VoxelShape previous=valid && design!=null && design.sameGeometry(next)?shape:null;
        design=next;valid=true;rejected=null;shape=previous;refreshClientMesh();
    }
    private void refreshClientMesh(){
        requestModelDataUpdate();
        if(level!=null&&level.isClientSide()){
            level.setBlocksDirty(worldPosition,getBlockState(),getBlockState());
            level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_ALL);
        }
    }
    private void refresh(){shape=null;refreshClientMesh();}
    @Override protected void saveAdditional(ValueOutput output){
        super.saveAdditional(output);
        output.store("edit_identity",net.minecraft.core.UUIDUtil.CODEC,editIdentity);
        output.store("edit_revision",Codec.LONG,revision);
        if(valid)output.store("design",BlockDesign.CODEC,design);
        else output.store("design",Codec.PASSTHROUGH,rejected==null?new Dynamic<>(NbtOps.INSTANCE,new CompoundTag()):rejected);
    }
    @Override protected void loadAdditional(ValueInput input){
        super.loadAdditional(input);
        if(net.nicomar.paintableblocks.design.PlacementDesign.current(worldPosition,design.resolution())==null){
            editIdentity=input.read("edit_identity",net.minecraft.core.UUIDUtil.CODEC).orElse(editIdentity);
            revision=input.read("edit_revision",Codec.LONG).orElse(0L);
        }
        var parsed=input.read("design",BlockDesign.CODEC);
        if(parsed.isPresent()&&parsed.get().resolution()==((PaintableBlock)getBlockState().getBlock()).resolution()&&!parsed.get().empty())accept(parsed.get());
        else {valid=false;rejected=input.read("design",Codec.PASSTHROUGH).orElseGet(()->new Dynamic<>(NbtOps.INSTANCE,new CompoundTag()));refresh();}
    }
    @Override protected void applyImplicitComponents(DataComponentGetter components){
        super.applyImplicitComponents(components);
        BlockDesign stored=net.nicomar.paintableblocks.design.PlacementDesign.current(worldPosition,design.resolution());
        if(stored==null)stored=components.get(ModContent.DESIGN.get());
        if(stored!=null && stored.resolution()==design.resolution()&&!stored.empty())accept(stored);
        else {valid=false;refresh();}
    }
    @Override protected void collectImplicitComponents(DataComponentMap.Builder builder){
        super.collectImplicitComponents(builder);if(valid)builder.set(ModContent.DESIGN.get(),design);
    }
    @Override public void removeComponentsFromTag(ValueOutput output){super.removeComponentsFromTag(output);output.discard("design");output.discard("edit_identity");output.discard("edit_revision");}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public ModelData getModelData(){return valid?ModelData.builder().with(DESIGN_PROPERTY,design).build():ModelData.EMPTY;}
    public ItemStack toItem(boolean keepPaint){
        if(!valid)return ItemStack.EMPTY;
        ItemStack out=ModContent.stack(keepPaint?design:design.withoutPaint());
        out.applyComponents(collectComponents());out.set(ModContent.DESIGN.get(),keepPaint?design:design.withoutPaint());return out;
    }
}
