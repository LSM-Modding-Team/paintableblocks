package net.nicomar.paintableblocks.production;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.design.BlockDesign;
import net.nicomar.paintableblocks.item.PaintableBlockItem;
import net.nicomar.paintableblocks.paint.PaintCatalog;
import net.nicomar.paintableblocks.registry.ModContent;

public final class ProductionInputs {
    private ProductionInputs(){}
    public static final List<Item> OUTER=List.of(Items.COBBLESTONE,Items.COPPER_INGOT,Items.IRON_INGOT,Items.GOLD_INGOT,Items.DIAMOND,Items.NETHERITE_INGOT);
    public static boolean plain(ItemStack stack){return !stack.isEmpty()&&ItemStack.isSameItemSameComponents(stack,new ItemStack(stack.getItem()));}
    public static boolean center(ItemStack stack){return plain(stack)&&List.of(Items.STONE,Items.DEEPSLATE,Items.ANDESITE,Items.DIORITE,Items.GRANITE,Items.TUFF).contains(stack.getItem());}
    public static PaintingBalance.Material material(ItemStack stack){int index=plain(stack)?OUTER.indexOf(stack.getItem()):-1;return index<0?null:PaintingBalance.Material.values()[index];}
    public static int paint(ItemStack stack){
        if(!plain(stack))return 0;var id=stack.is(Items.POWDER_SNOW_BUCKET)?net.minecraft.resources.Identifier.parse("minecraft:powder_snow"):BuiltInRegistries.ITEM.getKey(stack.getItem());
        return Paints.IDS.getOrDefault(id,0);
    }
    private static final class Paints{static final java.util.Map<net.minecraft.resources.Identifier,Integer> IDS=PaintCatalog.entries().stream().filter(PaintCatalog.Entry::survivalAvailable).collect(java.util.stream.Collectors.toUnmodifiableMap(PaintCatalog.Entry::identifier,PaintCatalog.Entry::paintId));}

    public static int storable(ItemStack stack){
        if(!(stack.getItem() instanceof PaintableBlockItem))return -1;var design=stack.get(ModContent.DESIGN.get());
        if(design==null||!design.complete()||design.painted())return -1;
        // Orientation is a visible property once personalized: counters accept canonical new blocks only.
        return ItemStack.isSameItemSameComponents(stack,ModContent.stack(BlockDesign.full(design.resolution())))?PaintingBalance.RESOLUTIONS.indexOf(design.resolution()):-1;
    }
}
