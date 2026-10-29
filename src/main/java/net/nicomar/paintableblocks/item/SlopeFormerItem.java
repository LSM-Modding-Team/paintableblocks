package net.nicomar.paintableblocks.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Configuration and all mutations are server-authoritative through VolumeEditing. */
public final class SlopeFormerItem extends Item {
    public SlopeFormerItem(Properties properties){super(properties);}
    @Override public InteractionResult useOn(UseOnContext context){return context.getPlayer()==null?InteractionResult.PASS:InteractionResult.SUCCESS;}
}
