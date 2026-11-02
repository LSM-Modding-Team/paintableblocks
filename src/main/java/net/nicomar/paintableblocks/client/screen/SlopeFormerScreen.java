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
    private boolean upper,create,vertical,combined,fullAtStart;private int page,fullLength;private int start,end=16;
    private Button confirm;
    public SlopeFormerScreen(){
        super(Component.translatable("item.paintableblocks.slope_former"));
        var s=VolumePreview.state();if(s!=null){var r=VolumePreview.region(s);direction=s.direction();resolution=s.resolution();upper=s.upper();create=s.create();vertical=s.vertical();combined=s.combined();fullLength=s.fullLength();fullAtStart=s.fullAtStart();start=s.start();end=s.end()<0?(vertical?(direction%2==0?r.z1()-r.z0():r.x1()-r.x0()):r.y1()-r.y0()):s.end();
            dimensions[0]=Math.clamp(r.x1()-r.x0(),1,256);dimensions[1]=Math.clamp(r.y1()-r.y0(),1,256);dimensions[2]=Math.clamp(r.z1()-r.z0(),1,256);
            offsets[0]=Math.floorMod(r.x0(),16);offsets[1]=Math.floorMod(r.y0(),16);offsets[2]=Math.floorMod(r.z0(),16);}
    }
    private int span(){return dimensions[vertical?(direction%2==0?2:0):1];}
    private int run(){return dimensions[direction%2==0?0:2];}
    private void configure(){fullLength=Math.min(fullLength,run());start=Math.min(start,span());end=Math.min(end,span());VolumePreview.configure(direction,upper,create,dimensions[0],dimensions[1],dimensions[2],resolution,offsets[0],offsets[1],offsets[2],vertical,start,end,combined,fullLength,fullAtStart);}
    private void button(String text,int x,int y,int w,Runnable action){addRenderableWidget(Button.builder(Component.literal(text),b->{action.run();configure();rebuildWidgets();}).bounds(width/2+x,height/2+y,w,18).build());}
    private static String measure(int units){return Double.toString(units/16.0);}
    private void adjust(int[] values,int axis,int delta,int min,int max){values[axis]=Math.clamp(values[axis]+delta*(16/precision),min,max);}
    @Override protected void init(){
        button(Component.translatable(page==0?"button.paintableblocks.profile_page":page==1?"button.paintableblocks.combination_page":"button.paintableblocks.dimensions_page").getString(),-135,-91,270,()->page=(page+1)%3);
        if(page==0){
            button(Component.translatable("button.paintableblocks.dimension_step").getString()+": 1/"+precision,-135,-70,270,()->precision=PaintingBalance.RESOLUTIONS.get((PaintingBalance.RESOLUTIONS.indexOf(precision)+1)%5));
            button(Component.translatable(vertical?"button.paintableblocks.vertical_slope":"button.paintableblocks.horizontal_slope").getString(),-135,-49,270,()->{vertical=!vertical;start=0;end=span();});
            button(Component.translatable("button.paintableblocks.start_level").getString()+": "+measure(start),-112,-28,224,()->{});
            button("−",-135,-28,22,()->start=Math.max(0,start-16/precision));button("+",113,-28,22,()->start=Math.min(span(),start+16/precision));
            button(Component.translatable("button.paintableblocks.end_level").getString()+": "+measure(end),-112,-7,224,()->{});
            button("−",-135,-7,22,()->end=Math.max(0,end-16/precision));button("+",113,-7,22,()->end=Math.min(span(),end+16/precision));
            button(Component.translatable("button.paintableblocks.slope_direction").getString()+": "+new String[]{"+X","+Z","−X","−Z"}[direction],-135,14,270,()->direction=(direction+1)%4);
            button(Component.translatable(vertical?(upper?"button.paintableblocks.positive_side":"button.paintableblocks.negative_side"):(upper?"button.paintableblocks.upper":"button.paintableblocks.lower")).getString(),-135,35,270,()->upper=!upper);
        }else if(page==1){
            button(Component.translatable(combined?"button.paintableblocks.combined_on":"button.paintableblocks.combined_off").getString(),-135,-70,270,()->{combined=!combined;if(combined&&fullLength==0)fullLength=run()/2;});
            button(Component.translatable("button.paintableblocks.dimension_step").getString()+": 1/"+precision,-135,-49,270,()->precision=PaintingBalance.RESOLUTIONS.get((PaintingBalance.RESOLUTIONS.indexOf(precision)+1)%5));
            button(Component.translatable("button.paintableblocks.full_length").getString()+": "+measure(fullLength),-112,-28,224,()->{});
            button("−",-135,-28,22,()->fullLength=Math.max(0,fullLength-16/precision));button("+",113,-28,22,()->fullLength=Math.min(run(),fullLength+16/precision));
            button(Component.translatable(fullAtStart?"button.paintableblocks.full_start":"button.paintableblocks.full_end").getString(),-135,-7,270,()->{fullAtStart=!fullAtStart;if(fullAtStart)end=start;else start=end;});
        }else if(minecraft.player!=null&&minecraft.player.isCreative()){
            button(Component.translatable(create?"button.paintableblocks.create":"button.paintableblocks.transform").getString(),-135,-70,270,()->create=!create);
            if(create){
                button(Component.translatable("button.paintableblocks.edit_grid").getString()+": "+resolution+"×"+resolution,-135,-49,270,()->resolution=PaintingBalance.RESOLUTIONS.get((PaintingBalance.RESOLUTIONS.indexOf(resolution)+1)%5));
                button(Component.translatable("button.paintableblocks.dimension_step").getString()+": 1/"+precision,-135,-28,270,()->precision=PaintingBalance.RESOLUTIONS.get((PaintingBalance.RESOLUTIONS.indexOf(precision)+1)%5));
                for(int axis=0;axis<3;axis++){
                    final int a=axis;int y=-7+axis*21;
                    button("−",-135,y,22,()->adjust(dimensions,a,-1,1,256));
                    button(new String[]{"X: ","Y: ","Z: "}[a]+measure(dimensions[a]),-112,y,86,()->{});
                    button("+",-25,y,22,()->adjust(dimensions,a,1,1,256));
                    button("−",3,y,22,()->adjust(offsets,a,-1,0,15));
                    button("Δ"+new String[]{"X: ","Y: ","Z: "}[a]+measure(offsets[a]),26,y,86,()->{});
                    button("+",113,y,22,()->adjust(offsets,a,1,0,15));
                }
            }
        }
        confirm=addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.confirm"),b->{VolumePreview.action(1,direction,upper,create,1,1,1,resolution);minecraft.gui.setScreen(null);}).bounds(width/2-135,height/2+60,88,20).build());
        button(Component.translatable("button.paintableblocks.preview").getString(),-44,60,88,this::configure);
        addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.cancel"),b->onClose()).bounds(width/2+47,height/2+60,88,20).build());
    }
    @Override public void tick(){
        var s=VolumePreview.state();if(s==null){minecraft.gui.setScreen(null);return;}
        var r=VolumePreview.region(s);
        confirm.active=s.phase()==VolumeEditing.READY&&s.direction()==direction&&s.upper()==upper&&s.create()==create&&s.resolution()==resolution&&s.vertical()==vertical&&s.combined()==combined&&s.fullLength()==fullLength&&s.fullAtStart()==fullAtStart&&s.start()==start&&(s.end()<0?span():s.end())==end&&(!create||
            r.x1()-r.x0()==dimensions[0]&&r.y1()-r.y0()==dimensions[1]&&r.z1()-r.z0()==dimensions[2]&&Math.floorMod(r.x0(),16)==offsets[0]&&Math.floorMod(r.y0(),16)==offsets[1]&&Math.floorMod(r.z0(),16)==offsets[2]);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float tick){
        g.fill(width/2-145,height/2-112,width/2+145,height/2+123,0xbb222222);g.text(font,title,width/2-135,height/2-104,0xffffffff);
        var s=VolumePreview.state();if(s!=null){var r=VolumePreview.region(s);

            g.text(font,Component.translatable("message.paintableblocks.volume_progress",s.reviewed(),r.size(),s.modified(),s.ignored()),width/2-135,height/2+86,0xffdddddd);
            g.text(font,Component.translatable("message.paintableblocks.volume_planned",s.affected()),width/2-135,height/2+98,0xffdddddd);}
        super.extractRenderState(g,x,y,tick);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){VolumePreview.cancel();super.onClose();}
}
