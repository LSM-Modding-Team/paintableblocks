package net.nicomar.paintableblocks.client.selection;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
import net.nicomar.paintableblocks.client.screen.SlopeFormerScreen;

public final class VolumePreview {
    private static ModNetworking.VolumeState state;
    private static long opened;
    private static Object item;private static int slot;
    public static ModNetworking.VolumeState state(){return state;}
    private VolumePreview(){}
    public static void register(){NeoForge.EVENT_BUS.addListener(VolumePreview::key);NeoForge.EVENT_BUS.addListener(VolumePreview::tick);NeoForge.EVENT_BUS.addListener(VolumePreview::outline);}
    public static void payloads(RegisterClientPayloadHandlersEvent e){e.register(ModNetworking.VolumeState.TYPE,(v,ctx)->{
        var c=Minecraft.getInstance();if(c.player==null||c.level==null||!v.dimension().equals(c.level.dimension().identifier()))return;
        if(state!=null&&v.id()<state.id())return;
        if(v.phase()==0){state=null;return;}state=v;item=c.player.getItemInHand(v.hand()).getItem();slot=c.player.getInventory().getSelectedSlot();
        if(v.former()&&v.phase()!=VolumeEditing.FIRST&&opened!=v.id()){opened=v.id();c.gui.setScreen(new SlopeFormerScreen());}
    });}
    public static void action(int action,int direction,boolean upper,boolean create,int width,int height,int depth,int resolution){if(state!=null)ClientPacketDistributor.sendToServer(new ModNetworking.VolumeAction(state.id(),action,direction,upper,create,width,height,depth,resolution));}
    public static void cancel(){if(state!=null){action(2,0,false,false,1,1,1,1);state=null;}}
    private static boolean control(){var c=Minecraft.getInstance();return InputConstants.isKeyDown(c.getWindow(),GLFW.GLFW_KEY_LEFT_CONTROL)||InputConstants.isKeyDown(c.getWindow(),GLFW.GLFW_KEY_RIGHT_CONTROL);}
    /** Called before the ordinary brush/editor input handler; consumes only volume tools. */
    public static boolean input(InputEvent.InteractionKeyMappingTriggered e){
        var c=Minecraft.getInstance();if(c.player==null||c.level==null||c.gui.screen()!=null||!e.isUseItem())return false;
        var held=c.player.getItemInHand(e.getHand());boolean former=held.getItem() instanceof SlopeFormerItem,hammer=held.getItem() instanceof HammerItem;
        if(e.getHand()==InteractionHand.OFF_HAND&&(net.nicomar.paintableblocks.interaction.ServerInteractions.tool(c.player.getMainHandItem())||c.player.getMainHandItem().getItem() instanceof SlopeFormerItem||c.player.getMainHandItem().getItem() instanceof HammerItem&&(control()||state!=null))){e.setCanceled(true);e.setSwingHand(false);return true;}
        if(!former&&!(hammer&&(control()||state!=null)))return false;
        e.setCanceled(true);e.setSwingHand(false);SelectionPreview.discardBrushSelection();
        if(state!=null&&state.phase()==VolumeEditing.READY){action(1,state.direction(),state.upper(),state.create(),1,1,1,state.resolution());return true;}
        if(state!=null&&(state.phase()==VolumeEditing.RUNNING||state.phase()==VolumeEditing.SCANNING))return true;
        if(c.hitResult instanceof BlockHitResult hit&&hit.getType()==HitResult.Type.BLOCK&&c.level.getBlockEntity(hit.getBlockPos()) instanceof PaintableBlockEntity)
            ClientPacketDistributor.sendToServer(ModNetworking.Use.capture(c.player,e.getHand(),hit,SelectionPreview.requestId(),0,control(),false,false));
        return true;
    }
    private static void key(InputEvent.Key e){if(e.getKey()==GLFW.GLFW_KEY_ESCAPE&&e.getAction()==GLFW.GLFW_PRESS)cancel();}
    private static void tick(ClientTickEvent.Post e){
        var c=Minecraft.getInstance();if(state==null)return;if(c.player==null||c.level==null){state=null;return;}
        if(!state.dimension().equals(c.level.dimension().identifier())||c.player.getItemInHand(state.hand()).getItem()!=item||state.hand()==InteractionHand.MAIN_HAND&&c.player.getInventory().getSelectedSlot()!=slot){cancel();return;}
        if(c.gui.screen()==null){
            Component text=state.phase()==VolumeEditing.FIRST?Component.translatable("message.paintableblocks.volume_first"):
                    state.phase()==VolumeEditing.READY?Component.translatable("message.paintableblocks.volume_ready",state.affected(),state.ignored()):
                    Component.translatable("message.paintableblocks.volume_progress",state.reviewed(),region(state).size(),state.modified(),state.ignored());
            c.gui.hud.setOverlayMessage(text.copy().append(" ").append(Component.translatable("message.paintableblocks.volume_warning_short")),false);
        }
    }
    public static VolumeRegion region(ModNetworking.VolumeState s){return new VolumeRegion(s.x0(),s.y0(),s.z0(),s.x1(),s.y1(),s.z1());}
    private static void outline(ExtractBlockOutlineRenderStateEvent e){
        var s=state;var c=Minecraft.getInstance();if(s==null||c.player==null)return;var r=region(s);var anchor=e.getBlockPos();
        VoxelShape box=Shapes.box(r.x0()/16.0-anchor.getX(),r.y0()/16.0-anchor.getY(),r.z0()/16.0-anchor.getZ(),r.x1()/16.0-anchor.getX(),r.y1()/16.0-anchor.getY(),r.z1()/16.0-anchor.getZ());
        int color=s.phase()==VolumeEditing.READY?0xff55ff88:0xffffcc55;
        e.addCustomRenderer((render,collector,poses,levelState)->{var camera=levelState.cameraRenderState.pos;poses.pushPose();poses.translate(anchor.getX()-camera.x,anchor.getY()-camera.y,anchor.getZ()-camera.z);collector.order(1).submitShapeOutline(poses,box,RenderTypes.lines(),color,2.5F,false);poses.popPose();return true;});
        // At most 27 nearby pieces: distant selections never allocate a complete mesh.
        var center=c.player.blockPosition();for(int z=-1;z<=1;z++)for(int y=-1;y<=1;y++)for(int x=-1;x<=1;x++){
            var pos=center.offset(x,y,z);if(pos.getX()<r.min().getX()||pos.getX()>r.max().getX()||pos.getY()<r.min().getY()||pos.getY()>r.max().getY()||pos.getZ()<r.min().getZ()||pos.getZ()>r.max().getZ()||!c.level.hasChunkAt(pos))continue;
            if(!(c.level.getBlockEntity(pos) instanceof PaintableBlockEntity be)||!be.valid())continue;var d=be.design();
            try{var plan=s.former()?new net.nicomar.paintableblocks.operation.DesignOperations.Plan(d,d.withSlope(r.plane(pos,d,s.direction(),s.upper())),java.util.List.of(),java.util.List.of(),0):r.hammer(pos,d);var shape=s.former()?new DesignOutlineShape(plan.after()):SelectionPreview.cells(plan);
                e.addCustomRenderer((render,collector,poses,levelState)->{var camera=levelState.cameraRenderState.pos;poses.pushPose();poses.translate(pos.getX()-camera.x,pos.getY()-camera.y,pos.getZ()-camera.z);collector.order(1).submitShapeOutline(poses,shape,RenderTypes.lines(),color,1.5F,false);poses.popPose();return false;});
            }catch(IllegalArgumentException ignored){}
        }
    }
}
