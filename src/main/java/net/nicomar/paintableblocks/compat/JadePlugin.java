package net.nicomar.paintableblocks.compat;

import snownee.jade.api.*;
import net.minecraft.world.phys.BlockHitResult;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.design.DesignGeometry;
import net.nicomar.paintableblocks.paint.PaintCatalog;

/** Optional client presentation only; no camouflage or block state is written to the world. */
@WailaPlugin
public final class JadePlugin implements IWailaPlugin {
    @Override public void registerClient(IWailaClientRegistration registration){
        registration.addRayTraceCallback((hit,accessor,original)->{
            if(!(hit instanceof BlockHitResult blockHit)||!(accessor instanceof BlockAccessor block)||!(block.getBlockEntity() instanceof PaintableBlockEntity be)||!be.valid())return null;
            var pos=blockHit.getBlockPos();var surface=DesignGeometry.target(be.design(),blockHit.getLocation().subtract(pos.getX(),pos.getY(),pos.getZ()),blockHit.getDirection());
            if(surface.isEmpty())return null;int paint=be.design().paint(surface.get());if(!PaintCatalog.validId(paint))return null;
            var state=PaintCatalog.byId(paint).sampleState();
            return registration.blockAccessor().from(block).blockState(state).serversideRep(new net.minecraft.world.item.ItemStack(state.getBlock())).blockEntity(()->null).serverData(null).serverConnected(false).requireVerification(false).build();
        });
    }
}
