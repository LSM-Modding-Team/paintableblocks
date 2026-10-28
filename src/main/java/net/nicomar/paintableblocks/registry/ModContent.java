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
import net.nicomar.paintableblocks.paint.*;
import net.nicomar.paintableblocks.block.PaintBucketBlock;
import net.nicomar.paintableblocks.blockentity.PaintBucketBlockEntity;
import net.nicomar.paintableblocks.menu.PaintBucketMenu;
import net.nicomar.paintableblocks.recipe.PaintExtractionRecipe;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public final class ModContent {
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<BlockDesign>> DESIGN =
            ModRegistries.DATA_COMPONENTS.registerComponentType("block_design", b -> b.persistent(BlockDesign.CODEC));
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> PAINT_ID =
            ModRegistries.DATA_COMPONENTS.registerComponentType("paint_id", b -> b.persistent(Codec.intRange(1,PaintingBalance.PAINTS)));
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<PaintContent>> BRUSH_PAINT =
            ModRegistries.DATA_COMPONENTS.registerComponentType("brush_paint",b->b.persistent(PaintContent.CODEC));
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<PaintStorage>> BUCKET_PAINTS =
            ModRegistries.DATA_COMPONENTS.registerComponentType("bucket_paints",b->b.persistent(PaintStorage.CODEC));
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> SCRAPER_USES =
            ModRegistries.DATA_COMPONENTS.registerComponentType("scraper_uses",b->b.persistent(Codec.intRange(1,PaintingBalance.Material.NETHERITE.scraperUses)));
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
    public static final List<DeferredItem<? extends Item>> EDITORS=List.of(
            ModRegistries.ITEMS.registerItem("hammer",HammerItem::new,()->new Item.Properties().stacksTo(1)),
            ModRegistries.ITEMS.registerItem("mallet",MalletItem::new,()->new Item.Properties().stacksTo(1)),
            ModRegistries.ITEMS.registerItem("chisel",ChiselItem::new,()->new Item.Properties().stacksTo(1)),
            ModRegistries.ITEMS.registerItem("face_scraper",FaceScraperItem::new,()->new Item.Properties().stacksTo(1)),
            ModRegistries.ITEMS.registerItem("cleaning_sponge",CleaningSpongeItem::new,()->new Item.Properties().stacksTo(1)),
            ModRegistries.ITEMS.registerItem("rotation_wrench",RotationWrenchItem::new,()->new Item.Properties().stacksTo(1)));
    public static final Map<PaintingBalance.Material,DeferredItem<BrushItem>> BRUSHES=new LinkedHashMap<>();
    public static final Map<PaintingBalance.Material,DeferredItem<ScraperItem>> SCRAPERS=new LinkedHashMap<>();
    public static final Map<PaintingBalance.Bucket,DeferredBlock<PaintBucketBlock>> BUCKETS=new LinkedHashMap<>();
    public static final Map<PaintingBalance.Bucket,DeferredItem<PaintBucketItem>> BUCKET_ITEMS=new LinkedHashMap<>();
    static {
        for(var material:PaintingBalance.Material.values()){
            String prefix=material.name().toLowerCase(java.util.Locale.ROOT);
            BRUSHES.put(material,ModRegistries.ITEMS.registerItem(prefix+"_brush",p->new BrushItem(material,p),()->new Item.Properties().stacksTo(1).component(BRUSH_PAINT.get(),PaintContent.EMPTY)));
            SCRAPERS.put(material,ModRegistries.ITEMS.registerItem(prefix+"_scraper",p->new ScraperItem(material,p),()->new Item.Properties().stacksTo(1).component(SCRAPER_USES.get(),material.scraperUses)));
        }
        for(var tier:PaintingBalance.Bucket.values()){
            String name=tier.name().toLowerCase(java.util.Locale.ROOT)+"_paint_bucket";
            var block=ModRegistries.BLOCKS.registerBlock(name,p->new PaintBucketBlock(tier,p),()->BlockBehaviour.Properties.of().strength(1.5F,6).sound(SoundType.METAL).noOcclusion().pushReaction(PushReaction.BLOCK));BUCKETS.put(tier,block);
            BUCKET_ITEMS.put(tier,ModRegistries.ITEMS.registerItem(name,p->new PaintBucketItem(block.get(),p),()->new Item.Properties().stacksTo(1).useBlockDescriptionPrefix().component(BUCKET_PAINTS.get(),PaintStorage.EMPTY)));
        }
    }
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PaintBucketBlockEntity>> BUCKET_ENTITY=
            ModRegistries.BLOCK_ENTITY_TYPES.register("paint_bucket",()->new BlockEntityType<>(PaintBucketBlockEntity::new,Set.copyOf(BUCKETS.values().stream().map(DeferredBlock::get).toList())));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PaintBucketBlock>> BUCKET_TYPE=ModRegistries.BLOCK_TYPES.register("paint_bucket",()->PaintBucketBlock.CODEC);
    public static final DeferredHolder<MenuType<?>,MenuType<PaintBucketMenu>> BUCKET_MENU=ModRegistries.MENUS.register("paint_bucket",()->IMenuTypeExtension.create((id,inv,buf)->new PaintBucketMenu(id,inv,buf.readBlockPos())));
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<PaintExtractionRecipe>> EXTRACTION=ModRegistries.RECIPE_SERIALIZERS.register("extract_paint",()->new RecipeSerializer<>(MapCodec.unit(PaintExtractionRecipe::new),StreamCodec.unit(new PaintExtractionRecipe())));
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<net.nicomar.paintableblocks.recipe.UpgradeRecipe>> NETHERITE_UPGRADE=ModRegistries.RECIPE_SERIALIZERS.register("upgrade_tools",()->new RecipeSerializer<>(MapCodec.unit(net.nicomar.paintableblocks.recipe.UpgradeRecipe::new),StreamCodec.unit(new net.nicomar.paintableblocks.recipe.UpgradeRecipe())));
    private ModContent(){}
    public static PaintableBlock block(int n){return BLOCKS.get(PaintingBalance.requireResolution(n)).get();}
    public static ItemStack stack(BlockDesign design){ItemStack out=new ItemStack(ITEMS.get(design.resolution()).get());out.set(DESIGN.get(),design);return out;}
    public static void initialize(IEventBus bus){ProductionContent.initialize(bus);bus.addListener(ModContent::creative);bus.addListener(ModContent::setup);bus.addListener(net.nicomar.paintableblocks.network.ModNetworking::register);net.nicomar.paintableblocks.interaction.ServerInteractions.register();}
    private static void setup(FMLCommonSetupEvent event){event.enqueueWork(PaintCatalog::validateRegistries);}
    private static void creative(BuildCreativeModeTabContentsEvent event){
        if(event.getTabKey().equals(CreativeModeTabs.BUILDING_BLOCKS))for(var item:ITEMS.values())event.accept(new ItemStack(item.get()));
        if(event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)){event.accept(new ItemStack(TONGS.get()));event.accept(new ItemStack(GLOVES.get()));for(var i:EDITORS)event.accept(new ItemStack(i.get()));for(var i:BRUSHES.values())event.accept(new ItemStack(i.get()));for(var i:SCRAPERS.values())event.accept(new ItemStack(i.get()));for(var i:BUCKET_ITEMS.values())event.accept(new ItemStack(i.get()));}
        if(event.getTabKey().equals(CreativeModeTabs.INGREDIENTS))for(var entry:PaintCatalog.entries()){
            ItemStack paint=new ItemStack(PAINT.get());paint.set(PAINT_ID.get(),entry.paintId());event.accept(paint);
        }
    }
}
