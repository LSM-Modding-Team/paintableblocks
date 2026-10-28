package net.nicomar.paintableblocks.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.nicomar.paintableblocks.PaintableBlocks;

/** Physical-client entrypoint; renderers and screens belong to later fronts. */
@Mod(value = PaintableBlocks.MOD_ID, dist = Dist.CLIENT)
public final class PaintableBlocksClient {
    public PaintableBlocksClient() {
    }
}
