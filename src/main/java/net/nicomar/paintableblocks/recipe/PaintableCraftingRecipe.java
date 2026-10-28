package net.nicomar.paintableblocks.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.nicomar.paintableblocks.design.BlockDesign;
import net.nicomar.paintableblocks.production.ProductionInputs;
import net.nicomar.paintableblocks.registry.*;

public final class PaintableCraftingRecipe extends CustomRecipe {
    private net.nicomar.paintableblocks.balance.PaintingBalance.Material material(CraftingInput input){
        if(input.width()!=3||input.height()!=3||!ProductionInputs.center(input.getItem(4)))return null;var outer=input.getItem(0);var material=ProductionInputs.material(outer);if(material==null)return null;
        for(int i=0;i<9;i++)if(i!=4&&(!ProductionInputs.plain(input.getItem(i))||!ItemStack.isSameItemSameComponents(outer,input.getItem(i))))return null;return material;
    }
    @Override public boolean matches(CraftingInput input,Level level){return material(input)!=null;}
    @Override public ItemStack assemble(CraftingInput input){var m=material(input);if(m==null)return ItemStack.EMPTY;var out=ModContent.stack(BlockDesign.full(1));out.setCount(m.manualYield);return out;}
    @Override public RecipeSerializer<? extends CustomRecipe> getSerializer(){return ProductionContent.MANUAL.get();}
}
