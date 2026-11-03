package net.nicomar.paintableblocks.client.screen;

import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.nicomar.paintableblocks.client.selection.VolumePreview;
import net.nicomar.paintableblocks.interaction.VolumeEditing;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.design.*;

public final class SlopeFormerScreen extends Screen {
    private int direction,resolution=1,precision=16,start,end=16,fullLength,otherLength,scroll,row;
    private final int[] dimensions={16,16,16},offsets={0,0,0};
    private boolean upper,create,vertical,combined,fullAtStart,bothEnds;
    private final Map<Button,Integer> controls=new LinkedHashMap<>();
    private Button confirm;
    private final SlopeScenePreview scene=new SlopeScenePreview();
    private float zoom=1;
    public SlopeFormerScreen(){
        super(Component.translatable("item.paintableblocks.slope_former"));
        var s=VolumePreview.state();if(s!=null){var r=VolumePreview.region(s);direction=s.direction();resolution=s.resolution();upper=s.upper();create=s.create();vertical=s.vertical();combined=s.combined();fullAtStart=s.fullAtStart();bothEnds=s.bothEnds();fullLength=s.fullLength();otherLength=s.otherLength();start=s.start();
            dimensions[0]=r.x1()-r.x0();dimensions[1]=r.y1()-r.y0();dimensions[2]=r.z1()-r.z0();end=s.end()<0?span():s.end();
            offsets[0]=Math.floorMod(r.x0(),16);offsets[1]=Math.floorMod(r.y0(),16);offsets[2]=Math.floorMod(r.z0(),16);}
    }
    private int span(){return dimensions[vertical?(direction%2==0?2:0):1];}
    private int run(){return dimensions[direction%2==0?0:2];}
    private String t(String key){return Component.translatable("button.paintableblocks."+key).getString();}
    private String measure(int v){return Double.toString(v/16.0);}
    private void configure(){
        start=Math.min(start,span());end=Math.min(end,span());fullLength=Math.min(fullLength,run());otherLength=Math.min(otherLength,run()-fullLength);
        if(bothEnds&&create)resolution=16;
        VolumePreview.configure(direction,upper,create,dimensions[0],dimensions[1],dimensions[2],resolution,offsets[0],offsets[1],offsets[2],vertical,start,end,combined,fullLength,fullAtStart,bothEnds,otherLength);
    }
    private void control(String text,int x,int w,Runnable action){
        var b=addRenderableWidget(Button.builder(Component.literal(text),v->{action.run();configure();rebuildWidgets();}).bounds(x,40+row*22-scroll,w,20).build());controls.put(b,row);
    }
    private void choice(String text,Runnable action){control(text,8,width/2-20,action);row++;}
    private void number(String text,Runnable minus,Runnable plus){
        int w=width/2-20;control("−",8,22,minus);control(text,32,w-48,()->{});control("+",8+w-22,22,plus);row++;
    }
    @Override protected void init(){
        controls.clear();row=0;
        if(minecraft.player!=null&&minecraft.player.isCreative())choice(t(create?"create":"transform"),()->create=!create);
        choice(t("dimension_step")+": 1/"+precision,()->precision=PaintingBalance.RESOLUTIONS.get((PaintingBalance.RESOLUTIONS.indexOf(precision)+1)%5));
        choice(t(vertical?"vertical_slope":"horizontal_slope"),()->{vertical=!vertical;start=0;end=span();});
        choice(t("slope_direction")+": "+new String[]{"+X","+Z","−X","−Z"}[direction],()->direction=(direction+1)%4);
        choice(t(vertical?(upper?"positive_side":"negative_side"):(upper?"upper":"lower")),()->upper=!upper);
        number(t("start_level")+": "+measure(start),()->start=Math.max(0,start-16/precision),()->start=Math.min(span(),start+16/precision));
        number(t("end_level")+": "+measure(end),()->end=Math.max(0,end-16/precision),()->end=Math.min(span(),end+16/precision));
        choice(t(combined?"combined_on":"combined_off"),()->{combined=!combined;if(!combined)bothEnds=false;});
        if(combined){
            choice(t(bothEnds?"both_ends":"one_end"),()->{bothEnds=!bothEnds;if(bothEnds){resolution=16;fullLength=Math.min(fullLength,run()/3);otherLength=Math.min(4,run()-fullLength);}});
            choice(t(fullAtStart?"full_start":"full_end"),()->fullAtStart=!fullAtStart);
            number(t("full_length")+": "+measure(fullLength),()->fullLength=Math.max(0,fullLength-16/precision),()->fullLength=Math.min(run()-(bothEnds?otherLength:0),fullLength+16/precision));
            if(bothEnds)number(t("other_length")+": "+measure(otherLength),()->otherLength=Math.max(0,otherLength-16/precision),()->otherLength=Math.min(run()-fullLength,otherLength+16/precision));
        }
        if(create){
            choice(t("edit_grid")+": "+resolution+"×"+resolution,()->{if(!bothEnds)resolution=PaintingBalance.RESOLUTIONS.get((PaintingBalance.RESOLUTIONS.indexOf(resolution)+1)%5);});
            for(int i=0;i<3;i++){final int a=i;
                number(new String[]{"X","Y","Z"}[i]+": "+measure(dimensions[i]),()->dimensions[a]=Math.max(1,dimensions[a]-16/precision),()->dimensions[a]=Math.min(256,dimensions[a]+16/precision));
                number("Δ"+new String[]{"X","Y","Z"}[i]+": "+measure(offsets[i]),()->offsets[a]=Math.max(0,offsets[a]-16/precision),()->offsets[a]=Math.min(15,offsets[a]+16/precision));
            }
        }
        confirm=addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.confirm"),b->{VolumePreview.action(1,direction,upper,create,1,1,1,resolution);minecraft.gui.setScreen(null);}).bounds(8,height-28,Math.max(80,width/3-12),20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.preview"),b->configure()).bounds(width/3+4,height-28,Math.max(70,width/3-12),20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.cancel"),b->onClose()).bounds(2*width/3,height-28,Math.max(70,width/3-8),20).build());
        position();
    }
    private void position(){
        scroll=Math.clamp(scroll,0,Math.max(0,row*22-(height-80)));
        controls.forEach((b,r)->{int y=40+r*22-scroll;b.setY(y);b.visible=y>=40&&y+20<=height-40;});
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double verticalScroll){
        if(x<width/2){scroll-=(int)Math.round(verticalScroll*24);position();return true;}zoom=Math.clamp(zoom+(float)verticalScroll*.1F,.5F,2F);return true;
    }
    @Override public void tick(){
        scene.tick();
        var s=VolumePreview.state();if(s==null){minecraft.gui.setScreen(null);return;}
        var r=VolumePreview.region(s);
        confirm.active=s.phase()==VolumeEditing.READY&&s.direction()==direction&&s.upper()==upper&&s.create()==create&&s.resolution()==resolution&&s.vertical()==vertical&&s.combined()==combined&&s.fullLength()==fullLength&&s.fullAtStart()==fullAtStart&&s.bothEnds()==bothEnds&&s.otherLength()==otherLength&&s.start()==start&&(s.end()<0?span():s.end())==end&&(!create||r.x1()-r.x0()==dimensions[0]&&r.y1()-r.y0()==dimensions[1]&&r.z1()-r.z0()==dimensions[2]&&Math.floorMod(r.x0(),16)==offsets[0]&&Math.floorMod(r.y0(),16)==offsets[1]&&Math.floorMod(r.z0(),16)==offsets[2]);
    }
    private void preview(GuiGraphicsExtractor g){
        int left=width/2+6,right=width-8,top=52,bottom=height-90;
        g.fill(left,top,right,bottom,0xff101923);
        if(bottom<=top||scene.stack().isEmpty())return;
        float pixels=Math.min(right-left,bottom-top)*.85F*zoom;
        g.enableScissor(left,top,right,bottom);
        g.pose().pushMatrix();
        try{
            g.pose().translate((left+right)/2F-pixels/2,(top+bottom)/2F-pixels/2);
            g.pose().scale(pixels/16,pixels/16);
            g.item(scene.stack(),0,0);
        }finally{g.pose().popMatrix();g.disableScissor();}
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float tick){
        g.fill(0,0,width,height,0xf01a2029);g.text(font,title,8,12,0xffffffff);
        g.text(font,Component.translatable("button.paintableblocks.blocks_preview"),width/2+6,36,0xffffffff);preview(g);
        var s=VolumePreview.state();if(s!=null){g.text(font,Component.translatable(s.phase()==VolumeEditing.READY?"message.paintableblocks.volume_ready":"message.paintableblocks.preview_loading",s.affected(),s.ignored()),width/2+6,height-76,0xffffffff);}
        if(scene.pending()||scene.limited())g.text(font,Component.translatable(scene.limited()?"message.paintableblocks.preview_simplified":"message.paintableblocks.preview_loading"),width/2+6,height-88,0xffffcc66);
        if(bothEnds)g.text(font,Component.translatable("button.paintableblocks.grid_requirement"),width/2+6,height-58,0xffffcc66);
        if(row*22>height-80){int track=height-80;int thumb=Math.max(12,track*track/(row*22));int y0=40+scroll*(track-thumb)/Math.max(1,row*22-track);g.fill(width/2-7,y0,width/2-4,y0+thumb,0xff9aaabb);}
        super.extractRenderState(g,x,y,tick);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){VolumePreview.cancel();super.onClose();}
}
