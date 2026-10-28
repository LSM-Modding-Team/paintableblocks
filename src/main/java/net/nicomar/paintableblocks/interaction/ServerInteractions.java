package net.nicomar.paintableblocks.interaction;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
import net.nicomar.paintableblocks.inventory.PaintTransfers;
import net.nicomar.paintableblocks.menu.PaintBucketMenu;
import net.nicomar.paintableblocks.network.ModNetworking;
import net.nicomar.paintableblocks.operation.DesignOperations;
import net.nicomar.paintableblocks.paint.*;
import net.nicomar.paintableblocks.registry.ModContent;
import net.nicomar.paintableblocks.event.PaintableBlockEditEvent;

/** Intent snapshots prevent stale clicks. Results, reach, permissions and inventory remain authoritative. */
public final class ServerInteractions {
    private record Pending(long id,BlockPos pos,Identifier dimension,UUID block,long revision,DesignGeometry.Surface first,InteractionHand hand,int slot,Identifier item,int paint){}
    private record Choice(Identifier dimension,BlockPos pos,int paint){}
    private static final Map<Player,Pending> PENDING=new WeakHashMap<>();
    private static final Map<Player,Choice> CHOICES=new WeakHashMap<>();
    private static final Map<Player,Long> LAST_USE=new WeakHashMap<>(),LAST_REQUEST=new WeakHashMap<>();
    private ServerInteractions(){}
    public static void register(){NeoForge.EVENT_BUS.addListener(ServerInteractions::tick);}
    public static boolean tool(ItemStack stack){return stack.getItem() instanceof EditingToolItem||stack.getItem() instanceof BrushItem;}
    public static boolean relevant(Player p,BlockPos pos){return p.level().hasChunkAt(pos)&&(p.level().getBlockEntity(pos) instanceof PaintableBlockEntity||p.level().getBlockEntity(pos) instanceof PaintBucketBlockEntity);}
    public static boolean allowed(Player p,BlockPos pos){
        return p instanceof ServerPlayer s&&p.isAlive()&&!p.isSpectator()&&p.mayBuild()&&s.level().hasChunkAt(pos)
                &&s.level().getWorldBorder().isWithinBounds(pos)&&p.isWithinBlockInteractionRange(pos,0)
                &&s.mayInteract(s.level(),pos)&&s.level().mayInteract(p,pos);
    }
    /** Stop the ray at the first unloaded chunk, rather than rejecting loaded targets before it. */
    private static Vec3 loadedEnd(ServerPlayer p,Vec3 eye,Vec3 end){
        double dx=end.x-eye.x,dz=end.z-eye.z;int cx=(int)Math.floor(eye.x/16),cz=(int)Math.floor(eye.z/16);
        if(!p.level().hasChunkAt(BlockPos.containing(eye)))return eye;
        int sx=dx>0?1:-1,sz=dz>0?1:-1;
        double tx=dx==0?Double.POSITIVE_INFINITY:((cx+(dx>0?1:0))*16-eye.x)/dx;
        double tz=dz==0?Double.POSITIVE_INFINITY:((cz+(dz>0?1:0))*16-eye.z)/dz;
        double stepX=dx==0?Double.POSITIVE_INFINITY:16/Math.abs(dx),stepZ=dz==0?Double.POSITIVE_INFINITY:16/Math.abs(dz);
        while(Math.min(tx,tz)<=1){double entry=Math.min(tx,tz);if(tx==entry){cx+=sx;tx+=stepX;}if(tz==entry){cz+=sz;tz+=stepZ;}
            if(!p.level().hasChunkAt(new BlockPos(cx*16,(int)Math.floor(eye.y),cz*16)))return eye.lerp(end,Math.max(0,entry-1e-7));}
        return end;
    }
    private static BlockHitResult hit(ServerPlayer p){
        var eye=p.getEyePosition();double range=p.blockInteractionRange();if(!Double.isFinite(range)||range<=0||range>128)return null;
        var end=loadedEnd(p,eye,eye.add(p.getViewVector(1).scale(range)));
        var hit=p.level().clip(new ClipContext(eye,end,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));
        return hit.getType()==HitResult.Type.BLOCK&&hit.getLocation().distanceToSqr(eye)<=range*range+1e-6?hit:null;
    }
    private static boolean heldMatches(Player p,InteractionHand hand,int slot,Identifier item,int paint){
        var held=p.getItemInHand(hand);if(!BuiltInRegistries.ITEM.getKey(held.getItem()).equals(item)||hand==InteractionHand.MAIN_HAND&&p.getInventory().getSelectedSlot()!=slot)return false;
        return held.getItem() instanceof BrushItem b?b.valid(held)&&b.content(held).paint()==paint:tool(held)&&paint==0;
    }
    private static boolean pendingValid(ServerPlayer p,Pending s){
        return s.dimension.equals(p.level().dimension().identifier())&&heldMatches(p,s.hand,s.slot,s.item,s.paint)&&allowed(p,s.pos)
                &&p.level().getBlockEntity(s.pos) instanceof PaintableBlockEntity be&&be.valid()&&be.editIdentity().equals(s.block)&&be.revision()==s.revision;
    }
    private static void reply(ServerPlayer p,long request){
        var s=PENDING.get(p);
        ModNetworking.selection(p,s==null?new ModNetworking.Selection(request,0,false,p.level().dimension().identifier(),InteractionHand.MAIN_HAND,0,Identifier.parse("minecraft:air"),0,BlockPos.ZERO,ModNetworking.NO_BLOCK,0,0,0):
                new ModNetworking.Selection(request,s.id,true,s.dimension,s.hand,s.slot,s.item,s.paint,s.pos,s.block,s.revision,s.first.cell(),s.first.face().ordinal()));
    }
    private static void tick(PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p){var pending=PENDING.get(p);if(pending!=null&&!pendingValid(p,pending)){PENDING.remove(p);reply(p,LAST_REQUEST.getOrDefault(p,0L));}}}
    public static void use(Player player,ModNetworking.Use request){
        if(!(player instanceof ServerPlayer p)||request.request()<=0||request.request()<=LAST_REQUEST.getOrDefault(p,0L))return;
        LAST_REQUEST.put(p,request.request());
        try{dispatch(p,request);}finally{reply(p,request.request());}
    }
    public static void nativeUse(UseOnContext context){
        if(!(context.getPlayer() instanceof ServerPlayer p))return;var pending=PENDING.get(p);
        var nativeHit=new BlockHitResult(context.getClickLocation(),context.getClickedFace(),context.getClickedPos(),false);
        var request=ModNetworking.Use.capture(p,context.getHand(),nativeHit,0,pending==null?0:pending.id,false,context.isSecondaryUseActive(),false);
        try{dispatch(p,request);}finally{reply(p,LAST_REQUEST.getOrDefault(p,0L));}
    }
    private static boolean matchesRequest(ServerPlayer p,ModNetworking.Use r){
        if(!r.dimension().equals(p.level().dimension().identifier())||!heldMatches(p,r.hand(),r.slot(),r.item(),r.paint())||!allowed(p,r.pos())||!p.mayUseItemAt(r.pos(),r.face(),p.getItemInHand(r.hand())))return false;
        if(p.level().getBlockEntity(r.pos()) instanceof PaintableBlockEntity be)return be.editIdentity().equals(r.block())&&be.revision()==r.revision();
        return r.block().equals(ModNetworking.NO_BLOCK)&&p.level().getBlockEntity(r.pos()) instanceof PaintBucketBlockEntity;
    }
    private static void dispatch(ServerPlayer p,ModNetworking.Use r){
        var pending=PENDING.get(p);
        if(r.cancel()){if(pending!=null&&r.selection()==pending.id)PENDING.remove(p);return;}
        // Invalidating a confirmation must consume the click, never turn it into single-face painting.
        if(pending!=null&&!pendingValid(p,pending)){PENDING.remove(p);return;}
        if((pending==null?0:pending.id)!=r.selection())return;
        if(pending!=null&&(pending.hand!=r.hand()||pending.slot!=r.slot()||!pending.item.equals(r.item())||pending.paint!=r.paint())){PENDING.remove(p);return;}
        if(p.isUsingItem()||p.containerMenu!=p.inventoryMenu||!matchesRequest(p,r))return;
        long now=p.level().getGameTime();if(LAST_USE.getOrDefault(p,Long.MIN_VALUE)==now)return;LAST_USE.put(p,now);
        if(r.control()&&pending!=null){PENDING.remove(p);return;}
        var hit=hit(p);
        if(hit==null||!hit.getBlockPos().equals(r.pos())||hit.getDirection()!=r.face()||!Double.isFinite(r.point().x)||!Double.isFinite(r.point().y)||!Double.isFinite(r.point().z)||hit.getLocation().distanceToSqr(r.point())>1.0/256)return;
        if(r.request()!=0){var permission=NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,r.hand(),r.pos(),hit));if(permission.isCanceled()||permission.getUseItem()==net.minecraft.util.TriState.FALSE||permission.getUseBlock()==net.minecraft.util.TriState.FALSE)return;}
        if(!matchesRequest(p,r))return;
        var pos=r.pos();var held=p.getItemInHand(r.hand());
        if(p.level().getBlockEntity(pos) instanceof PaintBucketBlockEntity){bucket(p,pos,r.hand(),r.reverse());return;}
        if(!(p.level().getBlockEntity(pos) instanceof PaintableBlockEntity be))return;
        if(!be.valid()){p.sendOverlayMessage(Component.translatable("message.paintableblocks.invalid_design"));return;}
        var before=be.design();var surface=DesignGeometry.target(before,hit.getLocation().subtract(pos.getX(),pos.getY(),pos.getZ()),hit.getDirection());
        var expected=DesignGeometry.target(before,r.point().subtract(pos.getX(),pos.getY(),pos.getZ()),r.face());
        if(surface.isEmpty()||!surface.equals(expected))return;
        DesignOperations.Plan plan;
        if(held.getItem() instanceof BrushItem brush){
            var content=brush.content(held);int id=content.paint();
            if(!PaintCatalog.validId(id)||!p.isCreative()&&!PaintCatalog.byId(id).survivalAvailable())return;
            if(r.control()){
                PENDING.put(p,new Pending(r.request(),pos.immutable(),r.dimension(),be.editIdentity(),be.revision(),surface.get(),r.hand(),r.slot(),r.item(),id));return;
            }
            if(pending!=null&&!pending.pos.equals(pos))return;
            List<DesignGeometry.Surface> faces;
            try{faces=pending==null?List.of(surface.get()):DesignOperations.area(before,pending.first,surface.get());}catch(IllegalArgumentException invalid){return;}
            plan=DesignOperations.paint(before,faces,id);
            if(!p.isCreative()&&content.units()<plan.units()){p.sendOverlayMessage(Component.translatable("message.paintableblocks.insufficient_paint",plan.units(),content.units()));return;}
        }else if(held.getItem() instanceof EditingToolItem tool)plan=DesignOperations.tool(before,surface.get(),tool.action(),hit.getDirection(),r.reverse());
        else return;
        if(!plan.changes()){if(pending!=null)PENDING.remove(p);return;}
        if(NeoForge.EVENT_BUS.post(new PaintableBlockEditEvent(p.level(),pos,be.getBlockState(),p,r.hand(),plan)).isCanceled())return;
        if(plan.after().count()<before.count()&&NeoForge.EVENT_BUS.post(new BreakBlockEvent(p.level(),pos,be.getBlockState(),p)).isCanceled())return;
        if(!matchesRequest(p,r)||p.level().getBlockEntity(pos)!=be||!be.valid()||!be.design().equals(before)||p.isUsingItem()||p.containerMenu!=p.inventoryMenu)return;
        var latest=hit(p);if(latest==null||!latest.getBlockPos().equals(pos)||latest.getDirection()!=hit.getDirection()||latest.getLocation().distanceToSqr(hit.getLocation())>1e-8)return;
        if(held.getItem() instanceof RotationWrenchItem){
            var shape=DesignGeometry.shape(plan.after()).move(pos.getX(),pos.getY(),pos.getZ());
            for(var collision:p.level().getEntityCollisions(null,shape.bounds()))if(Shapes.joinIsNotEmpty(shape,collision,BooleanOp.AND))return;
        }
        var current=p.getItemInHand(r.hand());
        if(current.getItem() instanceof BrushItem brush){var content=brush.content(current);if(!brush.valid(current)||!p.isCreative()&&(!PaintCatalog.byId(r.paint()).survivalAvailable()||content.units()<plan.units()))return;if(!p.isCreative())current.set(ModContent.BRUSH_PAINT.get(),content.withUnits(content.units()-plan.units()));}
        be.setDesign(plan.after());if(pending!=null)PENDING.remove(p);
    }
    public static int selected(Player p,PaintBucketBlockEntity be){
        var choice=CHOICES.get(p);if(choice!=null&&choice.dimension.equals(p.level().dimension().identifier())&&choice.pos.equals(be.getBlockPos())&&be.storage().units(choice.paint)>0)return choice.paint;
        return be.storage().entries().isEmpty()?0:be.storage().entries().getFirst().paint();
    }
    public static void select(Player p,PaintBucketBlockEntity be,int id){if(be.storage().units(id)>0)CHOICES.put(p,new Choice(p.level().dimension().identifier(),be.getBlockPos(),id));}
    public static void bucket(Player p,BlockPos pos,boolean reverse){bucket(p,pos,InteractionHand.MAIN_HAND,reverse);}
    public static void bucket(Player p,BlockPos pos,InteractionHand hand,boolean reverse){
        if(!allowed(p,pos)||!(p.level().getBlockEntity(pos) instanceof PaintBucketBlockEntity be)||!be.valid())return;
        var held=p.getItemInHand(hand);
        if(held.getItem() instanceof BrushItem brush){
            if(!brush.valid(held))return;var old=brush.content(held);var result=reverse?PaintTransfers.giveBack(old,be.storage(),be.tier()):PaintTransfers.load(old,brush.material().brushCapacity,be.storage(),selected(p,be),p.isCreative());
            if(result.moved()==0&&result.brush().equals(old))((ServerPlayer)p).sendOverlayMessage(Component.translatable("message.paintableblocks.no_transfer"));
            be.setStorage(result.storage());held.set(ModContent.BRUSH_PAINT.get(),result.brush());PENDING.remove(p);
        }else if(held.is(ModContent.PAINT.get())){if(PaintBucketMenu.deposit(be,held)==0)((ServerPlayer)p).sendOverlayMessage(Component.translatable("message.paintableblocks.no_transfer"));}
        else PaintBucketBlock.open(p,pos);
    }
}
