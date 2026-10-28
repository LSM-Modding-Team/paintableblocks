package net.nicomar.paintableblocks.operation;

import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.tags.ItemTags;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.registry.ModContent;

public final class CollectionPolicy {
    private static final Set<String> NO_DROPS=Set.of("paintableblocks:mallet","paintableblocks:chisel","paintableblocks:face_scraper","paintableblocks:cleaning_sponge");
    private CollectionPolicy(){}
    public static List<ItemStack> drops(PaintableBlockEntity block,LootParams.Builder params){
        if(!block.valid())return List.of();
        Entity actor=params.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if(actor instanceof Player player && player.isCreative())return List.of();
        ItemInstance tool=params.getOptionalParameter(LootContextParams.TOOL);
        boolean playerAction=actor instanceof Player && params.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS)==null;
        if(playerAction&&tool!=null){
            if(tool.is(ModContent.GLOVES.get()))return List.of(block.toItem(true));
            if(tool.is(ModContent.TONGS.get()))return List.of(block.toItem(false));
            Item type=tool.typeHolder().value();
            if(NO_DROPS.contains(BuiltInRegistries.ITEM.getKey(type).toString()))return List.of();
            if(!(type instanceof AxeItem) && (type instanceof HoeItem || type instanceof ShearsItem || tool.is(ItemTags.SWORDS) || tool.is(ItemTags.SPEARS) || tool.is(ItemTags.WEAPON_ENCHANTABLE) || tool.is(ItemTags.BOW_ENCHANTABLE) || tool.is(ItemTags.CROSSBOW_ENCHANTABLE) || tool.is(ItemTags.TRIDENT_ENCHANTABLE) || tool.is(ItemTags.MACE_ENCHANTABLE)))return List.of();
        }
        if(!block.design().complete())return List.of();
        // Use vanilla's explosion loot survival probability without applying it to tools.
        Float radius=params.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);
        if(radius!=null&&params.getLevel().getRandom().nextFloat()>1.0F/radius)return List.of();
        return List.of(block.toItem(false));
    }
}
