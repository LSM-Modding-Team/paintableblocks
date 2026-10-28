package net.nicomar.paintableblocks.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.menu.PaintMachineMenu;
import net.nicomar.paintableblocks.paint.PaintCatalog;

public final class PaintMachineScreen extends ProductionScreen<PaintMachineMenu> {
    public PaintMachineScreen(PaintMachineMenu menu,Inventory inv,Component title){super(menu,inv,title,218);}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){super.extractLabels(g,x,y);
        g.fill(30,52,270,64,0xff777777);g.fill(30,52,30+240*menu.progress()/PaintingBalance.MACHINE_TICKS,64,0xff55bb88);
        g.text(font,Component.translatable("label.paintableblocks.processing",menu.progress(),PaintingBalance.MACHINE_TICKS,menu.yield()),8,76,0xffffffff);
        if(PaintCatalog.validId(menu.paint()))g.text(font,Component.translatable(PaintCatalog.byId(menu.paint()).translationKey()),8,92,0xffffffff);
        g.text(font,Component.translatable("label.paintableblocks.machine_wait"),8,110,0xffffffff);
    }
}
