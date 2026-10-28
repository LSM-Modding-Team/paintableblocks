package net.nicomar.paintableblocks.client.selection;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
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

/** Extract immutable outlines only; rendering callbacks never retain a level or block entity. */
public final class SelectionPreview {
    private static ModNetworking.Selection selection;
    private static BlockDesign selectedDesign;
    private static Object selectedItem;
    private static Key cachedKey;
    private static DesignOperations.Plan cachedPlan;
    private static VoxelShape cachedShape=Shapes.empty();
    private record Key(BlockDesign design,DesignGeometry.Surface surface,Object tool,int paint,boolean reverse,ModNetworking.Selection selection){}
    private SelectionPreview(){}
    public static void register(){NeoForge.EVENT_BUS.addListener(SelectionPreview::input);NeoForge.EVENT_BUS.addListener(SelectionPreview::outline);NeoForge.EVENT_BUS.addListener(SelectionPreview::tick);}
    public static void payloads(RegisterClientPayloadHandlersEvent event){event.register(ModNetworking.Selection.TYPE,(v,ctx)->{
        selection=v.active()?v:null;selectedDesign=null;selectedItem=null;var c=Minecraft.getInstance();
        if(selection!=null&&c.level!=null&&c.player!=null&&c.level.getBlockEntity(v.pos()) instanceof PaintableBlockEntity be&&be.valid()&&v.cell()>=0&&v.cell()<be.design().resolution()*be.design().resolution()*be.design().resolution()&&v.face()>=0&&v.face()<6){selectedDesign=be.design();selectedItem=c.player.getMainHandItem();}
        else selection=null;
    });}
    private static boolean key(int first,int second){var c=Minecraft.getInstance();return InputConstants.isKeyDown(c.getWindow(),first)||InputConstants.isKeyDown(c.getWindow(),second);}
    private static boolean control(){return key(GLFW.GLFW_KEY_LEFT_CONTROL,GLFW.GLFW_KEY_RIGHT_CONTROL);}
    private static boolean reverse(){return key(GLFW.GLFW_KEY_LEFT_SHIFT,GLFW.GLFW_KEY_RIGHT_SHIFT);}
    private static void input(InputEvent.InteractionKeyMappingTriggered e){
        var c=Minecraft.getInstance();if(c.player==null||c.gui.screen()!=null||!e.isUseItem()||!ServerInteractions.tool(c.player.getMainHandItem()))return;
        e.setCanceled(true);e.setSwingHand(false);if(e.getHand()==InteractionHand.MAIN_HAND)PacketDistributor.sendToServer(new ModNetworking.Use(control(),reverse(),false));
    }
    private static void tick(ClientTickEvent.Post event){
        var c=Minecraft.getInstance();if(c.player==null||c.level==null){selection=null;cachedKey=null;return;}
        if(selection!=null&&(selectedItem!=c.player.getMainHandItem()||!c.level.hasChunkAt(selection.pos())||!c.player.isWithinBlockInteractionRange(selection.pos(),0)
                ||!(c.level.getBlockEntity(selection.pos()) instanceof PaintableBlockEntity b)||!b.valid()||!b.design().equals(selectedDesign))){selection=null;PacketDistributor.sendToServer(new ModNetworking.Use(false,false,true));}
        if(c.gui.screen()!=null||!(c.hitResult instanceof BlockHitResult hit)||hit.getType()!=HitResult.Type.BLOCK||!(c.level.getBlockEntity(hit.getBlockPos()) instanceof PaintableBlockEntity be))return;
        var held=c.player.getMainHandItem();
        if(held.is(ModContent.GLOVES.get())||held.is(ModContent.TONGS.get()))c.gui.hud.setOverlayMessage(Component.translatable(c.player.isCreative()?"message.paintableblocks.creative_copy":held.is(ModContent.GLOVES.get())?"message.paintableblocks.keep_design":"message.paintableblocks.keep_shape"),false);
        else if(cachedKey!=null&&cachedKey.design==be.design()&&held.getItem() instanceof BrushItem brush&&brush.valid(held))c.gui.hud.setOverlayMessage(Component.translatable(selection==null?"message.paintableblocks.paint_cost":"message.paintableblocks.area_cost",cachedPlan.units(),brush.content(held).units()),false);
    }
    private static void outline(ExtractBlockOutlineRenderStateEvent event){
        var c=Minecraft.getInstance();if(c.player==null||c.player.isSpectator()||!(event.getLevel().getBlockEntity(event.getBlockPos()) instanceof PaintableBlockEntity be)||!be.valid())return;
        var held=c.player.getMainHandItem();if(!ServerInteractions.tool(held))return;var d=be.design();var hit=event.getHitResult();
        var target=DesignGeometry.target(d,hit.getLocation().subtract(event.getBlockPos().getX(),event.getBlockPos().getY(),event.getBlockPos().getZ()),hit.getDirection());if(target.isEmpty())return;
        int paint=held.getItem() instanceof BrushItem b&&b.valid(held)?b.content(held).paint():0;
        var key=new Key(d,target.get(),held.getItem(),paint,reverse(),selection);
        try{
            if(!key.equals(cachedKey)){
                if(held.getItem() instanceof BrushItem){
                    if(!PaintCatalog.validId(paint))return;
                    if(selection!=null&&!selection.pos().equals(event.getBlockPos()))return;
                    var faces=selection==null?List.of(target.get()):DesignOperations.area(d,new DesignGeometry.Surface(selection.cell(),Direction.values()[selection.face()]),target.get());
                    cachedPlan=DesignOperations.paint(d,faces,paint);
                    // First corner and unchanged surfaces remain visible even when the cost is zero.
                    cachedShape=surfaces(d,faces);
                }else{cachedPlan=DesignOperations.tool(d,target.get(),((EditingToolItem)held.getItem()).action(),hit.getDirection(),reverse());cachedShape=held.getItem() instanceof RotationWrenchItem?DesignGeometry.shape(cachedPlan.after()):shape(cachedPlan);}
                cachedKey=key;
            }
        }catch(IllegalArgumentException invalid){return;}
        VoxelShape shape=cachedShape;if(shape.isEmpty())return;
        boolean insufficient=held.getItem() instanceof BrushItem b&&(!c.player.isCreative()&&(b.content(held).units()<cachedPlan.units()||!PaintCatalog.byId(paint).survivalAvailable()));
        boolean obstructed=false;
        if(held.getItem() instanceof RotationWrenchItem){var pos=event.getBlockPos();var worldShape=shape.move(pos.getX(),pos.getY(),pos.getZ());for(var collision:event.getLevel().getEntityCollisions(null,worldShape.bounds()))if(Shapes.joinIsNotEmpty(worldShape,collision,BooleanOp.AND)){obstructed=true;break;}}
        int color=insufficient||obstructed?0xffff5555:0xff55ddff;
        event.addCustomRenderer((state,collector,poses,levelState)->{
            var camera=levelState.cameraRenderState.pos;var pos=state.pos();poses.pushPose();poses.translate(pos.getX()-camera.x,pos.getY()-camera.y,pos.getZ()-camera.z);
            collector.order(1).submitShapeOutline(poses,shape,RenderTypes.lines(),color,2.5F,false);poses.popPose();return true;
        });
    }
    private static int physicalCell(BlockDesign d,int cell){int[] xyz=DesignOrientation.cell(d.orientation(),d.x(cell),d.y(cell),d.z(cell),d.resolution());return d.index(xyz[0],xyz[1],xyz[2]);}
    private static VoxelShape shape(DesignOperations.Plan plan){
        if(!plan.cells().isEmpty()){
            var d=plan.before();int n=d.resolution();VoxelShape out=Shapes.empty();
            for(int cell:plan.cells()){int c=physicalCell(d,cell);double x=d.x(c)/(double)n,y=d.y(c)/(double)n,z=d.z(c)/(double)n;out=Shapes.joinUnoptimized(out,Shapes.box(x,y,z,x+1.0/n,y+1.0/n,z+1.0/n),BooleanOp.OR);}return out.optimize();
        }
        return !plan.faces().isEmpty()?surfaces(plan.before(),plan.faces()):Shapes.empty();
    }
    private static VoxelShape surfaces(BlockDesign d,List<DesignGeometry.Surface> faces){
        int n=d.resolution();double thin=.0005;VoxelShape out=Shapes.empty();
        for(var face:faces){int c=physicalCell(d,face.cell());var dir=DesignOrientation.direction(d.orientation(),face.face());
            double x=d.x(c)/(double)n,y=d.y(c)/(double)n,z=d.z(c)/(double)n;double[] min={x,y,z},max={x+1.0/n,y+1.0/n,z+1.0/n};int axis=dir.getAxis()==Direction.Axis.X?0:dir.getAxis()==Direction.Axis.Y?1:2;
            double plane=dir.getAxisDirection()==Direction.AxisDirection.POSITIVE?max[axis]:min[axis];min[axis]=plane-thin;max[axis]=plane+thin;
            out=Shapes.joinUnoptimized(out,Shapes.box(min[0],min[1],min[2],max[0],max[1],max[2]),BooleanOp.OR);
        }return out.optimize();
    }
}
