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
    private int paint(CraftingInput input) {
        int id = 0;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) continue;
            if (id != 0 || !stack.is(ModContent.PAINT.get())) return 0;
            Integer candidate = stack.get(ModContent.PAINT_ID.get());
            if (candidate == null || !PaintCatalog.isGlass(candidate)) return 0;
            id = candidate;
        }
        return id;
    }
    @Override public boolean matches(CraftingInput input, Level level) { return paint(input) != 0; }
    @Override public ItemStack assemble(CraftingInput input) {
        int id = paint(input);
        if (id == 0) return ItemStack.EMPTY;
        ItemStack result = new ItemStack(ModContent.PAINT.get());
        result.set(ModContent.PAINT_ID.get(), PaintCatalog.alternateGlass(id));
        return result;
    }
    @Override public RecipeSerializer<? extends CustomRecipe> getSerializer() { return ModContent.GLASS_FINISH.get(); }
}
