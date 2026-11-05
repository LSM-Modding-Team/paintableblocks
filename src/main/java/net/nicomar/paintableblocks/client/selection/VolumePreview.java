package net.nicomar.paintableblocks.client.selection;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.nicomar.paintableblocks.item.*;
import net.nicomar.paintableblocks.network.ModNetworking;
import net.nicomar.paintableblocks.interaction.VolumeEditing;
import net.nicomar.paintableblocks.operation.VolumeRegion;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;

public final class VolumePreview {
    private static ModNetworking.VolumeState state;
    private static Object item;private static int slot;
    private record PreviewKey(VolumeRegion region,boolean former,boolean create,int direction,boolean upper,BlockDesign.GeometryKey geometry,int orientation,boolean vertical,int start,int end,boolean combined,int fullLength,boolean fullAtStart,boolean bothEnds,int otherLength,int lateralLow,int lateralHigh){}
    private record Preview(PreviewKey key,PreviewLines shape,PreviewLines before,boolean rejected){}
    private static final java.util.Map<net.minecraft.core.BlockPos,Preview> PREVIEWS=new java.util.HashMap<>();
    private static long feedbackTick;
    private static boolean previewPending;
    private static String previewConfig="";private static long previewCursor,previewVisited;
    private record Marker(Vec3 point,int color){}
    private static final java.util.List<Marker> markers=new java.util.ArrayList<>();

