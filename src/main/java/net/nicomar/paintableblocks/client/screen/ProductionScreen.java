package net.nicomar.paintableblocks.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.nicomar.paintableblocks.menu.ProductionMenu;

public abstract class ProductionScreen<T extends ProductionMenu> extends AbstractContainerScreen<T> {
    protected ProductionScreen(T menu,Inventory inventory,Component title,int height){super(menu,inventory,title,300,height);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float tick){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff343434);for(var slot:menu.slots)g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xff777777);}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,title,8,6,0xffffffff);}
}
