package net.nicomar.paintableblocks;

import java.util.ArrayList;
import java.util.List;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingInput;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.production.*;
import net.nicomar.paintableblocks.registry.*;
import net.nicomar.paintableblocks.recipe.ProgressionRecipe;

/** Invoke only after mod registries are available in an explicitly authorized test environment. */
public final class ProductionContractChecks {
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    public static void runAfterRegistries(){counts();outputs();portable();upgrades();}
    private static void counts(){
        for(var tier:PaintingBalance.Station.values())for(var material:PaintingBalance.Material.values())for(int n:PaintingBalance.RESOLUTIONS){
            int yield=tier.yield(material);var empty=List.of(0,0,0,0,0);var result=WorkstationOperations.produce(empty,tier,n,material);
            check(result.stream().mapToInt(Integer::intValue).sum()==yield,"Batch yield uses the shared material/station balance");
            var full=List.of(tier.capacity-yield+1,0,0,0,0);check(WorkstationOperations.produce(full,tier,n,material).equals(full),"Insufficient capacity rejects the complete batch");
            var converted=WorkstationOperations.convert(result,PaintingBalance.RESOLUTIONS.indexOf(n),(PaintingBalance.RESOLUTIONS.indexOf(n)+1)%5,64);
            check(converted.stream().mapToInt(Integer::intValue).sum()==yield,"Conversion cannot create or discard blocks");
        }
    }
    private static void outputs(){
        var source=new ItemStack(Items.STONE,64);var full=List.of(new ItemStack(Items.COBBLESTONE,64),new ItemStack(Items.COBBLESTONE,64),new ItemStack(Items.COBBLESTONE,64),new ItemStack(Items.COBBLESTONE,64));
        check(PaintMachineOperations.outputs(full,source,PaintingBalance.Production.PRESS)==null&&source.getCount()==64,"A blocked output cannot consume input");
        var snow=new ItemStack(Items.POWDER_SNOW_BUCKET);var oneFree=List.of(ItemStack.EMPTY,full.get(1),full.get(2),full.get(3));
        check(PaintMachineOperations.outputs(oneFree,snow,PaintingBalance.Production.MILL)==null,"Powder snow requires room for paint and its empty bucket");
        var twoFree=List.of(ItemStack.EMPTY,ItemStack.EMPTY,full.get(2),full.get(3));
        for(var tier:List.of(PaintingBalance.Production.MILL,PaintingBalance.Production.PRESS)){
            var output=PaintMachineOperations.outputs(twoFree,snow,tier);check(output!=null,"Two empty slots admit the batch and remainder");
            check(output.stream().filter(s->s.is(ModContent.PAINT.get())).mapToInt(ItemStack::getCount).sum()==tier.itemsPerBlock,"Paint output is exact");
            check(output.stream().filter(s->s.is(Items.BUCKET)).mapToInt(ItemStack::getCount).sum()==1&&snow.getCount()==1,"One empty bucket without mutating source planning");
        }
        check(ProductionInputs.paint(new ItemStack(Items.BEDROCK))==0,"Creative-only paint has no machine recipe");
        var named=new ItemStack(Items.STONE);named.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Keep me"));check(ProductionInputs.paint(named)==0,"Source components cannot be silently lost");
    }
    private static void portable(){
        var input=new ItemStackTemplate(Items.STONE,37);var state=new ProductionState(List.of(0,0,0,0,0),List.of(new ProductionState.Stored(0,input)),79,ProductionInputs.paint(input.create()));
        var json=ProductionState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();check(ProductionState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().equals(state),"Portable contents retain progress and exact item quantities");
        check(!new ProductionState(state.blocks(),List.of(new ProductionState.Stored(0,input),new ProductionState.Stored(0,input)),0,0).valid(),"Duplicate slots are rejected");
    }
    private static void upgrades(){
        var base=new ItemStack(ModContent.BUCKET_ITEMS.get(PaintingBalance.Bucket.GOLD).get());base.set(ModContent.BUCKET_PAINTS.get(),net.nicomar.paintableblocks.paint.PaintStorage.EMPTY.add(1,PaintingBalance.Bucket.GOLD.capacityPerPaint));
        var recipe=new ProgressionRecipe();var slots=new ArrayList<ItemStack>(List.of(base,new ItemStack(Items.DIAMOND),new ItemStack(Items.DIAMOND),new ItemStack(Items.DIAMOND),new ItemStack(Items.DIAMOND),ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY));
        check(recipe.assemble(CraftingInput.of(3,3,slots)).isEmpty(),"Gold-to-diamond refuses excessive per-paint contents");
        check(base.get(ModContent.BUCKET_PAINTS.get()).units(1)==PaintingBalance.Bucket.GOLD.capacityPerPaint,"Rejected upgrade keeps all paint");
        var station=new ItemStack(ProductionContent.STATION_ITEMS.get(PaintingBalance.Station.WORKBENCH).get());station.set(ProductionContent.PRODUCTION_CONTENTS.get(),new ProductionState(List.of(1,0,0,0,0),List.of(),0,0));
        slots=new ArrayList<>(List.of(station,new ItemStack(Items.IRON_INGOT),new ItemStack(Items.IRON_INGOT),new ItemStack(Items.IRON_INGOT),new ItemStack(Items.IRON_INGOT),new ItemStack(Items.COPPER_INGOT),new ItemStack(Items.COPPER_INGOT),new ItemStack(Items.DIAMOND),new ItemStack(Items.REDSTONE)));
        check(recipe.assemble(CraftingInput.of(3,3,slots)).isEmpty(),"A nonempty station cannot be consumed by an upgrade");
    }
}
