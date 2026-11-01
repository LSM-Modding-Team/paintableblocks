package net.nicomar.paintableblocks.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.nicomar.paintableblocks.paint.PaintCatalog;
import net.nicomar.paintableblocks.registry.ModContent;

/** Reversibly switches the finish of one complete glass paint item. */
public final class GlassFinishRecipe extends CustomRecipe {
    private ItemStack source(CraftingInput input) {
        ItemStack source = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) continue;
            if (!source.isEmpty() || !stack.is(ModContent.PAINT.get())) return ItemStack.EMPTY;
            Integer candidate = stack.get(ModContent.PAINT_ID.get());
            if (candidate == null || !PaintCatalog.isGlass(candidate)) return ItemStack.EMPTY;
            source = stack;
        }
        return source;
    }
    @Override public boolean matches(CraftingInput input, Level level) { return !source(input).isEmpty(); }
    @Override public ItemStack assemble(CraftingInput input) {
        ItemStack source = source(input);
        if (source.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = source.copyWithCount(1);
        result.set(ModContent.PAINT_ID.get(), PaintCatalog.alternateGlass(source.get(ModContent.PAINT_ID.get())));
        return result;
    }
    @Override public RecipeSerializer<? extends CustomRecipe> getSerializer() { return ModContent.GLASS_FINISH.get(); }
}
