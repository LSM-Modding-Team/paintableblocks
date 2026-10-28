package net.nicomar.paintableblocks.registry;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.block.*;
import net.nicomar.paintableblocks.blockentity.*;
import net.nicomar.paintableblocks.item.ProductionBlockItem;
import net.nicomar.paintableblocks.menu.*;
import net.nicomar.paintableblocks.production.ProductionState;
import net.nicomar.paintableblocks.recipe.*;

public final class ProductionContent {
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<ProductionState>> PRODUCTION_CONTENTS=ModRegistries.DATA_COMPONENTS.registerComponentType("production_contents",b->b.persistent(ProductionState.CODEC));
    public static final Map<PaintingBalance.Station,DeferredBlock<WorkstationBlock>> STATIONS=new LinkedHashMap<>();
    public static final Map<PaintingBalance.Station,DeferredItem<ProductionBlockItem>> STATION_ITEMS=new LinkedHashMap<>();
    public static final Map<PaintingBalance.Production,DeferredBlock<PaintMachineBlock>> MACHINES=new LinkedHashMap<>();
    public static final Map<PaintingBalance.Production,DeferredItem<ProductionBlockItem>> MACHINE_ITEMS=new LinkedHashMap<>();
    static {
        for(var tier:PaintingBalance.Station.values()){
            String name=tier.name().toLowerCase(Locale.ROOT);var block=ModRegistries.BLOCKS.registerBlock(name,p->new WorkstationBlock(tier,p),ProductionContent::properties);STATIONS.put(tier,block);
            STATION_ITEMS.put(tier,ModRegistries.ITEMS.registerItem(name,p->new ProductionBlockItem(block.get(),p),ProductionContent::itemProperties));
        }
        for(var tier:List.of(PaintingBalance.Production.MILL,PaintingBalance.Production.PRESS)){
            String name=tier==PaintingBalance.Production.MILL?"paint_mill":"paint_press";var block=ModRegistries.BLOCKS.registerBlock(name,p->new PaintMachineBlock(tier,p),ProductionContent::properties);MACHINES.put(tier,block);
            MACHINE_ITEMS.put(tier,ModRegistries.ITEMS.registerItem(name,p->new ProductionBlockItem(block.get(),p),ProductionContent::itemProperties));
        }
    }
    private static BlockBehaviour.Properties properties(){return BlockBehaviour.Properties.of().strength(3.5F,6).sound(SoundType.STONE).pushReaction(PushReaction.BLOCK);}
    private static Item.Properties itemProperties(){return new Item.Properties().stacksTo(1).useBlockDescriptionPrefix().component(PRODUCTION_CONTENTS.get(),ProductionState.EMPTY);}
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<WorkstationBlockEntity>> WORKSTATION_ENTITY=ModRegistries.BLOCK_ENTITY_TYPES.register("workstation",()->new BlockEntityType<>(WorkstationBlockEntity::new,Set.copyOf(STATIONS.values().stream().map(DeferredBlock::get).toList())));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PaintMachineBlockEntity>> MACHINE_ENTITY=ModRegistries.BLOCK_ENTITY_TYPES.register("paint_machine",()->new BlockEntityType<>(PaintMachineBlockEntity::new,Set.copyOf(MACHINES.values().stream().map(DeferredBlock::get).toList())));
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<WorkstationBlock>> WORKSTATION_TYPE=ModRegistries.BLOCK_TYPES.register("workstation",()->WorkstationBlock.CODEC);
    public static final DeferredHolder<MapCodec<? extends Block>,MapCodec<PaintMachineBlock>> MACHINE_TYPE=ModRegistries.BLOCK_TYPES.register("paint_machine",()->PaintMachineBlock.CODEC);
    public static final DeferredHolder<MenuType<?>,MenuType<WorkstationMenu>> WORKSTATION_MENU=ModRegistries.MENUS.register("workstation",()->IMenuTypeExtension.create((id,inv,buf)->new WorkstationMenu(id,inv,buf.readBlockPos())));
    public static final DeferredHolder<MenuType<?>,MenuType<PaintMachineMenu>> MACHINE_MENU=ModRegistries.MENUS.register("paint_machine",()->IMenuTypeExtension.create((id,inv,buf)->new PaintMachineMenu(id,inv,buf.readBlockPos())));
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<PaintableCraftingRecipe>> MANUAL=ModRegistries.RECIPE_SERIALIZERS.register("paintable_crafting",()->new RecipeSerializer<>(MapCodec.unit(PaintableCraftingRecipe::new),StreamCodec.unit(new PaintableCraftingRecipe())));
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<ProgressionRecipe>> PROGRESSION=ModRegistries.RECIPE_SERIALIZERS.register("progression",()->new RecipeSerializer<>(MapCodec.unit(ProgressionRecipe::new),StreamCodec.unit(new ProgressionRecipe())));
    private ProductionContent(){}
    public static void initialize(IEventBus bus){bus.addListener(ProductionContent::creative);}
    private static void creative(BuildCreativeModeTabContentsEvent event){if(event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)){for(var item:STATION_ITEMS.values())event.accept(new ItemStack(item.get()));for(var item:MACHINE_ITEMS.values())event.accept(new ItemStack(item.get()));}}
}
