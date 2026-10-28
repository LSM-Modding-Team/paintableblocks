package net.nicomar.paintableblocks.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.blockentity.WorkstationBlockEntity;
import net.nicomar.paintableblocks.menu.WorkstationMenu;
import net.nicomar.paintableblocks.production.ProductionState;

public final class WorkstationBlock extends PortableProductionBlock {
    public static final MapCodec<WorkstationBlock> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Codec.STRING.xmap(PaintingBalance.Station::valueOf,Enum::name).fieldOf("tier").forGetter(WorkstationBlock::tier),propertiesCodec()).apply(i,WorkstationBlock::new));
    private final PaintingBalance.Station tier;
    public WorkstationBlock(PaintingBalance.Station tier,Properties p){super(p);this.tier=tier;}
    public PaintingBalance.Station tier(){return tier;}
    @Override protected MapCodec<WorkstationBlock> codec(){return CODEC;}
    @Override public boolean accepts(ProductionState s){return s.valid()&&s.total()<=tier.capacity&&s.progress()==0&&s.paint()==0&&s.items().stream().allMatch(e->e.slot()<4);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new WorkstationBlockEntity(pos,state);}
    @Override protected AbstractContainerMenu menu(int id,Inventory inventory,BlockPos pos){return new WorkstationMenu(id,inventory,pos);}
}
