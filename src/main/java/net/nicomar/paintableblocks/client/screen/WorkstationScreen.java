package net.nicomar.paintableblocks.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.menu.WorkstationMenu;
import net.nicomar.paintableblocks.production.ProductionInputs;
import net.nicomar.paintableblocks.network.ModNetworking;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class WorkstationScreen extends ProductionScreen<WorkstationMenu> {
    private final java.util.List<Button> resolutions=new java.util.ArrayList<>();
    private Button craft;
    public WorkstationScreen(WorkstationMenu menu,Inventory inv,Component title){super(menu,inv,title,232);}
    private void send(int action,int variant){if(minecraft.getConnection()!=null)ClientPacketDistributor.sendToServer(new ModNetworking.ProductionAction(menu.containerId,action,variant,menu.resolution(),ProductionInputs.material(menu.slots.get(1).getItem())==null?-1:ProductionInputs.material(menu.slots.get(1).getItem()).ordinal()));}
    private Button button(Component text,int x,int y,int width,Runnable action){return addRenderableWidget(Button.builder(text,b->action.run()).bounds(leftPos+x,topPos+y,width,15).build());}
    @Override protected void init(){
        super.init();resolutions.clear();
        for(int n:PaintingBalance.RESOLUTIONS)resolutions.add(button(Component.literal(n+"×"+n),84+PaintingBalance.RESOLUTIONS.indexOf(n)*40,18,39,()->send(0,n)));
        craft=button(Component.translatable("button.paintableblocks.manufacture"),8,38,140,()->send(1,0));craft.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("tooltip.paintableblocks.station_inputs")));
        var convert=button(Component.translatable("button.paintableblocks.convert_item"),152,38,140,()->send(2,0));convert.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("tooltip.paintableblocks.station_convert")));
        for(int i=0;i<5;i++){final int index=i;int y=58+i*17;button(Component.literal("1"),112,y,30,()->send(4,index));button(Component.literal("64"),144,y,30,()->send(5,index));button(Component.literal("→1"),178,y,45,()->send(6,index));button(Component.literal("→64"),225,y,67,()->send(7,index));}
    }
    @Override protected void containerTick(){super.containerTick();for(int i=0;i<resolutions.size();i++){int n=PaintingBalance.RESOLUTIONS.get(i);resolutions.get(i).setMessage(Component.literal((menu.resolution()==n?"> ":"")+n+"×"+n));}
        if(craft!=null){var material=ProductionInputs.material(menu.slots.get(1).getItem());craft.setMessage(Component.translatable("button.paintableblocks.manufacture_count",material==null?0:menu.tier().yield(material)));}}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){super.extractLabels(g,x,y);for(int i=0;i<5;i++){int n=PaintingBalance.RESOLUTIONS.get(i);g.text(font,Component.literal(n+"×"+n+": "+menu.count(i)),8,61+i*17,0xffffffff);}}
}
