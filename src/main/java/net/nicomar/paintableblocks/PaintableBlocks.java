package net.nicomar.paintableblocks;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.nicomar.paintableblocks.registry.ModRegistries;
import net.nicomar.paintableblocks.registry.ModContent;

/** Common entrypoint. Group 1 content is attached before registry events. */
@Mod(PaintableBlocks.MOD_ID)
public final class PaintableBlocks {
    public static final String MOD_ID = "paintableblocks";

    public PaintableBlocks(IEventBus modBus) {
        ModContent.initialize(modBus);
        ModRegistries.register(modBus);
    }
}
