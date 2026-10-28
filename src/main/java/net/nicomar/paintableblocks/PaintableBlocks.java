package net.nicomar.paintableblocks;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.nicomar.paintableblocks.registry.ModRegistries;

/** Common entrypoint. Gameplay entries are added by their own development fronts. */
@Mod(PaintableBlocks.MOD_ID)
public final class PaintableBlocks {
    public static final String MOD_ID = "paintableblocks";

    public PaintableBlocks(IEventBus modBus) {
        ModRegistries.register(modBus);
    }
}
