package net.nicomar.paintableblocks.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.nicomar.paintableblocks.block.PaintBucketBlock;
import net.nicomar.paintableblocks.registry.ModContent;

public final class PaintBucketItem extends BlockItem {
    private final PaintBucketBlock bucket;
    public PaintBucketItem(PaintBucketBlock block,Properties p){super(block,p);bucket=block;}
    @Override public InteractionResult place(BlockPlaceContext context){
        var storage=context.getItemInHand().get(ModContent.BUCKET_PAINTS.get());
        if(storage==null||!storage.fits(bucket.tier()))return InteractionResult.FAIL;
        return super.place(context);
    }
    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,net.minecraft.world.item.Item.TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<net.minecraft.network.chat.Component> lines,net.minecraft.world.item.TooltipFlag flag){
        super.appendHoverText(stack,context,display,lines,flag);
        var storage=stack.get(ModContent.BUCKET_PAINTS.get());if(storage==null||!storage.fits(bucket.tier())){lines.accept(net.minecraft.network.chat.Component.translatable("message.paintableblocks.invalid_design"));return;}
        lines.accept(net.minecraft.network.chat.Component.translatable("tooltip.paintableblocks.bucket",storage.entries().size(),bucket.tier().paints,bucket.tier().capacityPerPaint));
        for(var entry:storage.entries())lines.accept(net.minecraft.network.chat.Component.translatable(net.nicomar.paintableblocks.paint.PaintCatalog.byId(entry.paint()).translationKey()).append(": "+entry.units()));
    }
}
