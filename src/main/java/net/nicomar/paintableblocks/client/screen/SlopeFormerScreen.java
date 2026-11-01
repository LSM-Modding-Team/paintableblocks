package net.nicomar.paintableblocks.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.nicomar.paintableblocks.client.selection.VolumePreview;
import net.nicomar.paintableblocks.interaction.VolumeEditing;
import net.nicomar.paintableblocks.balance.PaintingBalance;

/** Dimensions and origin use sixteenths; editing subdivision remains independent. */
public final class SlopeFormerScreen extends Screen {
    private int direction,resolution=1,precision=16;
    private final int[] dimensions={16,16,16},offsets={0,0,0};
    private boolean upper,create;
    private Button confirm;
    public SlopeFormerScreen(){
        super(Component.translatable("item.paintableblocks.slope_former"));
        var s=VolumePreview.state();if(s!=null){var r=VolumePreview.region(s);direction=s.direction();resolution=s.resolution();upper=s.upper();create=s.create();
            dimensions[0]=Math.clamp(r.x1()-r.x0(),1,256);dimensions[1]=Math.clamp(r.y1()-r.y0(),1,256);dimensions[2]=Math.clamp(r.z1()-r.z0(),1,256);
            offsets[0]=Math.floorMod(r.x0(),16);offsets[1]=Math.floorMod(r.y0(),16);offsets[2]=Math.floorMod(r.z0(),16);}
    }
    private void configure(){VolumePreview.configure(direction,upper,create,dimensions[0],dimensions[1],dimensions[2],resolution,offsets[0],offsets[1],offsets[2]);}
    private void button(String text,int x,int y,int w,Runnable action){addRenderableWidget(Button.builder(Component.literal(text),b->{action.run();configure();rebuildWidgets();}).bounds(width/2+x,height/2+y,w,18).build());}
    private static String measure(int units){return Double.toString(units/16.0);}
    private void adjust(int[] values,int axis,int delta,int min,int max){values[axis]=Math.clamp(values[axis]+delta*(16/precision),min,max);}
    @Override protected void init(){
        button(Component.translatable("button.paintableblocks.slope_direction").getString()+": "+new String[]{"+X","+Z","−X","−Z"}[direction],-135,-115,270,()->direction=(direction+1)%4);
        button(Component.translatable(upper?"button.paintableblocks.upper":"button.paintableblocks.lower").getString(),-135,-94,270,()->upper=!upper);
        if(minecraft.player!=null&&minecraft.player.isCreative()){
            button(Component.translatable(create?"button.paintableblocks.create":"button.paintableblocks.transform").getString(),-135,-73,270,()->create=!create);
            if(create){
                button(Component.translatable("button.paintableblocks.dimension_step").getString()+": 1/"+precision,-135,-52,132,()->precision=PaintingBalance.RESOLUTIONS.get((PaintingBalance.RESOLUTIONS.indexOf(precision)+1)%5));
                button(Component.translatable("button.paintableblocks.edit_grid").getString()+": "+resolution+"×"+resolution,3,-52,132,()->resolution=PaintingBalance.RESOLUTIONS.get((PaintingBalance.RESOLUTIONS.indexOf(resolution)+1)%5));
                for(int axis=0;axis<3;axis++){
                    final int a=axis;int y=-31+axis*21;
                    button("−",-135,y,22,()->adjust(dimensions,a,-1,1,256));
                    button(new String[]{"X: ","Y: ","Z: "}[a]+measure(dimensions[a]),-112,y,86,()->{});
                    button("+",-25,y,22,()->adjust(dimensions,a,1,1,256));
                    button("−",3,y,22,()->adjust(offsets,a,-1,0,15));
                    button("Δ"+new String[]{"X: ","Y: ","Z: "}[a]+measure(offsets[a]),26,y,86,()->{});
                    button("+",113,y,22,()->adjust(offsets,a,1,0,15));
                }
            }
        }
        button(Component.translatable("button.paintableblocks.preview").getString(),-44,60,88,this::configure);
        confirm=addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.confirm"),b->{VolumePreview.action(1,direction,upper,create,1,1,1,resolution);minecraft.gui.setScreen(null);}).bounds(width/2-135,height/2+60,88,20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.cancel"),b->onClose()).bounds(width/2+47,height/2+60,88,20).build());
    }
    @Override public void tick(){
        var s=VolumePreview.state();if(s==null){minecraft.gui.setScreen(null);return;}
        var r=VolumePreview.region(s);
        confirm.active=s.phase()==VolumeEditing.READY&&s.direction()==direction&&s.upper()==upper&&s.create()==create&&s.resolution()==resolution&&(!create||
            r.x1()-r.x0()==dimensions[0]&&r.y1()-r.y0()==dimensions[1]&&r.z1()-r.z0()==dimensions[2]&&Math.floorMod(r.x0(),16)==offsets[0]&&Math.floorMod(r.y0(),16)==offsets[1]&&Math.floorMod(r.z0(),16)==offsets[2]);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float tick){
        g.fill(width/2-145,height/2-144,width/2+145,height/2+123,0xbb222222);g.text(font,title,width/2-135,height/2-136,0xffffffff);
        var s=VolumePreview.state();if(s!=null){var r=VolumePreview.region(s);
            g.text(font,Component.literal(measure(r.x1()-r.x0())+" × "+measure(r.y1()-r.y0())+" × "+measure(r.z1()-r.z0())),width/2-135,height/2+39,0xffdddddd);
            g.text(font,Component.translatable("message.paintableblocks.volume_progress",s.reviewed(),r.size(),s.modified(),s.ignored()),width/2-135,height/2+86,0xffdddddd);
            g.text(font,Component.translatable("message.paintableblocks.volume_planned",s.affected()),width/2-135,height/2+98,0xffdddddd);}
        super.extractRenderState(g,x,y,tick);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){VolumePreview.cancel();super.onClose();}
}
