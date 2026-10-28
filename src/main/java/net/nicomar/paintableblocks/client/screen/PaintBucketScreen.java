package net.nicomar.paintableblocks.client.screen;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.nicomar.paintableblocks.menu.PaintBucketMenu;
import net.nicomar.paintableblocks.paint.PaintCatalog;

/** Native widgets and solid fills, without creating any tool model or texture. */
public final class PaintBucketScreen extends AbstractContainerScreen<PaintBucketMenu> {
    private final List<Button> paints=new ArrayList<>();
    public PaintBucketScreen(PaintBucketMenu menu,Inventory inventory,Component title){super(menu,inventory,title,300,218);}
    private void send(int id){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    @Override protected void init(){
        super.init();paints.clear();
        for(int slot=0;slot<8;slot++){final int index=slot;paints.add(addRenderableWidget(Button.builder(Component.empty(),b->send(index)).bounds(leftPos+8+(slot/4)*144,topPos+18+(slot%4)*20,140,19).build()));}
        addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.deposit"),b->send(8)).bounds(leftPos+8,topPos+98,284,18).build());
        addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.withdraw_one"),b->send(9)).bounds(leftPos+8,topPos+116,140,18).build());
        addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.withdraw_stack"),b->send(10)).bounds(leftPos+152,topPos+116,140,18).build());
        labels();
    }
    private void labels(){for(int i=0;i<paints.size();i++){int id=menu.paint(i);paints.get(i).active=id>0;var name=PaintCatalog.validId(id)?Component.translatable(PaintCatalog.byId(id).translationKey()):Component.literal("—");paints.get(i).setMessage(Component.literal(id==menu.selected()&&id>0?"> ":"").append(name).append(" · "+menu.units(i)));}}
    @Override protected void containerTick(){super.containerTick();labels();}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff343434);for(var slot:menu.slots)g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xff777777);}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){g.text(font,title,8,6,0xffffffff);}
}
