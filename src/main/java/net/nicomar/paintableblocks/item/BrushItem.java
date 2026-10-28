package net.nicomar.paintableblocks.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.paint.PaintContent;
import net.nicomar.paintableblocks.registry.ModContent;
import net.nicomar.paintableblocks.interaction.ServerInteractions;

public final class BrushItem extends Item {
    private final PaintingBalance.Material material;
    public BrushItem(PaintingBalance.Material material,Properties properties){super(properties);this.material=material;}
    public PaintingBalance.Material material(){return material;}
    public PaintContent content(ItemStack stack){return stack.get(ModContent.BRUSH_PAINT.get());}
    public boolean valid(ItemStack stack){var content=content(stack);return content!=null&&content.fits(material.brushCapacity);}
    @Override public InteractionResult useOn(UseOnContext context){
        if(context.getPlayer()==null)return InteractionResult.PASS;
        if(!context.getLevel().isClientSide())ServerInteractions.use(context.getPlayer(),false,context.isSecondaryUseActive(),0);
        return InteractionResult.SUCCESS;
    }
    @Override public boolean overrideOtherStackedOnMe(ItemStack self,ItemStack other,Slot slot,ClickAction action,Player player,SlotAccess access){
        if(!player.isCreative()||action!=ClickAction.SECONDARY||!other.is(ModContent.PAINT.get())||!valid(self))return false;
        Integer id=other.get(ModContent.PAINT_ID.get());if(id==null)return false;
        PaintContent old=content(self);if(old.units()>0&&old.paint()!=id)return true;
        self.set(ModContent.BRUSH_PAINT.get(),new PaintContent(id,old.units()));return true;
    }
    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,net.minecraft.world.item.Item.TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<net.minecraft.network.chat.Component> lines,net.minecraft.world.item.TooltipFlag flag){
        super.appendHoverText(stack,context,display,lines,flag);
        var c=content(stack);if(c==null||!valid(stack)){lines.accept(net.minecraft.network.chat.Component.translatable("message.paintableblocks.invalid_design"));return;}
        if(c.paint()>0)lines.accept(net.minecraft.network.chat.Component.translatable(net.nicomar.paintableblocks.paint.PaintCatalog.byId(c.paint()).translationKey()));
        lines.accept(net.minecraft.network.chat.Component.translatable("tooltip.paintableblocks.brush",c.units(),material.brushCapacity));
    }
}
