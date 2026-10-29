package net.nicomar.paintableblocks.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.nicomar.paintableblocks.client.selection.VolumePreview;
import net.nicomar.paintableblocks.interaction.VolumeEditing;
import net.nicomar.paintableblocks.balance.PaintingBalance;

/** Configuration remains visible while the world preview is rendered behind this non-pausing screen. */
public final class SlopeFormerScreen extends Screen {
    private int direction,widthBlocks,heightBlocks,depthBlocks,resolution;private boolean upper,create;
    private Button confirm;
    public SlopeFormerScreen(){super(Component.translatable("item.paintableblocks.slope_former"));var s=VolumePreview.state();if(s!=null){var r=VolumePreview.region(s);direction=s.direction();widthBlocks=r.width();heightBlocks=r.height();depthBlocks=r.depth();resolution=s.resolution();upper=s.upper();create=s.create();}}
    private void configure(){VolumePreview.action(0,direction,upper,create,widthBlocks,heightBlocks,depthBlocks,resolution);}
    private Button button(String label,int y,Runnable action){return addRenderableWidget(Button.builder(Component.literal(label),b->{action.run();configure();rebuildWidgets();}).bounds(width/2-135,height/2+y,270,18).build());}
    @Override protected void init(){
        button(Component.translatable("button.paintableblocks.slope_direction").getString()+": "+new String[]{"+X","+Z","−X","−Z"}[direction],-91,()->direction=(direction+1)%4);
        button(Component.translatable("button.paintableblocks.slope_side").getString()+": "+Component.translatable(upper?"button.paintableblocks.upper":"button.paintableblocks.lower").getString(),-70,()->upper=!upper);
        if(minecraft.player!=null&&minecraft.player.isCreative()){
            button(Component.translatable(create?"button.paintableblocks.create":"button.paintableblocks.transform").getString(),-49,()->create=!create);
            if(create){button("X: "+widthBlocks+"  (+)",-28,()->widthBlocks=widthBlocks%16+1);button("Y: "+heightBlocks+"  (+)",-7,()->heightBlocks=heightBlocks%16+1);button("Z: "+depthBlocks+"  (+)",14,()->depthBlocks=depthBlocks%16+1);
                button(resolution+"×"+resolution,35,()->resolution=PaintingBalance.RESOLUTIONS.get((PaintingBalance.RESOLUTIONS.indexOf(resolution)+1)%5));}
        }
        addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.preview"),b->configure()).bounds(width/2-44,height/2+60,88,20).build());
        confirm=addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.confirm"),b->{VolumePreview.action(1,direction,upper,create,widthBlocks,heightBlocks,depthBlocks,resolution);minecraft.gui.setScreen(null);}).bounds(width/2-135,height/2+60,88,20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.cancel"),b->onClose()).bounds(width/2+47,height/2+60,88,20).build());
    }
    @Override public void tick(){var s=VolumePreview.state();if(s==null){minecraft.gui.setScreen(null);return;}confirm.active=s.phase()==VolumeEditing.READY&&s.direction()==direction&&s.upper()==upper&&s.create()==create&&s.resolution()==resolution;}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float tick){
        g.fill(width/2-145,height/2-126,width/2+145,height/2+123,0xbb222222);g.text(font,title,width/2-135,height/2-116,0xffffffff);
        var s=VolumePreview.state();if(s!=null){var r=VolumePreview.region(s);g.text(font,Component.literal(r.width()+"×"+r.height()+"×"+r.depth()+"; "+r.height()+" / "+(direction%2==0?r.width():r.depth())),width/2-135,height/2-103,0xffdddddd);
            g.text(font,Component.translatable("message.paintableblocks.volume_progress",s.reviewed(),r.size(),s.modified(),s.ignored()),width/2-135,height/2+86,0xffdddddd);
            g.text(font,Component.translatable("message.paintableblocks.volume_planned",s.affected()),width/2-135,height/2+98,0xffdddddd);}
        g.text(font,Component.translatable("message.paintableblocks.volume_warning_short"),width/2-135,height/2+111,0xffffcc66);super.extractRenderState(g,x,y,tick);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){VolumePreview.cancel();super.onClose();}
}
