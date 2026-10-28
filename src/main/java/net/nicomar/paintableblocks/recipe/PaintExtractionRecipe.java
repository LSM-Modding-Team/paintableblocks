package net.nicomar.paintableblocks.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.nicomar.paintableblocks.item.ScraperItem;
import net.nicomar.paintableblocks.paint.PaintCatalog;
import net.nicomar.paintableblocks.registry.ModContent;

/** One source + scraper, or eight identical sources around a central scraper. */
public final class PaintExtractionRecipe extends CustomRecipe {
    private record Match(int scraper,int paint,int blocks){}
    private Match match(CraftingInput input){
        int tool=-1,blocks=0,paint=0;ScraperItem scraper=null;
        for(int slot=0;slot<input.size();slot++){
            var stack=input.getItem(slot);if(stack.isEmpty())continue;
            if(stack.getItem() instanceof ScraperItem item){if(tool!=-1||stack.getCount()!=1)return null;tool=slot;scraper=item;continue;}
            int id=net.nicomar.paintableblocks.production.ProductionInputs.paint(stack);
            if(id==0||paint!=0&&paint!=id)return null;paint=id;blocks++;
        }
        if(scraper==null||blocks!=1&&blocks!=8||!scraper.valid(input.getItem(tool),blocks))return null;
        if(blocks==8&&(input.width()!=3||input.height()!=3||tool!=4))return null;
        return new Match(tool,paint,blocks);
    }
    @Override public boolean matches(CraftingInput input,Level level){return match(input)!=null;}
    @Override public ItemStack assemble(CraftingInput input){var m=match(input);if(m==null)return ItemStack.EMPTY;var out=new ItemStack(ModContent.PAINT.get(),m.blocks);out.set(ModContent.PAINT_ID.get(),m.paint);return out;}
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingInput input){
        var out=NonNullList.withSize(input.size(),ItemStack.EMPTY);var m=match(input);if(m==null)return out;
        for(int slot=0;slot<input.size();slot++){
            var stack=input.getItem(slot);if(slot==m.scraper){var tool=(ScraperItem)stack.getItem();int remaining=tool.uses(stack)-m.blocks;if(remaining>0){var copy=stack.copyWithCount(1);copy.set(ModContent.SCRAPER_USES.get(),remaining);out.set(slot,copy);}}
            else if(stack.is(Items.POWDER_SNOW_BUCKET))out.set(slot,new ItemStack(Items.BUCKET));
            else if(!stack.isEmpty()&&stack.getItem().getCraftingRemainder()!=null)out.set(slot,stack.getItem().getCraftingRemainder().create());
        }
        return out;
    }
    @Override public RecipeSerializer<? extends CustomRecipe> getSerializer(){return ModContent.EXTRACTION.get();}
}
