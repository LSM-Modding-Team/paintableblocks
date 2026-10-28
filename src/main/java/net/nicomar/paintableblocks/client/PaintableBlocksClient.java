package net.nicomar.paintableblocks.client;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.nicomar.paintableblocks.PaintableBlocks;
import net.nicomar.paintableblocks.client.model.*;
import net.nicomar.paintableblocks.client.selection.SelectionPreview;

@Mod(value=PaintableBlocks.MOD_ID,dist=Dist.CLIENT)
public final class PaintableBlocksClient {
    public PaintableBlocksClient(IEventBus bus){bus.addListener(PaintableBlocksClient::blocks);bus.addListener(PaintableBlocksClient::items);bus.addListener(SelectionPreview::payloads);bus.addListener(PaintableBlocksClient::screens);SelectionPreview.register();}
    private static void screens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event){event.register(net.nicomar.paintableblocks.registry.ModContent.BUCKET_MENU.get(),net.nicomar.paintableblocks.client.screen.PaintBucketScreen::new);}
    private static Identifier id(String path){return Identifier.fromNamespaceAndPath(PaintableBlocks.MOD_ID,path);}
    private static void blocks(RegisterBlockStateModels event){event.registerModel(id("design"),PaintableBlockModel.Unbaked.CODEC);}
    private static void items(RegisterItemModelsEvent event){event.register(id("design"),DesignItemModel.Unbaked.CODEC);event.register(id("paint_sample"),PaintSampleItemModel.Unbaked.CODEC);}
}
