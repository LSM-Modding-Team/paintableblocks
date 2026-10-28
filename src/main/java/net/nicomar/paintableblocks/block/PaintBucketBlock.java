package net.nicomar.paintableblocks.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.blockentity.PaintBucketBlockEntity;
import net.nicomar.paintableblocks.menu.PaintBucketMenu;
import net.nicomar.paintableblocks.interaction.ServerInteractions;

public final class PaintBucketBlock extends BaseEntityBlock {
    public static final MapCodec<PaintBucketBlock> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
            com.mojang.serialization.Codec.STRING.xmap(PaintingBalance.Bucket::valueOf,Enum::name).fieldOf("tier").forGetter(PaintBucketBlock::tier),propertiesCodec()).apply(i,PaintBucketBlock::new));
    private final PaintingBalance.Bucket tier;
    public PaintBucketBlock(PaintingBalance.Bucket tier,Properties p){super(p);this.tier=tier;}
    public PaintingBalance.Bucket tier(){return tier;}
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return net.nicomar.paintableblocks.design.VisualShapes.of(tier.name().toLowerCase(java.util.Locale.ROOT)+"_paint_bucket");}
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return getShape(state,level,pos,context);}
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getVisualShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return getShape(state,level,pos,context);}
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getBlockSupportShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos){return net.nicomar.paintableblocks.design.VisualShapes.of(tier.name().toLowerCase(java.util.Locale.ROOT)+"_paint_bucket");}
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getInteractionShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos){return net.minecraft.world.phys.shapes.Shapes.empty();}
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getOcclusionShape(BlockState state){return net.minecraft.world.phys.shapes.Shapes.empty();}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state){return net.minecraft.world.level.block.RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new PaintBucketBlockEntity(pos,state);}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!level.isClientSide())ServerInteractions.bucket(player,pos,hand,player.isShiftKeyDown());
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(!level.isClientSide())open(player,pos);return InteractionResult.SUCCESS;
    }
    public static void open(Player player,BlockPos pos){
        if(player instanceof ServerPlayer server&&ServerInteractions.allowed(player,pos)&&player.level().getBlockEntity(pos) instanceof PaintBucketBlockEntity be&&be.valid())
            server.openMenu(new SimpleMenuProvider((id,inv,p)->new PaintBucketMenu(id,inv,pos),Component.translatable("container.paintableblocks.paint_bucket")),pos);
    }
    @Override protected List<ItemStack> getDrops(BlockState state,LootParams.Builder params){
        if(!(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof PaintBucketBlockEntity be)||!be.valid())return List.of();
        if(params.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player p&&p.isCreative())return List.of();
        Float radius=params.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);
        return radius!=null&&params.getLevel().getRandom().nextFloat()>1.0F/radius?List.of():List.of(be.toItem());
    }
    @Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){
        if(!level.isClientSide()&&player.isCreative()&&level.getBlockEntity(pos) instanceof PaintBucketBlockEntity be&&be.valid()&&!be.storage().entries().isEmpty())Block.popResource(level,pos,be.toItem());
        return super.playerWillDestroy(level,pos,state,player);
    }
    @Override protected ItemStack getCloneItemStack(LevelReader level,BlockPos pos,BlockState state,boolean includeData){
        return includeData&&level.getBlockEntity(pos) instanceof PaintBucketBlockEntity be?be.toItem():new ItemStack(asItem());
    }
}
