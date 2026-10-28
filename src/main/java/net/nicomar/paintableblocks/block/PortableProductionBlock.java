package net.nicomar.paintableblocks.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.nicomar.paintableblocks.blockentity.PortableProductionEntity;
import net.nicomar.paintableblocks.interaction.ServerInteractions;
import net.nicomar.paintableblocks.production.ProductionState;

public abstract class PortableProductionBlock extends BaseEntityBlock {
    protected PortableProductionBlock(Properties p){super(p);}
    public abstract boolean accepts(ProductionState state);
    protected abstract AbstractContainerMenu menu(int id,Inventory inventory,BlockPos pos);
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    private void open(Player p,BlockPos pos){
        if(p instanceof ServerPlayer s&&ServerInteractions.allowed(p,pos)&&p.level().getBlockEntity(pos) instanceof PortableProductionEntity be&&be.valid())
            s.openMenu(new SimpleMenuProvider((id,inv,player)->menu(id,inv,pos),Component.translatable(getDescriptionId())),pos);
    }
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit){if(!level.isClientSide())open(p,pos);return InteractionResult.SUCCESS;}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player p,BlockHitResult hit){if(!level.isClientSide())open(p,pos);return InteractionResult.SUCCESS;}
    @Override protected List<ItemStack> getDrops(BlockState state,LootParams.Builder params){
        if(!(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof PortableProductionEntity be)||!be.valid())return List.of();
        if(params.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player p&&p.isCreative())return List.of();
        Float radius=params.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);return radius!=null&&params.getLevel().getRandom().nextFloat()>1.0F/radius?List.of():List.of(be.toItem());
    }
    @Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){
        if(!level.isClientSide()&&player.isCreative()&&level.getBlockEntity(pos) instanceof PortableProductionEntity be&&be.valid()&&!be.isEmpty())Block.popResource(level,pos,be.toItem());
        return super.playerWillDestroy(level,pos,state,player);
    }
    @Override public ItemStack getCloneItemStack(LevelReader level,BlockPos pos,BlockState state,boolean includeData,Player player){return includeData&&player.isCreative()&&level.getBlockEntity(pos) instanceof PortableProductionEntity be?be.toItem():new ItemStack(asItem());}
    @Override protected void affectNeighborsAfterRemoval(BlockState state,ServerLevel level,BlockPos pos,boolean moved){
        // Inventories travel inside the single block drop; never call dropContents here.
        level.updateNeighbourForOutputSignal(pos,this);
    }
}
