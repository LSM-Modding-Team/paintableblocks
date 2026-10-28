package net.nicomar.paintableblocks.client.selection;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.registry.ModContent;

/** Conservation feedback; the native outline uses the block's exact remaining shape. */
public final class SelectionPreview {
    private static int refresh;
    private static BlockPos lastPosition;
    private static boolean lastGloves;
    private SelectionPreview() {}
    public static void register() { NeoForge.EVENT_BUS.addListener(SelectionPreview::tick); }
    private static void tick(ClientTickEvent.Post event) {
        var client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.gui.screen() != null
                || client.player.isSpectator() || !(client.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) {
            lastPosition = null;
            return;
        }
        var held = client.player.getMainHandItem();
        boolean gloves = held.is(ModContent.GLOVES.get()), tongs = held.is(ModContent.TONGS.get());
        if ((!gloves && !tongs) || !(client.level.getBlockEntity(hit.getBlockPos()) instanceof PaintableBlockEntity block)) {
            lastPosition = null;
            return;
        }
        if (!hit.getBlockPos().equals(lastPosition) || lastGloves != gloves || ++refresh % 10 == 0) {
            lastPosition = hit.getBlockPos().immutable();
            lastGloves = gloves;
            String key = !block.valid() ? "message.paintableblocks.invalid_design"
                    : client.player.isCreative() ? "message.paintableblocks.creative_copy"
                    : gloves ? "message.paintableblocks.keep_design" : "message.paintableblocks.keep_shape";
            client.gui.hud.setOverlayMessage(Component.translatable(key), false);
        }
    }
}
