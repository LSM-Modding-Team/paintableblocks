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
    private int yaw=45,pitch=30,viewMode;
    private record Settings(int direction,int resolution,int precision,int start,int end,int fullLength,int otherLength,boolean upper,boolean create,boolean vertical,boolean combined,boolean fullAtStart,boolean bothEnds,int[] size,int[] offset){}
    private static Settings saved;
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
    private void localChoice(String text,Runnable action){var b=addRenderableWidget(Button.builder(Component.literal(text),v->{action.run();rebuildWidgets();}).bounds(8,40+row*22-scroll,width/2-20,20).build());controls.put(b,row++);}
    private void number(String label,int value,int min,int max,java.util.function.IntConsumer setter){
        int w=width/2-20;control("−",8,22,()->setter.accept(Math.max(min,value-16/precision)));
        var center=addRenderableWidget(Button.builder(Component.literal(label+": "+measure(value)),v->minecraft.gui.setScreen(new SlopeNumberScreen(this,label,value,min,max,n->{setter.accept(n);configure();rebuildWidgets();}))).bounds(32,40+row*22-scroll,w-48,20).build());controls.put(center,row);
        control("+",8+w-22,22,()->setter.accept(Math.min(max,value+16/precision)));row++;
    }
    private void copy(){saved=new Settings(direction,resolution,precision,start,end,fullLength,otherLength,upper,create,vertical,combined,fullAtStart,bothEnds,dimensions.clone(),offsets.clone());}
    private void paste(){if(saved==null)return;var v=saved;direction=v.direction;resolution=v.resolution;precision=v.precision;start=v.start;end=v.end;fullLength=v.fullLength;otherLength=v.otherLength;upper=v.upper;vertical=v.vertical;combined=v.combined;fullAtStart=v.fullAtStart;bothEnds=v.bothEnds;if(minecraft.player.isCreative()){create=v.create;System.arraycopy(v.size,0,dimensions,0,3);System.arraycopy(v.offset,0,offsets,0,3);}if(!create){var state=VolumePreview.state();if(state!=null){var r=VolumePreview.region(state);dimensions[0]=r.x1()-r.x0();dimensions[1]=r.y1()-r.y0();dimensions[2]=r.z1()-r.z0();}}configure();}
    private void camera(){scene.view(viewMode,yaw,pitch);}
    @Override public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event,double dx,double dy){
        if(event.x()>width/2&&event.y()>52&&event.y()<height-114){yaw=Math.floorMod(yaw+(int)Math.round(dx),360);pitch=Math.clamp(pitch+(int)Math.round(dy),-90,90);camera();return true;}
        return super.mouseDragged(event,dx,dy);
    }
    @Override protected void init(){
        controls.clear();row=0;
        if(minecraft.player!=null&&minecraft.player.isCreative())choice(t(create?"create":"transform"),()->create=!create);
        choice(t("dimension_step")+": 1/"+precision,()->precision=PaintingBalance.RESOLUTIONS.get((PaintingBalance.RESOLUTIONS.indexOf(precision)+1)%5));
        choice(t(vertical?"vertical_slope":"horizontal_slope"),()->{vertical=!vertical;start=0;end=span();});
        choice(t("slope_direction")+": "+new String[]{"+X","+Z","−X","−Z"}[direction],()->direction=(direction+1)%4);
        choice(t(vertical?(upper?"positive_side":"negative_side"):(upper?"upper":"lower")),()->upper=!upper);
        number(t("start_level"),start,0,span(),n->start=n);
        number(t("end_level"),end,0,span(),n->end=n);
        choice(t(combined?"combined_on":"combined_off"),()->{combined=!combined;if(!combined)bothEnds=false;});
        if(combined){
            choice(t(bothEnds?"step_join":"continuous_join"),()->{bothEnds=!bothEnds;if(bothEnds){resolution=16;otherLength=0;start=0;end=span();}});
            choice(t(fullAtStart?"full_start":"full_end"),()->{fullAtStart=!fullAtStart;if(!bothEnds){if(fullAtStart)end=start;else start=end;}});
            number(t(fullAtStart?"full_start":"full_end"),fullLength,0,run()-(bothEnds?otherLength:0),n->fullLength=n);
            if(bothEnds){
                number(t(fullAtStart?"full_end":"full_start"),otherLength,0,run()-fullLength,n->otherLength=n);
                number(t("incline_length"),run()-fullLength-otherLength,0,run()-fullLength,n->otherLength=run()-fullLength-n);
                choice(t("move_incline_left"),()->{int step=Math.min(16/precision,fullAtStart?fullLength:otherLength);if(fullAtStart){fullLength-=step;otherLength+=step;}else{otherLength-=step;fullLength+=step;}});
                choice(t("move_incline_right"),()->{int step=Math.min(16/precision,fullAtStart?otherLength:fullLength);if(fullAtStart){otherLength-=step;fullLength+=step;}else{fullLength-=step;otherLength+=step;}});
            }
        }
        if(create){
            choice(t("edit_grid")+": "+resolution+"×"+resolution,()->{if(!bothEnds)resolution=PaintingBalance.RESOLUTIONS.get((PaintingBalance.RESOLUTIONS.indexOf(resolution)+1)%5);});
            for(int i=0;i<3;i++){final int a=i;
                number(new String[]{"X","Y","Z"}[i],dimensions[i],1,256,n->dimensions[a]=n);
                number("Δ"+new String[]{"X","Y","Z"}[i],offsets[i],0,15,n->offsets[a]=n);
            }
        }
        localChoice(t("copy_settings"),this::copy);localChoice(t("paste_settings"),this::paste);
        localChoice(t("undo"),()->VolumePreview.action(3,direction,upper,create,1,1,1,resolution));
        localChoice(t("view_mode")+": "+t(new String[]{"after","before","removed"}[viewMode]),()->{viewMode=(viewMode+1)%3;camera();});
        localChoice(t("front_view"),()->{yaw=0;pitch=0;camera();});
        localChoice(t("side_view"),()->{yaw=90;pitch=0;camera();});
        localChoice(t("top_view"),()->{yaw=0;pitch=90;camera();});
        localChoice(t("isometric_view"),()->{yaw=45;pitch=30;camera();});
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
        int left=width/2+6,right=width-8,top=52,bottom=height-114;
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
        g.text(font,Component.literal(measure(fullAtStart?fullLength:otherLength)+" | "+measure(run()-fullLength-(bothEnds?otherLength:0))+" | "+measure(fullAtStart?otherLength:fullLength)+" · "+measure(start)+" → "+measure(end)),width/2+6,height-102,0xffdddddd);
        var notice=VolumePreview.notice();
        String problem=notice==null?scene.error():Component.translatable("message.paintableblocks."+notice.reason()).getString()+" "+notice.pos().toShortString();
        if(!problem.isEmpty())g.text(font,Component.literal(problem),width/2+6,height-58,0xffff7777);
        else g.text(font,Component.literal(measure(dimensions[0])+" × "+measure(dimensions[1])+" × "+measure(dimensions[2])+" | "+measure(start)+" → "+measure(end)),width/2+6,height-58,0xffdddddd);
        if(row*22>height-80){int track=height-80;int thumb=Math.max(12,track*track/(row*22));int y0=40+scroll*(track-thumb)/Math.max(1,row*22-track);g.fill(width/2-7,y0,width/2-4,y0+thumb,0xff9aaabb);}
        super.extractRenderState(g,x,y,tick);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){VolumePreview.cancel();super.onClose();}
}
