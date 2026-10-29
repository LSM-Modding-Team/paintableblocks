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
    private boolean valid=true,loadedFromStorage;
    private VoxelShape shape,outline,support;
    public VoxelShape supportShape(){if(!valid)return Shapes.block();if(support==null)support=DesignGeometry.supportShape(design);return support;}
    public VoxelShape outlineShape(){if(!valid)return Shapes.block();if(outline==null)outline=design.slope()==null?shape():new net.nicomar.paintableblocks.design.DesignOutlineShape(design);return outline;}
    public PaintableBlockEntity(BlockPos pos,BlockState state){
        super(ModContent.PAINTABLE_ENTITY.get(),pos,state);
        int n=((PaintableBlock)state.getBlock()).resolution();
        var placing=net.nicomar.paintableblocks.design.PlacementDesign.current(pos,n);
        design=placing==null?BlockDesign.full(n):placing;
    }
    @Override public void setLevel(net.minecraft.world.level.Level next){boolean restored=loadedFromStorage;loadedFromStorage=false;super.setLevel(next);if(!restored)net.nicomar.paintableblocks.interaction.VolumeEditing.changed(next,worldPosition);}
    @Override public void setRemoved(){net.nicomar.paintableblocks.interaction.VolumeEditing.changed(level,worldPosition);super.setRemoved();}
    public boolean valid(){return valid;}
    public BlockDesign design(){return design;}
    public VoxelShape shape(){
        if(!valid)return Shapes.block();
        if(shape==null)shape=DesignGeometry.shape(design);return shape;
    }
    public void setDesign(BlockDesign next){
        if(level!=null && level.isClientSide())throw new IllegalStateException("Only the server changes block designs");
        if(valid&&design.equals(next))return;
        BlockDesign before=design;boolean wasValid=valid;long previousRevision=revision;
        accept(next);revision++;boolean geometryChanged=!wasValid||!before.sameGeometry(design);
        if(level!=null && design.empty()){level.removeBlock(worldPosition,false);return;}
        boolean cut=!design.complete();boolean stateChanged=level!=null&&getBlockState().getValue(PaintableBlock.CUT)!=cut;
        if(level!=null && getBlockState().getValue(PaintableBlock.CUT)!=cut) {
            level.setBlock(worldPosition,getBlockState().setValue(PaintableBlock.CUT,cut),Block.UPDATE_ALL);
        }
        if(level!=null&&geometryChanged&&!stateChanged){
            // Same BlockState can have a different support/collision shape after any edit.
            var state=getBlockState();state.updateNeighbourShapes(level,worldPosition,Block.UPDATE_ALL);
            state.updateIndirectNeighbourShapes(level,worldPosition,Block.UPDATE_ALL);
        }
        setChanged();
        if(level!=null){if(stateChanged)net.nicomar.paintableblocks.network.DesignUpdates.nativeUpdate(this);else net.nicomar.paintableblocks.network.DesignUpdates.changed(this,wasValid?before:null,previousRevision);if(geometryChanged&&!stateChanged&&level instanceof net.minecraft.server.level.ServerLevel server)net.nicomar.paintableblocks.performance.PhysicalNotifications.add(server,worldPosition);}
    }
    private void accept(BlockDesign next){
        var placing=net.nicomar.paintableblocks.design.PlacementDesign.current(worldPosition,((PaintableBlock)getBlockState().getBlock()).resolution());
        if(placing!=null)next=placing;
        if(next.resolution()!=((PaintableBlock)getBlockState().getBlock()).resolution())throw new IllegalArgumentException("Design resolution does not match block");
        var before=valid?design:null;boolean same=before!=null&&before.sameGeometry(next);
        if(level!=null&&!level.isClientSide()&&before!=null&&!before.equals(next))net.nicomar.paintableblocks.interaction.VolumeEditing.changed(level,worldPosition);
        if(!same){shape=null;outline=null;support=null;}design=next;valid=true;rejected=null;refreshClientMesh(before);
    }
    private void refreshClientMesh(BlockDesign before){
        requestModelDataUpdate();
        if(level!=null&&level.isClientSide()){
            net.nicomar.paintableblocks.performance.VisualInvalidation.add(level,worldPosition);
            if(before==null||!before.sameGeometry(design))for(var direction:net.minecraft.core.Direction.values()){
                if(before!=null&&net.nicomar.paintableblocks.design.BoundaryGeometry.faces(before).get(direction.ordinal()).coverage().equals(net.nicomar.paintableblocks.design.BoundaryGeometry.faces(design).get(direction.ordinal()).coverage()))continue;
                var p=worldPosition.relative(direction);if(level.hasChunkAt(p)){net.nicomar.paintableblocks.performance.VisualInvalidation.add(level,p);}
            }

        }
    }
    private void refresh(){shape=null;outline=null;support=null;refreshClientMesh(null);}
    @Override protected void saveAdditional(ValueOutput output){
        super.saveAdditional(output);
        output.store("edit_identity",net.minecraft.core.UUIDUtil.CODEC,editIdentity);
        output.store("edit_revision",Codec.LONG,revision);
        if(valid)output.store("design",BlockDesign.CODEC,design);
        else output.store("design",Codec.PASSTHROUGH,rejected==null?new Dynamic<>(NbtOps.INSTANCE,new CompoundTag()):rejected);
    }
    @Override protected void loadAdditional(ValueInput input){
        if(level==null&&net.nicomar.paintableblocks.design.PlacementDesign.current(worldPosition,design.resolution())==null)loadedFromStorage=true;
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
    public boolean applySync(net.nicomar.paintableblocks.network.ModNetworking.DesignSync packet){
        if(level==null||!level.isClientSide())return false;
        if(valid&&editIdentity.equals(packet.block())&&packet.after()<=revision)return true;
        if(packet.full()==null&&(!valid||!editIdentity.equals(packet.block())||revision!=packet.before()))return false;
        if(editIdentity.equals(packet.block())&&packet.after()<revision)return true;
        try{
            BlockDesign next=packet.full();if(next==null){next=design;if(packet.plane()!=null)next=next.withSlope(packet.plane());next=next.remove(java.util.BitSet.valueOf(packet.removed().stream().mapToLong(Long::longValue).toArray())).withOrientation(packet.orientation());var changes=new java.util.TreeMap<Integer,Integer>();for(var e:packet.paints().entrySet())if(next.validSurfaceKey(e.getKey())||e.getValue()!=0)changes.put(e.getKey(),e.getValue());next=next.paintFaces(changes);}
            if(next.resolution()!=design.resolution()||next.empty())return false;accept(next);editIdentity=packet.block();revision=packet.after();return true;
        }catch(IllegalArgumentException|IllegalStateException invalid){return false;}
    }
    @Override public ModelData getModelData(){return valid?ModelData.builder().with(DESIGN_PROPERTY,design).build():ModelData.EMPTY;}
    public ItemStack toItem(boolean keepPaint){
        if(!valid)return ItemStack.EMPTY;
        ItemStack out=ModContent.stack(keepPaint?design:design.withoutPaint());
        out.applyComponents(collectComponents());out.set(ModContent.DESIGN.get(),keepPaint?design:design.withoutPaint());return out;
    }
}
