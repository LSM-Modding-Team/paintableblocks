package net.nicomar.paintableblocks.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.operation.CollectionPolicy;

public final class PaintableBlock extends BaseEntityBlock {
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty CUT =
            net.minecraft.world.level.block.state.properties.BooleanProperty.create("cut");
    public static final MapCodec<PaintableBlock> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
            Codec.INT.fieldOf("resolution").forGetter(PaintableBlock::resolution),propertiesCodec()).apply(i,PaintableBlock::new));
    private final int resolution;
    public PaintableBlock(int resolution,Properties properties){super(properties);this.resolution=PaintingBalance.requireResolution(resolution);registerDefaultState(stateDefinition.any().setValue(CUT,false));}
    public int resolution(){return resolution;}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new PaintableBlockEntity(pos,state);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
        return level.getBlockEntity(pos) instanceof PaintableBlockEntity entity?entity.shape():Shapes.block();
    }
    @Override protected VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return getShape(state,level,pos,context);}
    @Override protected VoxelShape getVisualShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return getShape(state,level,pos,context);}
    @Override protected VoxelShape getBlockSupportShape(BlockState state,BlockGetter level,BlockPos pos){return getShape(state,level,pos,CollisionContext.empty());}
    @Override protected VoxelShape getInteractionShape(BlockState state,BlockGetter level,BlockPos pos){return Shapes.empty();}
    @Override protected VoxelShape getOcclusionShape(BlockState state){return Shapes.empty();}
    @Override protected List<ItemStack> getDrops(BlockState state,LootParams.Builder params){
        BlockEntity entity=params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        return entity instanceof PaintableBlockEntity block?CollectionPolicy.drops(block,params):List.of();
    }
    @Override public ItemStack getCloneItemStack(LevelReader level,BlockPos pos,BlockState state,boolean includeData,Player player){
        if(includeData && player.isCreative() && level.getBlockEntity(pos) instanceof PaintableBlockEntity block){
            if(!block.valid()&&player instanceof net.minecraft.server.level.ServerPlayer server)server.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.paintableblocks.invalid_design"));
            return block.toItem(true);
        }
        return new ItemStack(asItem());
    }
    @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> builder){builder.add(CUT);}
    @Override protected int getLightDampening(BlockState state){return state.getValue(CUT)?0:15;}
    @Override protected boolean propagatesSkylightDown(BlockState state){return state.getValue(CUT);}

}
