package net.nicomar.paintableblocks.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.registry.ModContent;

/** Recipe-only wear, stored once. No vanilla damage or repair/enchantment properties. */
public final class ScraperItem extends Item {
    private final PaintingBalance.Material material;
    public ScraperItem(PaintingBalance.Material material,Properties properties){super(properties);this.material=material;}
    public PaintingBalance.Material material(){return material;}
    public int uses(ItemStack stack){return stack.getOrDefault(ModContent.SCRAPER_USES.get(),-1);}
    public boolean valid(ItemStack stack,int cost){
        var enchantments=stack.get(DataComponents.ENCHANTMENTS);int uses=uses(stack);
        return uses>=cost&&uses<=material.scraperUses&&(enchantments==null||enchantments.isEmpty());
    }
    @Override public boolean isBarVisible(ItemStack stack){return uses(stack)>=0&&uses(stack)<material.scraperUses;}
    @Override public int getBarWidth(ItemStack stack){return Math.clamp(Math.round(13.0F*Math.max(0,uses(stack))/material.scraperUses),0,13);}
    @Override public int getBarColor(ItemStack stack){return 0x55CC77;}
    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,net.minecraft.world.item.Item.TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<net.minecraft.network.chat.Component> lines,net.minecraft.world.item.TooltipFlag flag){
        super.appendHoverText(stack,context,display,lines,flag);
        lines.accept(net.minecraft.network.chat.Component.translatable("tooltip.paintableblocks.scraper",Math.max(0,uses(stack)),material.scraperUses));
    }
}
