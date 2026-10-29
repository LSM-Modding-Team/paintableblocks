package net.nicomar.paintableblocks.client.selection;

import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.item.*;
import net.nicomar.paintableblocks.interaction.ServerInteractions;
import net.nicomar.paintableblocks.network.ModNetworking;
import net.nicomar.paintableblocks.operation.DesignOperations;
import net.nicomar.paintableblocks.paint.PaintCatalog;
import net.nicomar.paintableblocks.registry.ModContent;

/** Previews and responses identify the level, block revision and logical held item, never ItemStack references. */
public final class SelectionPreview {
    private static ModNetworking.Selection selection;
    private static long nextRequest,lastSent;
    public static long requestId(){return ++nextRequest;}
    public static void discardBrushSelection(){selection=null;invalidate();}
    private static boolean overlayOwned;
    private static Key cachedKey;
    private static DesignOperations.Plan cachedPlan;
    private static VoxelShape cachedShape=Shapes.empty();
    private record Key(Identifier dimension,BlockPos pos,UUID block,long revision,DesignGeometry.Surface surface,InteractionHand hand,Object tool,int paint,boolean reverse,long selection){}
    private SelectionPreview(){}
    public static void register(){VolumePreview.register();NeoForge.EVENT_BUS.addListener(SelectionPreview::input);NeoForge.EVENT_BUS.addListener(SelectionPreview::outline);NeoForge.EVENT_BUS.addListener(SelectionPreview::tick);}
    public static void payloads(RegisterClientPayloadHandlersEvent event){VolumePreview.payloads(event);event.register(ModNetworking.Selection.TYPE,(v,ctx)->{
        if(v.request()!=lastSent)return;selection=v.active()?v:null;invalidate();
    });}
    private static void feedback(Component message){Minecraft.getInstance().gui.hud.setOverlayMessage(message,false);overlayOwned=true;}
    private static void clearFeedback(){if(overlayOwned){Minecraft.getInstance().gui.hud.setOverlayMessage(Component.empty(),false);overlayOwned=false;}}
    private static void invalidate(){cachedKey=null;cachedPlan=null;cachedShape=Shapes.empty();}
    private static boolean key(int first,int second){var c=Minecraft.getInstance();return InputConstants.isKeyDown(c.getWindow(),first)||InputConstants.isKeyDown(c.getWindow(),second);}
    private static boolean control(){return key(GLFW.GLFW_KEY_LEFT_CONTROL,GLFW.GLFW_KEY_RIGHT_CONTROL);}
    private static boolean reverse(){return key(GLFW.GLFW_KEY_LEFT_SHIFT,GLFW.GLFW_KEY_RIGHT_SHIFT);}
    private static InteractionHand activeHand(){var p=Minecraft.getInstance().player;return p!=null&&ServerInteractions.tool(p.getMainHandItem())?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;}
    private static boolean formerActive(){var p=Minecraft.getInstance().player;return p!=null&&(p.getMainHandItem().getItem() instanceof SlopeFormerItem||!ServerInteractions.tool(p.getMainHandItem())&&p.getOffhandItem().getItem() instanceof SlopeFormerItem);}
    private static boolean selectionValid(){
        var c=Minecraft.getInstance();var s=selection;if(s==null)return true;
        if(c.player==null||c.level==null||!s.dimension().equals(c.level.dimension().identifier())||!c.level.hasChunkAt(s.pos())||!c.player.isWithinBlockInteractionRange(s.pos(),0)||s.face()<0||s.face()>=12)return false;
        var held=c.player.getItemInHand(s.hand());
        return BuiltInRegistries.ITEM.getKey(held.getItem()).equals(s.item())&&(s.hand()!=InteractionHand.MAIN_HAND||c.player.getInventory().getSelectedSlot()==s.slot())
                &&held.getItem() instanceof BrushItem brush&&brush.valid(held)&&brush.content(held).paint()==s.paint()
                &&c.level.getBlockEntity(s.pos()) instanceof PaintableBlockEntity be&&be.valid()&&be.editIdentity().equals(s.block())&&be.revision()==s.revision()
                &&s.cell()>=0&&s.cell()<be.design().resolution()*be.design().resolution()*be.design().resolution();
    }
    private static void cancel(){
        if(selection==null)return;var s=selection;lastSent=++nextRequest;
        ClientPacketDistributor.sendToServer(new ModNetworking.Use(lastSent,s.selection(),s.hand(),s.slot(),s.dimension(),s.pos(),new Vec3(s.pos().getX()+.5,s.pos().getY()+.5,s.pos().getZ()+.5),Direction.UP,s.item(),s.paint(),s.block(),s.revision(),false,false,true));
        selection=null;invalidate();
    }
    private static void input(InputEvent.InteractionKeyMappingTriggered e){
        if(VolumePreview.input(e))return;
        var c=Minecraft.getInstance();if(c.player==null||c.level==null||c.gui.screen()!=null||!e.isUseItem())return;
        if(selection!=null&&!selectionValid()){cancel();e.setCanceled(true);e.setSwingHand(false);return;}
        if(selection!=null&&control()){if(e.getHand()==selection.hand())cancel();e.setCanceled(true);e.setSwingHand(false);return;}
        if(!(c.hitResult instanceof BlockHitResult hit)||hit.getType()!=HitResult.Type.BLOCK||!ServerInteractions.relevant(c.player,hit.getBlockPos()))return;
        var hand=e.getHand();
        if(hand==InteractionHand.OFF_HAND&&ServerInteractions.tool(c.player.getMainHandItem())){e.setCanceled(true);e.setSwingHand(false);return;}
        if(!ServerInteractions.tool(c.player.getItemInHand(hand)))return;
        e.setCanceled(true);e.setSwingHand(false);lastSent=++nextRequest;
        ClientPacketDistributor.sendToServer(ModNetworking.Use.capture(c.player,hand,hit,lastSent,selection==null?0:selection.selection(),control(),reverse(),false));
    }
    private static void tick(ClientTickEvent.Post event){
        if(VolumePreview.state()!=null||formerActive()){discardBrushSelection();return;}
        var c=Minecraft.getInstance();if(c.player==null||c.level==null){selection=null;invalidate();clearFeedback();return;}
        if(selection!=null&&(!selectionValid()||c.gui.screen()!=null))cancel();
        if(c.gui.screen()!=null||!(c.hitResult instanceof BlockHitResult hit)||hit.getType()!=HitResult.Type.BLOCK||!(c.level.getBlockEntity(hit.getBlockPos()) instanceof PaintableBlockEntity be)){invalidate();clearFeedback();return;}
        var held=c.player.getMainHandItem();if(!held.is(ModContent.GLOVES.get())&&!held.is(ModContent.TONGS.get()))held=c.player.getOffhandItem();
        if(!be.valid()){invalidate();if(ServerInteractions.tool(c.player.getItemInHand(activeHand()))||held.is(ModContent.GLOVES.get())||held.is(ModContent.TONGS.get()))feedback(Component.translatable("message.paintableblocks.invalid_design"));else clearFeedback();return;}
        if(!ServerInteractions.tool(c.player.getItemInHand(activeHand()))&&(held.is(ModContent.GLOVES.get())||held.is(ModContent.TONGS.get()))){invalidate();feedback(Component.translatable(c.player.isCreative()?"message.paintableblocks.creative_copy":held.is(ModContent.GLOVES.get())?"message.paintableblocks.keep_design":"message.paintableblocks.keep_shape"));return;}
        if(!prepare(hit,be)){if(selection!=null)feedback(Component.translatable("message.paintableblocks.invalid_area"));else clearFeedback();return;}
        var tool=c.player.getItemInHand(activeHand());
        if(tool.getItem() instanceof BrushItem brush&&brush.valid(tool))feedback(Component.translatable(selection==null?"message.paintableblocks.paint_cost":"message.paintableblocks.area_cost",cachedPlan.units(),brush.content(tool).units()));else clearFeedback();
    }
    private static boolean prepare(BlockHitResult hit,PaintableBlockEntity be){
        var c=Minecraft.getInstance();var hand=activeHand();var held=c.player.getItemInHand(hand);
        if(!be.valid()||!ServerInteractions.tool(held)){invalidate();return false;}
        var d=be.design();var target=DesignGeometry.target(d,hit.getLocation().subtract(hit.getBlockPos().getX(),hit.getBlockPos().getY(),hit.getBlockPos().getZ()),hit.getDirection());if(target.isEmpty()){invalidate();return false;}
        int paint=held.getItem() instanceof BrushItem b&&b.valid(held)?b.content(held).paint():0;
        var key=new Key(c.level.dimension().identifier(),hit.getBlockPos(),be.editIdentity(),be.revision(),target.get(),hand,held.getItem(),paint,reverse(),selection==null?0:selection.selection());
        if(key.equals(cachedKey))return true;
        invalidate();
        try{
            if(held.getItem() instanceof BrushItem){
                if(!PaintCatalog.validId(paint)||selection!=null&&(!selectionValid()||selection.hand()!=hand||!selection.pos().equals(hit.getBlockPos())))return false;
                var faces=selection==null?List.of(target.get()):DesignOperations.area(d,new DesignGeometry.Surface(selection.cell(),Direction.values()[selection.face()%6],selection.face()>=6),target.get());
                cachedPlan=DesignOperations.paint(d,faces,paint);cachedShape=surfaces(d,cachedPlan.faces());
            }else{cachedPlan=DesignOperations.tool(d,target.get(),((EditingToolItem)held.getItem()).action(),hit.getDirection(),reverse());cachedShape=held.getItem() instanceof RotationWrenchItem?DesignGeometry.shape(cachedPlan.after()):shape(cachedPlan);}
            cachedKey=key;return true;
        }catch(IllegalArgumentException invalid){invalidate();return false;}
    }
    private static void outline(ExtractBlockOutlineRenderStateEvent event){
        var c=Minecraft.getInstance();if(VolumePreview.state()!=null||formerActive())return;if(c.player==null||c.player.isSpectator()||!(event.getLevel().getBlockEntity(event.getBlockPos()) instanceof PaintableBlockEntity be)||!prepare(event.getHitResult(),be))return;
        var held=c.player.getItemInHand(activeHand());VoxelShape shape=cachedShape;
        boolean insufficient=held.getItem() instanceof BrushItem b&&!c.player.isCreative()&&(b.content(held).units()<cachedPlan.units()||!PaintCatalog.byId(cachedKey.paint).survivalAvailable());
        boolean obstructed=false;
        if(held.getItem() instanceof RotationWrenchItem&&!shape.isEmpty()){var pos=event.getBlockPos();var worldShape=shape.move(pos.getX(),pos.getY(),pos.getZ());for(var collision:event.getLevel().getEntityCollisions(null,worldShape.bounds()))if(Shapes.joinIsNotEmpty(worldShape,collision,BooleanOp.AND)){obstructed=true;break;}}
        int color=insufficient||obstructed?0xffff5555:0xff55ddff;
        event.addCustomRenderer((state,collector,poses,levelState)->{
            if(!shape.isEmpty()){var camera=levelState.cameraRenderState.pos;var pos=state.pos();poses.pushPose();poses.translate(pos.getX()-camera.x,pos.getY()-camera.y,pos.getZ()-camera.z);
            collector.order(1).submitShapeOutline(poses,shape,RenderTypes.lines(),color,2.5F,false);poses.popPose();}return true;
        });
    }
    private static int physicalCell(BlockDesign d,int cell){int[] xyz=DesignOrientation.cell(d.orientation(),d.x(cell),d.y(cell),d.z(cell),d.resolution());return d.index(xyz[0],xyz[1],xyz[2]);}
    public static VoxelShape cells(DesignOperations.Plan plan){return shape(plan);}
    private static VoxelShape shape(DesignOperations.Plan plan){
        if(!plan.cells().isEmpty()){
            var d=plan.before();int n=d.resolution();VoxelShape out=Shapes.empty();
            for(int cell:plan.cells()){int c=physicalCell(d,cell);double x=d.x(c)/(double)n,y=d.y(c)/(double)n,z=d.z(c)/(double)n;out=Shapes.joinUnoptimized(out,Shapes.box(x,y,z,x+1.0/n,y+1.0/n,z+1.0/n),BooleanOp.OR);}return out.optimize();
        }
        return !plan.faces().isEmpty()?surfaces(plan.before(),plan.faces()):Shapes.empty();
    }
    private static VoxelShape surfaces(BlockDesign d,List<DesignGeometry.Surface> faces){
        if(d.slope()!=null)return new net.nicomar.paintableblocks.design.SurfaceOutlineShape(d,faces);
        int n=d.resolution();double thin=.0005;VoxelShape out=Shapes.empty();
        for(var face:faces){int c=physicalCell(d,face.cell());var dir=DesignOrientation.direction(d.orientation(),face.face());
            double x=d.x(c)/(double)n,y=d.y(c)/(double)n,z=d.z(c)/(double)n;double[] min={x,y,z},max={x+1.0/n,y+1.0/n,z+1.0/n};int axis=dir.getAxis()==Direction.Axis.X?0:dir.getAxis()==Direction.Axis.Y?1:2;
            double plane=dir.getAxisDirection()==Direction.AxisDirection.POSITIVE?max[axis]:min[axis];min[axis]=plane-thin;max[axis]=plane+thin;
            out=Shapes.joinUnoptimized(out,Shapes.box(min[0],min[1],min[2],max[0],max[1],max[2]),BooleanOp.OR);
        }return out.optimize();
    }
}
