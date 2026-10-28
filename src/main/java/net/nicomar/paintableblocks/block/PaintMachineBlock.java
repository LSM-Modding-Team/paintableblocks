package net.nicomar.paintableblocks.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.blockentity.PaintMachineBlockEntity;
import net.nicomar.paintableblocks.menu.PaintMachineMenu;
import net.nicomar.paintableblocks.production.ProductionState;
import net.nicomar.paintableblocks.registry.ModContent;

public final class PaintMachineBlock extends PortableProductionBlock {
    public static final MapCodec<PaintMachineBlock> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Codec.STRING.xmap(PaintingBalance.Production::valueOf,Enum::name).fieldOf("tier").forGetter(PaintMachineBlock::tier),propertiesCodec()).apply(i,PaintMachineBlock::new));
    private final PaintingBalance.Production tier;
    public PaintMachineBlock(PaintingBalance.Production tier,Properties p){super(p);if(tier==PaintingBalance.Production.SCRAPER)throw new IllegalArgumentException("Not a machine");this.tier=tier;}
    public PaintingBalance.Production tier(){return tier;}
    @Override protected String visualName(){return tier==PaintingBalance.Production.MILL?"paint_mill":"paint_press";}
    @Override protected MapCodec<PaintMachineBlock> codec(){return CODEC;}
    @Override public boolean accepts(ProductionState s){return s.valid()&&s.total()==0;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new PaintMachineBlockEntity(pos,state);}
    @Override protected AbstractContainerMenu menu(int id,Inventory inventory,BlockPos pos){return new PaintMachineMenu(id,inventory,pos);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide()?null:createTickerHelper(type,net.nicomar.paintableblocks.registry.ProductionContent.MACHINE_ENTITY.get(),PaintMachineBlockEntity::tick);}
}
