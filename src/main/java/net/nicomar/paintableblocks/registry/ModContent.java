package net.nicomar.paintableblocks.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.block.PaintableBlock;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.design.BlockDesign;
import net.nicomar.paintableblocks.item.*;
import net.nicomar.paintableblocks.paint.PaintCatalog;

public final class ModContent {
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<BlockDesign>> DESIGN =
            ModRegistries.DATA_COMPONENTS.registerComponentType("block_design", b -> b.persistent(BlockDesign.CODEC));
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> PAINT_ID =
            ModRegistries.DATA_COMPONENTS.registerComponentType("paint_id", b -> b.persistent(Codec.intRange(1,PaintingBalance.PAINTS)));
    private static final Map<Integer,DeferredBlock<PaintableBlock>> BLOCKS = new LinkedHashMap<>();
    private static final Map<Integer,DeferredItem<PaintableBlockItem>> ITEMS = new LinkedHashMap<>();
    static {
        for(int n:PaintingBalance.RESOLUTIONS){
            String name = n==1?"paintable":"paintable_"+n+"x"+n;
            var block = ModRegistries.BLOCKS.registerBlock(name,p->new PaintableBlock(n,p),()->BlockBehaviour.Properties.of()
                    .strength(PaintingBalance.HARDNESS,PaintingBalance.BLAST_RESISTANCE).sound(SoundType.STONE)
                    .dynamicShape().noOcclusion().pushReaction(PushReaction.BLOCK).isRedstoneConductor((s,l,p)->false));
            BLOCKS.put(n,block);
            ITEMS.put(n,ModRegistries.ITEMS.registerItem(name,p->new PaintableBlockItem(block.get(),p),
                    ()->new Item.Properties().useBlockDescriptionPrefix().component(DESIGN.get(),BlockDesign.full(n))));
        }
    }
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PaintableBlockEntity>> PAINTABLE_ENTITY =
            ModRegistries.BLOCK_ENTITY_TYPES.register("paintable",()->new BlockEntityType<>(PaintableBlockEntity::new,
                    Set.copyOf(BLOCKS.values().stream().map(DeferredBlock::get).toList())));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PaintableBlock>> PAINTABLE_TYPE =
            ModRegistries.BLOCK_TYPES.register("paintable",()->PaintableBlock.CODEC);
    public static final DeferredItem<TongsItem> TONGS = ModRegistries.ITEMS.registerItem("tongs",TongsItem::new,()->new Item.Properties().stacksTo(1));
    public static final DeferredItem<GlovesItem> GLOVES = ModRegistries.ITEMS.registerItem("gloves",GlovesItem::new,()->new Item.Properties().stacksTo(1));
    public static final DeferredItem<PaintItem> PAINT = ModRegistries.ITEMS.registerItem("paint",PaintItem::new);
    private ModContent(){}
    public static PaintableBlock block(int n){return BLOCKS.get(PaintingBalance.requireResolution(n)).get();}
    public static ItemStack stack(BlockDesign design){ItemStack out=new ItemStack(ITEMS.get(design.resolution()).get());out.set(DESIGN.get(),design);return out;}
    public static void initialize(IEventBus bus){bus.addListener(ModContent::creative);bus.addListener(ModContent::setup);}
    private static void setup(FMLCommonSetupEvent event){event.enqueueWork(PaintCatalog::validateRegistries);}
    private static void creative(BuildCreativeModeTabContentsEvent event){
        if(event.getTabKey().equals(CreativeModeTabs.BUILDING_BLOCKS))for(var item:ITEMS.values())event.accept(new ItemStack(item.get()));
        if(event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)){event.accept(new ItemStack(TONGS.get()));event.accept(new ItemStack(GLOVES.get()));}
        if(event.getTabKey().equals(CreativeModeTabs.INGREDIENTS))for(var entry:PaintCatalog.entries()){
            ItemStack paint=new ItemStack(PAINT.get());paint.set(PAINT_ID.get(),entry.paintId());event.accept(paint);
        }
    }
}
