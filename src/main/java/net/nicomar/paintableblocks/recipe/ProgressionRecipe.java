package net.nicomar.paintableblocks.recipe;

import java.util.*;
import java.util.function.Function;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.block.PortableProductionBlock;
import net.nicomar.paintableblocks.item.BrushItem;
import net.nicomar.paintableblocks.production.ProductionInputs;
import net.nicomar.paintableblocks.registry.*;

/** Data-aware upgrades cannot use vanilla shaped output copying, which would discard contents. */
public final class ProgressionRecipe extends CustomRecipe {
    private record Rule(Item base,Map<Item,Integer> ingredients,Function<ItemStack,ItemStack> result){}
    private static ItemStack retain(ItemStack base,Item target){var out=new ItemStack(target);out.applyComponents(base.getComponentsPatch());return out;}
    private static Rule station(Item from,Item to,Map<Item,Integer> ingredients){return new Rule(from,ingredients,base->{
        var contents=base.get(ProductionContent.PRODUCTION_CONTENTS.get());if(contents==null||!contents.empty()||!contents.valid()||!(base.getItem() instanceof BlockItem block)||!(block.getBlock() instanceof PortableProductionBlock production)||!production.accepts(contents))return ItemStack.EMPTY;
        return retain(base,to);
    });}
    private static List<Rule> rules(){
        var rules=new ArrayList<Rule>();
        rules.add(station(ProductionContent.STATION_ITEMS.get(PaintingBalance.Station.WORKBENCH).get(),ProductionContent.STATION_ITEMS.get(PaintingBalance.Station.CUTTER).get(),Map.of(Items.IRON_INGOT,4,Items.COPPER_INGOT,2,Items.DIAMOND,1,Items.REDSTONE,1)));
        rules.add(station(ProductionContent.STATION_ITEMS.get(PaintingBalance.Station.CUTTER).get(),ProductionContent.STATION_ITEMS.get(PaintingBalance.Station.ASSEMBLER).get(),Map.of(Items.DIAMOND,4,Items.GOLD_INGOT,2,Items.REDSTONE,1,Items.IRON_INGOT,1)));
        rules.add(station(ProductionContent.MACHINE_ITEMS.get(PaintingBalance.Production.MILL).get(),ProductionContent.MACHINE_ITEMS.get(PaintingBalance.Production.PRESS).get(),Map.of(Items.IRON_INGOT,4,Items.DIAMOND,2,Items.REDSTONE,2)));
        var materials=PaintingBalance.Material.values();
        for(int i=1;i<materials.length-1;i++){var from=materials[i-1];var to=materials[i];rules.add(new Rule(ModContent.BRUSHES.get(from).get(),Map.of(ProductionInputs.OUTER.get(i),1),base->{
            var brush=(BrushItem)base.getItem();if(!brush.valid(base)||!brush.content(base).fits(to.brushCapacity))return ItemStack.EMPTY;return retain(base,ModContent.BRUSHES.get(to).get());
        }));}
        var buckets=new PaintingBalance.Bucket[]{PaintingBalance.Bucket.COPPER,PaintingBalance.Bucket.GOLD,PaintingBalance.Bucket.DIAMOND};
        for(int i=1;i<buckets.length;i++){var from=buckets[i-1];var to=buckets[i];Item material=i==1?Items.GOLD_INGOT:Items.DIAMOND;rules.add(new Rule(ModContent.BUCKET_ITEMS.get(from).get(),Map.of(material,4),base->{
            var storage=base.get(ModContent.BUCKET_PAINTS.get());if(storage==null||!storage.fits(from)||!storage.fits(to))return ItemStack.EMPTY;return retain(base,ModContent.BUCKET_ITEMS.get(to).get());
        }));}
        return List.copyOf(rules);
    }
    private static final class Holder{static final List<Rule> RULES=rules();}
    @Override public boolean matches(CraftingInput input,Level level){return !assemble(input).isEmpty();}
    @Override public ItemStack assemble(CraftingInput input){
        for(var rule:Holder.RULES){ItemStack base=ItemStack.EMPTY;var ingredients=new HashMap<Item,Integer>();boolean invalid=false;
            for(int i=0;i<input.size();i++){var stack=input.getItem(i);if(stack.isEmpty())continue;
                if(stack.is(rule.base)){if(!base.isEmpty()||stack.getCount()!=1){invalid=true;break;}base=stack;}
                else if(!ProductionInputs.plain(stack)){invalid=true;break;}else ingredients.merge(stack.getItem(),1,Integer::sum);
            }
            if(!invalid&&!base.isEmpty()&&ingredients.equals(rule.ingredients))return rule.result.apply(base);
        }return ItemStack.EMPTY;
    }
    @Override public RecipeSerializer<? extends CustomRecipe> getSerializer(){return ProductionContent.PROGRESSION.get();}
}
