package net.nicomar.paintableblocks.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Configuration and all mutations are server-authoritative through VolumeEditing. */
public final class SlopeFormerItem extends Item {
    public SlopeFormerItem(Properties properties){super(properties);}
    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,Item.TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<net.minecraft.network.chat.Component> lines,net.minecraft.world.item.TooltipFlag flag){super.appendHoverText(stack,context,display,lines,flag);lines.accept(net.minecraft.network.chat.Component.translatable("tooltip.paintableblocks.undo"));}
    @Override public InteractionResult useOn(UseOnContext context){return context.getPlayer()==null?InteractionResult.PASS:InteractionResult.SUCCESS;}
}
