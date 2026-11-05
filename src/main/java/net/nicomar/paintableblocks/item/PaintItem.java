package net.nicomar.paintableblocks.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.nicomar.paintableblocks.paint.PaintCatalog;
import net.nicomar.paintableblocks.registry.ModContent;

/** Catalog identity and creative-only boundary painting; survival stock remains unchanged. */
public final class PaintItem extends Item {
    public PaintItem(Properties properties){super(properties);}
    @Override public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context){
        if(context.getPlayer()==null||!context.getPlayer().isCreative()||!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof net.nicomar.paintableblocks.blockentity.PaintableBlockEntity))return net.minecraft.world.InteractionResult.PASS;
        if(!context.getLevel().isClientSide())net.nicomar.paintableblocks.interaction.ServerInteractions.nativeUse(context);
        return net.minecraft.world.InteractionResult.SUCCESS;
    }
    @Override public Component getName(ItemStack stack){
        Integer id=stack.get(ModContent.PAINT_ID.get());
        return id!=null&&PaintCatalog.validId(id)?Component.translatable("item.paintableblocks.paint_of",Component.translatable(PaintCatalog.byId(id).translationKey())).append(PaintCatalog.byId(id).survivalAvailable()?Component.empty():Component.translatable("item.paintableblocks.creative_only")):super.getName(stack);
    }
}
