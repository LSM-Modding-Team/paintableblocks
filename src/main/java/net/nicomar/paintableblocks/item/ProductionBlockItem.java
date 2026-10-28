package net.nicomar.paintableblocks.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.nicomar.paintableblocks.block.PortableProductionBlock;
import net.nicomar.paintableblocks.registry.ModContent;

public final class ProductionBlockItem extends BlockItem {
    private final PortableProductionBlock block;
    public ProductionBlockItem(PortableProductionBlock block,Properties p){super(block,p);this.block=block;}
    @Override public InteractionResult place(BlockPlaceContext context){var contents=context.getItemInHand().get(net.nicomar.paintableblocks.registry.ProductionContent.PRODUCTION_CONTENTS.get());return contents!=null&&block.accepts(contents)?super.place(context):InteractionResult.FAIL;}
    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,net.minecraft.world.item.Item.TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<net.minecraft.network.chat.Component> lines,net.minecraft.world.item.TooltipFlag flag){
        super.appendHoverText(stack,context,display,lines,flag);var contents=stack.get(net.nicomar.paintableblocks.registry.ProductionContent.PRODUCTION_CONTENTS.get());
        if(contents==null||!block.accepts(contents)){lines.accept(net.minecraft.network.chat.Component.translatable("message.paintableblocks.invalid_design"));return;}
        lines.accept(net.minecraft.network.chat.Component.translatable("tooltip.paintableblocks.production",contents.total(),contents.items().size(),contents.progress(),net.nicomar.paintableblocks.balance.PaintingBalance.MACHINE_TICKS));
    }
}