    private static ModNetworking.VolumeNotice notice;
    public static ModNetworking.VolumeNotice notice(){return notice;}
    public static ModNetworking.VolumeState state(){return state;}
    private VolumePreview(){}
    public static void register(){NeoForge.EVENT_BUS.addListener(VolumePreview::key);NeoForge.EVENT_BUS.addListener(VolumePreview::tick);NeoForge.EVENT_BUS.addListener(VolumePreview::outline);}
    public static void payloads(RegisterClientPayloadHandlersEvent e){e.register(ModNetworking.VolumeNotice.TYPE,(v,ctx)->{notice=v;if(v.reason().startsWith("undo_")){var client=Minecraft.getInstance();client.gui.hud.setOverlayMessage(Component.translatable("message.paintableblocks."+v.reason()),false);}});e.register(ModNetworking.VolumeState.TYPE,(v,ctx)->{
        var c=Minecraft.getInstance();if(c.player==null||c.level==null||!v.dimension().equals(c.level.dimension().identifier()))return;
        if(state!=null&&v.id()<state.id())return;
        if(v.phase()==0){state=null;PREVIEWS.clear();markers.clear();return;}if(state==null||state.id()!=v.id()||v.phase()==VolumeEditing.SCANNING)notice=null;state=v;feedbackTick=0;item=c.player.getItemInHand(v.hand()).getItem();slot=c.player.getInventory().getSelectedSlot();
    });}
    public static void action(int action,int direction,boolean upper,boolean create,int width,int height,int depth,int resolution){if(state!=null)ClientPacketDistributor.sendToServer(new ModNetworking.VolumeAction(state.id(),action,direction,upper,create,width,height,depth,resolution));}
    public static void configure(int direction,boolean upper,boolean create,int width,int height,int depth,int resolution,int x,int y,int z,boolean vertical,int start,int end,boolean combined,int fullLength,boolean fullAtStart,boolean bothEnds,int otherLength){notice=null;if(state!=null)ClientPacketDistributor.sendToServer(new ModNetworking.VolumeAction(state.id(),0,direction,upper,create,width,height,depth,resolution,x,y,z,vertical,start,end,combined,fullLength,fullAtStart,bothEnds,otherLength));}
    public static void cancel(){if(state!=null){action(2,0,false,false,1,1,1,1);state=null;PREVIEWS.clear();markers.clear();}}
    private static boolean control(){var c=Minecraft.getInstance();return InputConstants.isKeyDown(c.getWindow(),GLFW.GLFW_KEY_LEFT_CONTROL)||InputConstants.isKeyDown(c.getWindow(),GLFW.GLFW_KEY_RIGHT_CONTROL);}
    /** Called before the ordinary brush/editor input handler; consumes only volume tools. */
    public static boolean input(InputEvent.InteractionKeyMappingTriggered e){
        var c=Minecraft.getInstance();if(c.player==null||c.level==null||c.gui.screen()!=null||!e.isUseItem())return false;
        var held=c.player.getItemInHand(e.getHand());boolean former=held.getItem() instanceof SlopeFormerItem,hammer=held.getItem() instanceof HammerItem;
        if(e.getHand()==InteractionHand.OFF_HAND&&(net.nicomar.paintableblocks.interaction.ServerInteractions.tool(c.player.getMainHandItem())||c.player.getMainHandItem().getItem() instanceof SlopeFormerItem||c.player.getMainHandItem().getItem() instanceof HammerItem&&(control()||state!=null))){e.setCanceled(true);e.setSwingHand(false);return true;}
        if(!former&&!(hammer&&(control()||state!=null)))return false;
        e.setCanceled(true);e.setSwingHand(false);SelectionPreview.discardBrushSelection();
        if(former&&c.player.isShiftKeyDown()&&(c.hitResult==null||c.hitResult.getType()!=HitResult.Type.BLOCK)){ClientPacketDistributor.sendToServer(new ModNetworking.VolumeAction(0,3,0,false,false,1,1,1,1));return true;}
        if(state!=null&&state.phase()==VolumeEditing.READY&&(!former||control())){action(1,state.direction(),state.upper(),state.create(),1,1,1,state.resolution());return true;}
        if(state!=null&&(state.phase()==VolumeEditing.RUNNING||state.phase()==VolumeEditing.SCANNING))return true;
        if(c.hitResult instanceof BlockHitResult hit&&hit.getType()==HitResult.Type.BLOCK&&c.level.getBlockEntity(hit.getBlockPos()) instanceof PaintableBlockEntity){
            if(former){if(markers.size()>=6)markers.subList(2,markers.size()).clear();var p=hit.getLocation();markers.add(new Marker(new Vec3(Math.rint(p.x*16)/16,Math.rint(p.y*16)/16,Math.rint(p.z*16)/16),c.player.isShiftKeyDown()?0xffffdd55:0xff55ddff));}
            ClientPacketDistributor.sendToServer(ModNetworking.Use.capture(c.player,e.getHand(),hit,SelectionPreview.requestId(),0,control(),c.player.isShiftKeyDown(),false));
        }
        return true;
    }
    private static void key(InputEvent.Key e){
        var c=Minecraft.getInstance();if(c.gui.screen()!=null||e.getAction()!=GLFW.GLFW_PRESS)return;
        if(e.getKey()==GLFW.GLFW_KEY_ESCAPE||e.getKey()==GLFW.GLFW_KEY_BACKSPACE){cancel();return;}
        var s=state;if(s==null||!s.former()||c.player==null)return;
        if(e.getKey()==GLFW.GLFW_KEY_ENTER){if(s.phase()==VolumeEditing.READY)action(1,s.direction(),s.upper(),s.create(),1,1,1,s.resolution());return;}
        if(s.phase()==VolumeEditing.FIRST||s.phase()==VolumeEditing.RUNNING)return;
        if(e.getKey()==GLFW.GLFW_KEY_F10||e.getKey()==GLFW.GLFW_KEY_F11){
            if(s.phase()!=VolumeEditing.FIRST&&s.phase()!=VolumeEditing.RUNNING&&s.phase()!=VolumeEditing.SCANNING){markers.clear();action(e.getKey()==GLFW.GLFW_KEY_F10?7:8,s.direction(),s.upper(),s.create(),1,1,1,s.resolution());}return;
        }
        if(e.getKey()==GLFW.GLFW_KEY_F4||e.getKey()==GLFW.GLFW_KEY_F5||e.getKey()==GLFW.GLFW_KEY_F6){
            if(s.phase()==VolumeEditing.READY){markers.clear();action(e.getKey()==GLFW.GLFW_KEY_F4?4:e.getKey()==GLFW.GLFW_KEY_F5?5:6,s.direction(),s.upper(),s.create(),1,1,1,s.resolution());}return;
        }
        int direction=s.direction(),start=s.start(),end=s.end()<0?(s.vertical()?(direction%2==0?s.z1()-s.z0():s.x1()-s.x0()):s.y1()-s.y0()):s.end(),resolution=s.resolution();boolean vertical=s.vertical(),upper=s.upper(),create=s.create();
        switch(e.getKey()){
            case GLFW.GLFW_KEY_F7 -> upper=!upper;
            case GLFW.GLFW_KEY_F8 -> {if(c.player.isCreative())create=!create;}
            case GLFW.GLFW_KEY_F9 -> resolution=net.nicomar.paintableblocks.balance.PaintingBalance.RESOLUTIONS.get((net.nicomar.paintableblocks.balance.PaintingBalance.RESOLUTIONS.indexOf(resolution)+1)%5);
            default -> {return;}
        }
        int max=vertical?(direction%2==0?s.z1()-s.z0():s.x1()-s.x0()):s.y1()-s.y0();
        configure(direction,upper,create,s.x1()-s.x0(),s.y1()-s.y0(),s.z1()-s.z0(),resolution,Math.floorMod(s.x0(),16),Math.floorMod(s.y0(),16),Math.floorMod(s.z0(),16),vertical,Math.min(start,max),Math.min(end,max),s.combined(),s.fullLength(),s.fullAtStart(),s.bothEnds(),s.otherLength());
    }
    private static void tick(ClientTickEvent.Post e){
        var c=Minecraft.getInstance();if(state==null)return;if(c.player==null||c.level==null){state=null;PREVIEWS.clear();return;}
        if(!state.dimension().equals(c.level.dimension().identifier())||c.player.getItemInHand(state.hand()).getItem()!=item||state.hand()==InteractionHand.MAIN_HAND&&c.player.getInventory().getSelectedSlot()!=slot){cancel();return;}
        prepare();
        if(c.gui.screen()==null&&(feedbackTick==0||c.level.getGameTime()-feedbackTick>=20)){
            feedbackTick=c.level.getGameTime();
            Component text=state.phase()==VolumeEditing.CONFIG&&state.former()?Component.translatable("message.paintableblocks.world_second_handle"):state.phase()==VolumeEditing.FIRST?Component.translatable(state.former()?"message.paintableblocks.world_first":"message.paintableblocks.volume_first"):
                    state.phase()==VolumeEditing.READY?Component.translatable(state.former()?"message.paintableblocks.world_ready":"message.paintableblocks.volume_ready",state.affected(),state.ignored()):
                    Component.translatable("message.paintableblocks.volume_progress",state.reviewed(),region(state).size(),state.modified(),state.ignored());
            boolean simplified=PREVIEWS.values().stream().anyMatch(v->v.shape().simplified||v.shape().count()>8192/Math.max(1,PREVIEWS.size()));
            if(notice!=null)text=Component.translatable("message.paintableblocks."+notice.reason()).append(" "+notice.pos().toShortString());
            c.gui.hud.setOverlayMessage(text.copy().append(" ").append(Component.translatable(state.former()?"message.paintableblocks.world_controls":"message.paintableblocks.volume_warning_short")).append(simplified?Component.translatable("message.paintableblocks.preview_simplified"):Component.empty()).append(previewPending?Component.translatable("message.paintableblocks.preview_loading"):Component.empty()),false);
        }
    }
    public static VolumeRegion region(ModNetworking.VolumeState s){return new VolumeRegion(s.x0(),s.y0(),s.z0(),s.x1(),s.y1(),s.z1());}
    private static void outline(ExtractBlockOutlineRenderStateEvent e){
        var s=state;var c=Minecraft.getInstance();if(c.player==null||c.gui.screen()!=null)return;
        if(c.player.getMainHandItem().getItem() instanceof SlopeFormerItem||c.player.getOffhandItem().getItem() instanceof SlopeFormerItem){
            if(c.hitResult instanceof BlockHitResult hit&&hit.getType()==HitResult.Type.BLOCK){
                var pos=hit.getBlockPos();var local=hit.getLocation().subtract(pos.getX(),pos.getY(),pos.getZ());var grid=PreviewLines.grid(hit.getDirection(),hit.getDirection().getAxis()==net.minecraft.core.Direction.Axis.X?local.x:hit.getDirection().getAxis()==net.minecraft.core.Direction.Axis.Y?local.y:local.z);var cell=VolumeRegion.aim(pos,hit.getLocation(),hit.getDirection());
                var box=Shapes.box(cell.x0()/16.0-pos.getX(),cell.y0()/16.0-pos.getY(),cell.z0()/16.0-pos.getZ(),cell.x1()/16.0-pos.getX(),cell.y1()/16.0-pos.getY(),cell.z1()/16.0-pos.getZ());
                e.addCustomRenderer((render,collector,poses,levelState)->{var camera=levelState.cameraRenderState.pos;poses.pushPose();poses.translate(pos.getX()-camera.x,pos.getY()-camera.y,pos.getZ()-camera.z);collector.order(1).submitShapeOutline(poses,grid,RenderTypes.lines(),0x6655ddff,1F,false);collector.order(1).submitShapeOutline(poses,box,RenderTypes.lines(),0xff55ddff,2F,false);poses.popPose();return false;});
            }
            var anchor=e.getBlockPos();
            for(var mark:java.util.List.copyOf(markers)){var p=mark.point().subtract(anchor.getX(),anchor.getY(),anchor.getZ());var shape=Shapes.box(p.x-.015625,p.y-.015625,p.z-.015625,p.x+.015625,p.y+.015625,p.z+.015625);e.addCustomRenderer((render,collector,poses,levelState)->{var camera=levelState.cameraRenderState.pos;poses.pushPose();poses.translate(anchor.getX()-camera.x,anchor.getY()-camera.y,anchor.getZ()-camera.z);collector.order(1).submitShapeOutline(poses,shape,RenderTypes.lines(),mark.color(),3F,false);poses.popPose();return false;});}
        }
        if(s==null)return;var r=region(s);var anchor=e.getBlockPos();
        VoxelShape box=Shapes.box(r.x0()/16.0-anchor.getX(),r.y0()/16.0-anchor.getY(),r.z0()/16.0-anchor.getZ(),r.x1()/16.0-anchor.getX(),r.y1()/16.0-anchor.getY(),r.z1()/16.0-anchor.getZ());
        if(s.former()&&s.phase()!=VolumeEditing.FIRST){
            var arrow=PreviewLines.direction(s,anchor);
            e.addCustomRenderer((render,collector,poses,levelState)->{var camera=levelState.cameraRenderState.pos;poses.pushPose();poses.translate(anchor.getX()-camera.x,anchor.getY()-camera.y,anchor.getZ()-camera.z);collector.order(1).submitShapeOutline(poses,arrow,RenderTypes.lines(),0xff55ddff,3F,false);poses.popPose();return false;});
        }
        int color=s.phase()==VolumeEditing.READY?0xff55ff88:0xffffcc55;
        e.addCustomRenderer((render,collector,poses,levelState)->{var camera=levelState.cameraRenderState.pos;poses.pushPose();poses.translate(anchor.getX()-camera.x,anchor.getY()-camera.y,anchor.getZ()-camera.z);collector.order(1).submitShapeOutline(poses,box,RenderTypes.lines(),color,2.5F,false);poses.popPose();return true;});
        for(var entry:PREVIEWS.entrySet()){
            int pieceColor=entry.getValue().rejected()?0xffff5555:color;var pos=entry.getKey();var shape=entry.getValue().shape().limited(8192/Math.max(1,PREVIEWS.size()));
            e.addCustomRenderer((render,collector,poses,levelState)->{var camera=levelState.cameraRenderState.pos;poses.pushPose();poses.translate(pos.getX()-camera.x,pos.getY()-camera.y,pos.getZ()-camera.z);collector.order(1).submitShapeOutline(poses,shape,RenderTypes.lines(),pieceColor,1.5F,false);poses.popPose();return false;});
            if(s.former()&&!s.create()){var before=entry.getValue().before().limited(8192/Math.max(1,PREVIEWS.size()));e.addCustomRenderer((render,collector,poses,levelState)->{var camera=levelState.cameraRenderState.pos;poses.pushPose();poses.translate(pos.getX()-camera.x,pos.getY()-camera.y,pos.getZ()-camera.z);collector.order(1).submitShapeOutline(poses,before,RenderTypes.lines(),0x88ff5555,1F,false);poses.popPose();return false;});}
        }
    }
    private static void prepare(){
        var c=Minecraft.getInstance();var s=state;if(s==null)return;var r=region(s);
        String config=s.id()+"|"+r+"|"+s.direction()+"|"+s.upper()+"|"+s.create()+"|"+s.resolution()+"|"+s.vertical()+"|"+s.start()+"|"+s.end()+"|"+s.combined()+"|"+s.fullLength()+"|"+s.fullAtStart()+"|"+s.bothEnds()+"|"+s.otherLength()+"|"+s.lateralLow()+"|"+s.lateralHigh();
        if(!config.equals(previewConfig)){previewConfig=config;previewCursor=0;previewVisited=0;PREVIEWS.clear();}
        long deadline=System.nanoTime()+1_000_000;int work=16,scanned=0;boolean missing=false;
        while(scanned++<64&&work>0&&System.nanoTime()<deadline){
            var pos=r.position(previewCursor);previewCursor=(previewCursor+1)%r.size();previewVisited=Math.min(r.size(),previewVisited+1);
            if(!c.level.hasChunkAt(pos)){PREVIEWS.remove(pos);missing=true;continue;}
            BlockDesign d;
            if(s.create()){if(!c.level.getBlockState(pos).isAir()){PREVIEWS.remove(pos);continue;}d=BlockDesign.full(s.resolution());}
            else{if(!(c.level.getBlockEntity(pos) instanceof PaintableBlockEntity be)||!be.valid()){PREVIEWS.remove(pos);continue;}d=be.design();}
            var key=new PreviewKey(r,s.former(),s.create(),s.direction(),s.upper(),d.geometryKey(),d.orientation(),s.vertical(),s.start(),s.end(),s.combined(),s.fullLength(),s.fullAtStart(),s.bothEnds(),s.otherLength(),s.lateralLow(),s.lateralHigh());
            var cached=PREVIEWS.get(pos);if(cached!=null&&cached.key().equals(key))continue;PREVIEWS.remove(pos);work--;
            try{
                var clean=d.withoutPaint();if(s.bothEnds())clean=r.doubleSeed(pos,clean);
                var shape=s.former()?new DesignOutlineShape(clean.withSlope(r.lateralPlane(pos,clean,r.doublePlane(pos,clean,s.direction(),s.upper(),s.vertical(),s.start(),s.end(),s.create(),s.combined(),s.fullLength(),s.fullAtStart(),s.bothEnds(),s.otherLength()),s.direction(),s.vertical(),s.lateralLow(),s.lateralHigh()))):SelectionPreview.cells(r.hammer(pos,clean));
                int detail=Math.max(8,(int)(8192/r.size()));
                PREVIEWS.put(pos,new Preview(key,PreviewLines.capture(shape).limited(detail),PreviewLines.capture(new DesignOutlineShape(d)).limited(detail),false));
            }catch(IllegalArgumentException bad){
                var before=PreviewLines.capture(new DesignOutlineShape(d)).limited(64);PREVIEWS.put(pos,new Preview(key,before,before,true));
            }
            if(previewVisited>=r.size()&&previewCursor==0)break;
        }
        boolean pending=previewVisited<r.size()||missing;if(pending!=previewPending){previewPending=pending;feedbackTick=0;}
    }
}
