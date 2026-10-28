package net.nicomar.paintableblocks.interaction;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.nicomar.paintableblocks.block.PaintBucketBlock;
import net.nicomar.paintableblocks.blockentity.*;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.item.*;
import net.nicomar.paintableblocks.inventory.*;
import net.nicomar.paintableblocks.menu.PaintBucketMenu;
import net.nicomar.paintableblocks.network.ModNetworking;
import net.nicomar.paintableblocks.operation.DesignOperations;
import net.nicomar.paintableblocks.paint.*;
import net.nicomar.paintableblocks.registry.ModContent;

/** Client packets contain intent only. Geometry, reach, inventory and plans come from the server. */
public final class ServerInteractions {
    private record Pending(BlockPos pos,ResourceKey<Level> dimension,BlockDesign design,DesignGeometry.Surface first,ItemStack stack,int paint){}
    private record Choice(ResourceKey<Level> dimension,BlockPos pos,int paint){}
    private static final ThreadLocal<Boolean> CHECKING_USE=ThreadLocal.withInitial(()->false);
    private static final Map<Player,Pending> PENDING=new WeakHashMap<>();
    private static final Map<Player,Choice> CHOICES=new WeakHashMap<>();
    private static final Map<Player,Long> LAST_USE=new WeakHashMap<>();
    private ServerInteractions(){}
    public static void register(){NeoForge.EVENT_BUS.addListener(ServerInteractions::tick);NeoForge.EVENT_BUS.addListener(ServerInteractions::interact);}
    public static boolean tool(ItemStack stack){return stack.getItem() instanceof EditingToolItem||stack.getItem() instanceof BrushItem;}
    public static boolean allowed(Player p,BlockPos pos){
        return p instanceof ServerPlayer s&&p.isAlive()&&!p.isSpectator()&&p.mayBuild()&&s.level().hasChunkAt(pos)
                &&s.level().getWorldBorder().isWithinBounds(pos)&&p.isWithinBlockInteractionRange(pos,0)
                &&s.mayInteract(s.level(),pos)&&s.level().mayInteract(p,pos)&&p.mayUseItemAt(pos,net.minecraft.core.Direction.UP,p.getMainHandItem());
    }
    private static void interact(PlayerInteractEvent.RightClickBlock e){
        if(CHECKING_USE.get()||e.getLevel().isClientSide()||!tool(e.getEntity().getMainHandItem()))return;
        // Native useOn remains a fallback; the client intent packet is the sole path for Ctrl-aware actions.
        e.setCanceled(true);e.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
    }
    private static BlockHitResult hit(ServerPlayer p){
        var eye=p.getEyePosition();double range=p.blockInteractionRange();if(!Double.isFinite(range)||range<0||range>128)return null;
        var end=eye.add(p.getViewVector(1).scale(range));
        for(int i=0;i<=Math.ceil(range*2);i++)if(!p.level().hasChunkAt(BlockPos.containing(eye.lerp(end,i/Math.max(1.0,Math.ceil(range*2))))))return null;
        var hit=p.level().clip(new ClipContext(eye,end,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));
        return hit.getType()==HitResult.Type.BLOCK&&hit.getLocation().distanceToSqr(eye)<=range*range+1e-6?hit:null;
    }
    private static void clear(ServerPlayer p){if(PENDING.remove(p)!=null)ModNetworking.clear(p);}
    private static boolean pendingValid(ServerPlayer p,Pending s){
        return s.dimension.equals(p.level().dimension())&&s.stack==p.getMainHandItem()&&s.stack.getItem() instanceof BrushItem b&&b.valid(s.stack)
                &&b.content(s.stack).paint()==s.paint&&allowed(p,s.pos)&&p.level().getBlockEntity(s.pos) instanceof PaintableBlockEntity be&&be.valid()&&be.design().equals(s.design);
    }
    private static void tick(PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p){var pending=PENDING.get(p);if(pending!=null&&!pendingValid(p,pending))clear(p);}}
    public static void use(Player player,boolean control,boolean reverse,int action){
        if(!(player instanceof ServerPlayer p))return;
        if(action==1){clear(p);return;}
        if(!tool(p.getMainHandItem())){clear(p);return;}
        long now=p.level().getGameTime();if(LAST_USE.getOrDefault(p,Long.MIN_VALUE)==now)return;LAST_USE.put(p,now);
        var pending=PENDING.get(p);if(pending!=null&&!pendingValid(p,pending)){clear(p);pending=null;}
        if(control&&pending!=null){clear(p);return;}
        var hit=hit(p);if(hit==null||!allowed(p,hit.getBlockPos())||!p.mayUseItemAt(hit.getBlockPos(),hit.getDirection(),p.getMainHandItem()))return;
        CHECKING_USE.set(true);
        try{var permission=NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,net.minecraft.world.InteractionHand.MAIN_HAND,hit.getBlockPos(),hit));if(permission.isCanceled()||(permission.getUseItem()==net.minecraft.util.TriState.FALSE||permission.getUseBlock()==net.minecraft.util.TriState.FALSE))return;}
        finally{CHECKING_USE.remove();}
        var pos=hit.getBlockPos();var held=p.getMainHandItem();
        if(p.level().getBlockEntity(pos) instanceof PaintBucketBlockEntity){bucket(p,pos,reverse);return;}
        if(!(p.level().getBlockEntity(pos) instanceof PaintableBlockEntity be)||!be.valid())return;
        BlockDesign before=be.design();var surface=DesignGeometry.target(before,hit.getLocation().subtract(pos.getX(),pos.getY(),pos.getZ()),hit.getDirection());if(surface.isEmpty())return;
        DesignOperations.Plan plan;
        if(held.getItem() instanceof BrushItem brush){
            if(!brush.valid(held))return;PaintContent content=brush.content(held);int id=content.paint();
            if(!PaintCatalog.validId(id)||(!p.isCreative()&&!PaintCatalog.byId(id).survivalAvailable()))return;
            if(control){PENDING.put(p,new Pending(pos.immutable(),p.level().dimension(),before,surface.get(),held,id));ModNetworking.selection(p,pos,surface.get().cell(),surface.get().face().ordinal());return;}
            if(pending!=null&&!pending.pos.equals(pos))return;
            List<DesignGeometry.Surface> faces;
            try{faces=pending==null?List.of(surface.get()):DesignOperations.area(before,pending.first,surface.get());}catch(IllegalArgumentException invalid){return;}
            plan=DesignOperations.paint(before,faces,id);
            if(!p.isCreative()&&content.units()<plan.units()){p.sendOverlayMessage(Component.translatable("message.paintableblocks.insufficient_paint",plan.units(),content.units()));return;}
        }else if(held.getItem() instanceof EditingToolItem tool)plan=DesignOperations.tool(before,surface.get(),tool.action(),hit.getDirection(),reverse);
        else return;
        if(!plan.changes()){if(pending!=null)clear(p);return;}
        var event=NeoForge.EVENT_BUS.post(new BreakBlockEvent(p.level(),pos,be.getBlockState(),p));if(event.isCanceled())return;
        // A protection listener may have changed the block, held item, mode or paint.
        if(!allowed(p,pos)||p.getMainHandItem()!=held||p.level().getBlockEntity(pos)!=be||!be.design().equals(before))return;
        var latest=hit(p);if(latest==null||!latest.getBlockPos().equals(pos)||latest.getDirection()!=hit.getDirection()||latest.getLocation().distanceToSqr(hit.getLocation())>1e-8)return;
        if(held.getItem() instanceof BrushItem brush){
            if(!brush.valid(held))return;var content=brush.content(held);int intended=plan.after().paint(surface.get().cell(),surface.get().face());
            if(content.paint()!=intended||(!p.isCreative()&&(!PaintCatalog.byId(intended).survivalAvailable()||content.units()<plan.units())))return;
            if(!p.isCreative())held.set(ModContent.BRUSH_PAINT.get(),content.withUnits(content.units()-plan.units()));
        }
        if(held.getItem() instanceof RotationWrenchItem){
            var shape=DesignGeometry.shape(plan.after()).move(pos.getX(),pos.getY(),pos.getZ());
            for(var collision:p.level().getEntityCollisions(null,shape.bounds()))if(Shapes.joinIsNotEmpty(shape,collision,BooleanOp.AND))return;
        }
        be.setDesign(plan.after());if(pending!=null)clear(p);
    }
    public static int selected(Player p,PaintBucketBlockEntity be){
        var choice=CHOICES.get(p);if(choice!=null&&choice.dimension.equals(p.level().dimension())&&choice.pos.equals(be.getBlockPos())&&be.storage().units(choice.paint)>0)return choice.paint;
        return be.storage().entries().isEmpty()?0:be.storage().entries().getFirst().paint();
    }
    public static void select(Player p,PaintBucketBlockEntity be,int id){if(be.storage().units(id)>0)CHOICES.put(p,new Choice(p.level().dimension(),be.getBlockPos(),id));}
    public static void bucket(Player p,BlockPos pos,boolean reverse){
        if(!allowed(p,pos)||!(p.level().getBlockEntity(pos) instanceof PaintBucketBlockEntity be)||!be.valid())return;
        var held=p.getMainHandItem();
        if(held.getItem() instanceof BrushItem brush){
            if(!brush.valid(held))return;var old=brush.content(held);var result=reverse?PaintTransfers.giveBack(old,be.storage(),be.tier()):PaintTransfers.load(old,brush.material().brushCapacity,be.storage(),selected(p,be),p.isCreative());
            if(result.moved()==0&&result.brush().equals(old))((ServerPlayer)p).sendOverlayMessage(Component.translatable("message.paintableblocks.no_transfer"));
            be.setStorage(result.storage());held.set(ModContent.BRUSH_PAINT.get(),result.brush());clear((ServerPlayer)p);
        }else if(held.is(ModContent.PAINT.get())){if(PaintBucketMenu.deposit(be,held)==0)((ServerPlayer)p).sendOverlayMessage(Component.translatable("message.paintableblocks.no_transfer"));}
        else PaintBucketBlock.open(p,pos);
    }
}
