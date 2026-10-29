package net.nicomar.paintableblocks.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.nicomar.paintableblocks.paint.PaintCatalog;
import net.nicomar.paintableblocks.registry.ModContent;

/** Catalog identity only. Extraction and brush application belong to group 2. */
public final class PaintItem extends Item {
    public PaintItem(Properties properties){super(properties);}
    @Override public Component getName(ItemStack stack){
        Integer id=stack.get(ModContent.PAINT_ID.get());
        return id!=null&&PaintCatalog.validId(id)?Component.translatable("item.paintableblocks.paint_of",Component.translatable(PaintCatalog.byId(id).translationKey())).append(PaintCatalog.byId(id).survivalAvailable()?Component.empty():Component.translatable("item.paintableblocks.creative_only")):super.getName(stack);
    }
}
