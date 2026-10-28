package net.nicomar.paintableblocks.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.nicomar.paintableblocks.block.PaintableBlock;

/** Functional conservation tool; final visual assets are a separate task. */
public final class TongsItem extends Item {
    public TongsItem(Properties properties){super(properties);}
    @Override public float getDestroySpeed(ItemStack stack,BlockState state){return state.getBlock() instanceof PaintableBlock?6.0F:super.getDestroySpeed(stack,state);}
}
