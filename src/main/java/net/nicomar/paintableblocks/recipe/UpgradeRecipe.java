package net.nicomar.paintableblocks.recipe;

import java.util.Optional;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.item.*;
import net.nicomar.paintableblocks.registry.ModContent;

/** Shared netherite upgrade: copies patches and replaces only tier-dependent state. */
public final class UpgradeRecipe extends SimpleSmithingRecipe {
    public UpgradeRecipe(){super(new Recipe.CommonInfo(false));}
    @Override public Optional<Ingredient> templateIngredient(){return Optional.of(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));}
    @Override public Ingredient baseIngredient(){return Ingredient.of(ModContent.BRUSHES.get(PaintingBalance.Material.DIAMOND).get(),ModContent.SCRAPERS.get(PaintingBalance.Material.DIAMOND).get(),ModContent.BUCKET_ITEMS.get(PaintingBalance.Bucket.DIAMOND).get());}
    @Override public Optional<Ingredient> additionIngredient(){return Optional.of(Ingredient.of(Items.NETHERITE_INGOT));}
    @Override protected PlacementInfo createPlacementInfo(){return PlacementInfo.createFromOptionals(java.util.List.of(templateIngredient(),Optional.of(baseIngredient()),additionIngredient()));}
    @Override public boolean matches(SmithingRecipeInput input,Level level){return input.template().is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)&&input.addition().is(Items.NETHERITE_INGOT)&&!assemble(input).isEmpty();}
    @Override public ItemStack assemble(SmithingRecipeInput input){
        var base=input.base();ItemStack out;
        if(base.is(ModContent.BRUSHES.get(PaintingBalance.Material.DIAMOND).get())){
            if(!((BrushItem)base.getItem()).valid(base))return ItemStack.EMPTY;out=new ItemStack(ModContent.BRUSHES.get(PaintingBalance.Material.NETHERITE).get());out.applyComponents(base.getComponentsPatch());
        }else if(base.is(ModContent.SCRAPERS.get(PaintingBalance.Material.DIAMOND).get())){
            var scraper=(ScraperItem)base.getItem();if(!scraper.valid(base,1))return ItemStack.EMPTY;
            int uses=(int)((long)scraper.uses(base)*PaintingBalance.Material.NETHERITE.scraperUses/PaintingBalance.Material.DIAMOND.scraperUses);
            out=new ItemStack(ModContent.SCRAPERS.get(PaintingBalance.Material.NETHERITE).get());out.applyComponents(base.getComponentsPatch());out.set(ModContent.SCRAPER_USES.get(),uses);
        }else if(base.is(ModContent.BUCKET_ITEMS.get(PaintingBalance.Bucket.DIAMOND).get())){
            var storage=base.get(ModContent.BUCKET_PAINTS.get());if(storage==null||!storage.fits(PaintingBalance.Bucket.DIAMOND)||!storage.fits(PaintingBalance.Bucket.NETHERITE))return ItemStack.EMPTY;
            out=new ItemStack(ModContent.BUCKET_ITEMS.get(PaintingBalance.Bucket.NETHERITE).get());out.applyComponents(base.getComponentsPatch());
        }else return ItemStack.EMPTY;
        return out;
    }
    @Override public RecipeSerializer<? extends SimpleSmithingRecipe> getSerializer(){return ModContent.NETHERITE_UPGRADE.get();}
}
