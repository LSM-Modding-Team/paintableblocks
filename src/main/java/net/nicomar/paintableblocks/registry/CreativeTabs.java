package net.nicomar.paintableblocks.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.bus.api.IEventBus;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.paint.PaintCatalog;

/** Integrations can register additional tabs when their mod is present; no empty integration tabs. */
public final class CreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,"paintableblocks");
    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> BLOCKS=TABS.register("blocks",()->CreativeModeTab.builder(CreativeModeTab.Row.TOP,0)
            .title(Component.translatable("tab.paintableblocks.blocks")).icon(()->ModContent.stack(BlockDesign.full(4))).displayItems((parameters,out)->{
                for(int n:PaintingBalance.RESOLUTIONS)out.accept(ModContent.stack(BlockDesign.full(n)));
                for(int n:PaintingBalance.RESOLUTIONS)out.accept(ModContent.stack(BlockDesign.full(n).withSlope(new SlopePlane(-1,1,0,0))));
                for(var item:ProductionContent.STATION_ITEMS.values())out.accept(item.get());for(var item:ProductionContent.MACHINE_ITEMS.values())out.accept(item.get());
            }).build());
    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> TOOLS=TABS.register("tools",()->CreativeModeTab.builder(CreativeModeTab.Row.TOP,1)
            .withTabsBefore(net.minecraft.resources.ResourceKey.create(Registries.CREATIVE_MODE_TAB,Identifier.fromNamespaceAndPath("paintableblocks","blocks")))
            .title(Component.translatable("tab.paintableblocks.tools")).icon(()->new ItemStack(ModContent.EDITORS.getFirst().get())).displayItems((parameters,out)->{
                for(var i:ModContent.EDITORS)out.accept(i.get());out.accept(ModContent.SLOPE_FORMER.get());out.accept(ModContent.TONGS.get());out.accept(ModContent.GLOVES.get());
                for(var i:ModContent.BRUSHES.values())out.accept(i.get());for(var i:ModContent.SCRAPERS.values())out.accept(i.get());for(var i:ModContent.BUCKET_ITEMS.values())out.accept(i.get());
            }).build());
    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> PAINTS=TABS.register("vanilla_paints",()->CreativeModeTab.builder(CreativeModeTab.Row.TOP,2)
            .withTabsBefore(net.minecraft.resources.ResourceKey.create(Registries.CREATIVE_MODE_TAB,Identifier.fromNamespaceAndPath("paintableblocks","tools")))
            .title(Component.translatable("tab.paintableblocks.paints")).icon(()->paint(1)).displayItems((parameters,out)->{for(var entry:PaintCatalog.entries())out.accept(paint(entry.paintId()));}).build());
    private static ItemStack paint(int id){var stack=new ItemStack(ModContent.PAINT.get());stack.set(ModContent.PAINT_ID.get(),id);return stack;}
    public static void register(IEventBus bus){TABS.register(bus);}
    private CreativeTabs(){}
}
