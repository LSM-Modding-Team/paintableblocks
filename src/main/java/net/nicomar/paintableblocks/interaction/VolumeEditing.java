package net.nicomar.paintableblocks.interaction;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.nicomar.paintableblocks.block.PaintableBlock;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.event.PaintableBlockEditEvent;
import net.nicomar.paintableblocks.item.*;
import net.nicomar.paintableblocks.network.ModNetworking;
import net.nicomar.paintableblocks.operation.*;
import net.nicomar.paintableblocks.registry.ModContent;

/** Bounded server-owned jobs. No chunk tickets, giant cell arrays, persistent jobs or client geometry. */
public final class VolumeEditing {
    public static final int FIRST=1,SCANNING=2,READY=3,RUNNING=4,CONFIG=5;
    private static final Map<ServerPlayer,Session> SESSIONS=new LinkedHashMap<>();
    private record ChunkKey(Level level,int x,int z){}
    private static final Map<ChunkKey,Set<ServerPlayer>> WATCHERS=new HashMap<>();
    private record PlanKey(VolumeRegion region,BlockPos pos,BlockDesign design,int direction,boolean upper,boolean former,boolean create,int resolution){}

    private static final net.nicomar.paintableblocks.performance.WorkCache<PlanKey,DesignOperations.Plan> PLANS=new net.nicomar.paintableblocks.performance.WorkCache<>("volume-plans",1024,262144);
    private static long nextId;
    private static int roundRobin;
    private static boolean checkingEvents;
    private static final int BUDGET=2048;
    private static final long NANOS=3_000_000;
    private static final class Session {
        long id=++nextId,cursor,modified,ignored,expected;
        final boolean former;final InteractionHand hand;final int slot;final ItemStack tool;final net.minecraft.resources.Identifier dimension;
        final BlockPos first;final BlockPos createAnchor;BlockPos last;VolumeRegion firstCell,region;VolumeCursor traversal;BlockPos writingPos,pendingPos;PlanWork pendingWork;
        final net.nicomar.paintableblocks.performance.WorkCache<PlanKey,DesignOperations.Plan> plans=PLANS;
        final Set<ChunkKey> watches=new HashSet<>();long sentCursor=-1,sentModified=-1;int sentPhase=-1;int phase=FIRST,direction,resolution=1;boolean upper,create,invalid,writing;
        Session(ServerPlayer p,ModNetworking.Use r,VolumeRegion cell,boolean former){this.former=former;hand=r.hand();slot=r.slot();tool=p.getItemInHand(hand).copy();dimension=r.dimension();first=r.pos();createAnchor=first.relative(r.face());last=first;firstCell=cell;region=cell;}
    }
    private static final class PlanWork {
        final Session session;final BlockPos pos;final BlockDesign before;final BlockDesign.ClipTask clip;final BitSet removed=new BitSet();int cell;
        PlanWork(Session s,BlockPos pos,BlockDesign before){session=s;this.pos=pos;this.before=before;clip=s.former?before.clipTask(s.region.plane(pos,before,s.direction,s.upper)):null;}
        DesignOperations.Plan advance(){
            BlockDesign after;
            if(clip!=null){if(!clip.advance(128))return null;after=clip.result();}
            else{int n=before.resolution(),end=Math.min(n*n*n,cell+128);for(;cell<end;cell++)if(n!=1&&before.occupied(cell)){var center=DesignOrientation.rotate(before.orientation(),new net.minecraft.world.phys.Vec3((before.x(cell)+.5)/n,(before.y(cell)+.5)/n,(before.z(cell)+.5)/n)).add(pos.getX(),pos.getY(),pos.getZ());if(session.region.contains(center))removed.set(cell);}if(cell<n*n*n)return null;after=before.remove(removed);}
            return new DesignOperations.Plan(session.create?before.remove(all(before.resolution())):before,after,removed.stream().boxed().toList(),List.of(),0);
        }
    }
    private static PlanKey key(Session s,BlockPos pos,BlockDesign design){return new PlanKey(s.region,pos,design,s.direction,s.upper,s.former,s.create,s.resolution);}
    private VolumeEditing(){}
    public static void register(){NeoForge.EVENT_BUS.addListener(VolumeEditing::tick);NeoForge.EVENT_BUS.addListener(VolumeEditing::unload);NeoForge.EVENT_BUS.addListener(VolumeEditing::placed);NeoForge.EVENT_BUS.addListener(VolumeEditing::broken);NeoForge.EVENT_BUS.addListener(VolumeEditing::stopped);}
    /** Observe only positions in the selected region; schedule chunk-worker callbacks on the server thread. */
    public static void changed(Level level,BlockPos pos){
        if(level==null||level.isClientSide())return;if(level.getServer()!=null&&!level.getServer().isSameThread()){var p=pos.immutable();level.getServer().execute(()->changed(level,p));return;}
        var players=WATCHERS.get(new ChunkKey(level,pos.getX()>>4,pos.getZ()>>4));if(players==null)return;
        for(var player:players){var s=SESSIONS.get(player);if(s!=null&&(!s.writing||!pos.equals(s.writingPos))&&watched(s,pos))s.invalid=true;}
    }
    private static void watch(ServerPlayer p,Session s){
        for(var key:s.watches){var set=WATCHERS.get(key);if(set!=null){set.remove(p);if(set.isEmpty())WATCHERS.remove(key);}}s.watches.clear();
        for(int z=s.region.min().getZ()>>4;z<=s.region.max().getZ()>>4;z++)for(int x=s.region.min().getX()>>4;x<=s.region.max().getX()>>4;x++){var key=new ChunkKey(p.level(),x,z);s.watches.add(key);WATCHERS.computeIfAbsent(key,k->new HashSet<>()).add(p);}
    }
    private static void unwatch(ServerPlayer p,Session s){for(var key:s.watches){var set=WATCHERS.get(key);if(set!=null){set.remove(p);if(set.isEmpty())WATCHERS.remove(key);}}s.watches.clear();}
    private static boolean watched(Session s,BlockPos pos){var lo=s.region.min();var hi=s.region.max();return pos.getX()>=lo.getX()&&pos.getX()<=hi.getX()&&pos.getY()>=lo.getY()&&pos.getY()<=hi.getY()&&pos.getZ()>=lo.getZ()&&pos.getZ()<=hi.getZ();}
    private static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent e){SESSIONS.clear();WATCHERS.clear();PLANS.clear();roundRobin=0;}
    private static void placed(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent e){if(!checkingEvents&&e.getLevel() instanceof Level l)changed(l,e.getPos());}
    private static void broken(BreakBlockEvent e){if(!checkingEvents&&e.getLevel() instanceof Level l)changed(l,e.getPos());}
    private static void unload(ChunkEvent.Unload e){if(!(e.getLevel() instanceof Level l)||l.isClientSide())return;var pos=e.getChunk().getPos();var players=WATCHERS.get(new ChunkKey(l,pos.x,pos.z));if(players!=null)for(var player:players){var s=SESSIONS.get(player);if(s!=null)s.invalid=true;}}
    private static boolean held(ServerPlayer p,Session s){return p.isAlive()&&!p.isSpectator()&&p.mayBuild()&&!p.isUsingItem()&&p.containerMenu==p.inventoryMenu&&p.level().dimension().identifier().equals(s.dimension)&&(s.hand!=InteractionHand.MAIN_HAND||p.getInventory().getSelectedSlot()==s.slot)&&(ItemStack.isSameItemSameComponents(s.tool,p.getItemInHand(s.hand))||(s.phase==RUNNING&&s.modified==s.expected&&s.tool.isEmpty()&&p.getItemInHand(s.hand).isEmpty()));}
    public static boolean allowed(ServerPlayer p,BlockPos pos,InteractionHand hand){return p.isAlive()&&!p.isSpectator()&&p.mayBuild()&&p.level().hasChunkAt(pos)&&!p.level().isOutsideBuildHeight(pos)&&p.level().getWorldBorder().isWithinBounds(pos)&&p.mayInteract(p.level(),pos)&&p.level().mayInteract(p,pos)&&p.mayUseItemAt(pos,Direction.UP,p.getItemInHand(hand));}
    private static void message(ServerPlayer p,String key,Object... args){p.sendOverlayMessage(Component.translatable("message.paintableblocks."+key,args));}
    private static void reply(ServerPlayer p,Session s){if(s.sentPhase==s.phase&&s.sentCursor==s.cursor&&s.sentModified==s.modified)return;s.sentPhase=s.phase;s.sentCursor=s.cursor;s.sentModified=s.modified;var r=s.region;PacketDistributor.sendToPlayer(p,new ModNetworking.VolumeState(s.id,s.phase,s.former,s.hand,s.dimension,s.first,s.last,r.x0(),r.y0(),r.z0(),r.x1(),r.y1(),r.z1(),s.direction,s.upper,s.create,s.resolution,s.cursor,s.modified,s.ignored,s.expected));}
    private static void reject(ServerPlayer p,Session s,String reason){
        if(s.former&&s.phase!=RUNNING){s.phase=CONFIG;s.cursor=0;s.expected=0;reply(p,s);message(p,reason,s.modified);}else stop(p,reason);
    }
    private static void stop(ServerPlayer p,String reason){var s=SESSIONS.remove(p);if(s==null)return;unwatch(p,s);s.phase=0;reply(p,s);message(p,reason,s.modified);}
    public static boolean use(ServerPlayer p,ModNetworking.Use r){
        var item=p.getItemInHand(r.hand());boolean former=item.getItem() instanceof SlopeFormerItem,hammer=item.getItem() instanceof HammerItem;var s=SESSIONS.get(p);
        if(!former&&!hammer)return false;if(!former&&s==null&&!r.control())return false;
        if(r.cancel()){if(s!=null)stop(p,"volume_stopped");return true;}
        if(s!=null&&(s.invalid||!held(p,s))){stop(p,"volume_invalid");return true;}
        if(s!=null&&(s.phase==SCANNING||s.phase==RUNNING))return true;
        if(s!=null&&s.phase==READY){if(!r.control())start(p,s);return true;}
        if(!former&&!r.control())return true;
        if(p.isUsingItem()||p.containerMenu!=p.inventoryMenu||!r.dimension().equals(p.level().dimension().identifier())||!BuiltInRegistries.ITEM.getKey(item.getItem()).equals(r.item())||(r.hand()==InteractionHand.MAIN_HAND&&r.slot()!=p.getInventory().getSelectedSlot())||!ServerInteractions.allowed(p,r.pos()))return true;
        var hit=ServerInteractions.hit(p);if(hit==null||!hit.getBlockPos().equals(r.pos())||hit.getDirection()!=r.face()||!Double.isFinite(r.point().x)||!Double.isFinite(r.point().y)||!Double.isFinite(r.point().z)||hit.getLocation().distanceToSqr(r.point())>1.0/256)return true;
        var interaction=NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,r.hand(),r.pos(),hit));if(interaction.isCanceled()||interaction.getUseItem()==net.minecraft.util.TriState.FALSE||interaction.getUseBlock()==net.minecraft.util.TriState.FALSE)return true;
        if(!(p.level().getBlockEntity(r.pos()) instanceof PaintableBlockEntity be)||!be.valid()||!be.editIdentity().equals(r.block())||be.revision()!=r.revision())return true;
        if(!ServerInteractions.allowed(p,r.pos())||p.getItemInHand(r.hand()).getItem()!=item.getItem()||be.revision()!=r.revision())return true;
        var target=DesignGeometry.target(be.design(),hit.getLocation().subtract(r.pos().getX(),r.pos().getY(),r.pos().getZ()),hit.getDirection());if(target.isEmpty())return true;
        var corner=former?VolumeRegion.block(r.pos()):VolumeRegion.cell(r.pos(),be.design(),target.get().cell());
        if(s==null){s=new Session(p,r,corner,former);SESSIONS.put(p,s);watch(p,s);reply(p,s);message(p,"volume_first");return true;}
        if(s.hand!=r.hand()||s.former!=former)return true;
        var region=s.firstCell.union(corner);if(!region.valid(former?16:256)){message(p,"volume_limit",former?16:256);return true;}
        s.region=region;watch(p,s);s.last=r.pos();if(former){s.phase=CONFIG;reply(p,s);}else scan(p,s);return true;
    }
    public static void action(Player player,ModNetworking.VolumeAction r){
        if(!(player instanceof ServerPlayer p))return;var s=SESSIONS.get(p);if(s==null||s.id!=r.id())return;
        if(r.action()==2){stop(p,"volume_stopped");return;}
        if(s.invalid||!held(p,s)){stop(p,"volume_invalid");return;}
        if(r.action()==1){if(s.phase==READY)start(p,s);return;}
        if(r.action()!=0||!s.former||s.phase==RUNNING||s.phase==FIRST||r.direction()<0||r.direction()>3||!net.nicomar.paintableblocks.balance.PaintingBalance.RESOLUTIONS.contains(r.resolution())||r.create()&&!p.isCreative())return;
        if(r.width()<1||r.width()>16||r.height()<1||r.height()>16||r.depth()<1||r.depth()>16)return;
        s.direction=r.direction();s.upper=r.upper();s.create=r.create();s.resolution=r.resolution();
        if(s.create){var min=s.createAnchor; s.region=new VolumeRegion(min.getX()*16,min.getY()*16,min.getZ()*16,(min.getX()+r.width())*16,(min.getY()+r.height())*16,(min.getZ()+r.depth())*16);}
        else s.region=VolumeRegion.block(s.first).union(VolumeRegion.block(s.last));
        watch(p,s);scan(p,s);
    }
    private static boolean chunks(ServerPlayer p,Session s){var lo=s.region.min();var hi=s.region.max();for(int z=lo.getZ()>>4;z<=hi.getZ()>>4;z++)for(int x=lo.getX()>>4;x<=hi.getX()>>4;x++)if(!p.level().hasChunkAt(new BlockPos(x*16,lo.getY(),z*16)))return false;return true;}
    private static void scan(ServerPlayer p,Session s){if(!chunks(p,s)){stop(p,"volume_unloaded");return;}s.pendingPos=null;s.pendingWork=null;s.sentPhase=-1;s.phase=SCANNING;s.traversal=new VolumeCursor(s.region,s.create);s.cursor=0;s.modified=0;s.ignored=0;s.expected=0;reply(p,s);message(p,"volume_warning");p.sendSystemMessage(Component.translatable("message.paintableblocks.volume_warning"));}
    private static void start(ServerPlayer p,Session s){if(s.invalid||!held(p,s)||!chunks(p,s)){stop(p,"volume_invalid");return;}
        var tool=p.getItemInHand(s.hand);if(s.former&&!p.isCreative()&&tool.getMaxDamage()-tool.getDamageValue()<s.expected){message(p,"volume_durability",s.expected);return;}
        s.pendingPos=null;s.pendingWork=null;s.phase=RUNNING;s.traversal=new VolumeCursor(s.region,s.create);s.cursor=0;s.modified=0;reply(p,s);
    }
    private static DesignOperations.Plan plan(ServerPlayer p,Session s,BlockPos pos){
        BlockDesign design=p.level().getBlockEntity(pos) instanceof PaintableBlockEntity be?be.design():BlockDesign.full(s.resolution);
        var key=key(s,pos,design);
        // No world-dependent permission, collision or validity checks are cached.
        if(s.create||!(p.level().getBlockEntity(pos) instanceof PaintableBlockEntity be)||!be.valid())return computePlan(p,s,pos);
        return s.plans.get(key,()->computePlan(p,s,pos),plan->plan.cells().size()+plan.before().paints().size()+plan.after().paints().size()+64);
    }
    private static DesignOperations.Plan computePlan(ServerPlayer p,Session s,BlockPos pos){
        if(!s.create&&p.level().getBlockEntity(pos) instanceof PaintableBlockEntity be){
            if(!be.valid())throw new IllegalArgumentException("Invalid design");var d=be.design();
            if(!s.former)return s.region.hammer(pos,d);
            var after=d.withSlope(s.region.plane(pos,d,s.direction,s.upper));return new DesignOperations.Plan(d,after,List.of(),List.of(),0);
        }
        if(s.create){
            if(!p.level().getBlockState(pos).isAir()||!p.level().getFluidState(pos).isEmpty())throw new IllegalArgumentException("Creation requires empty positions");
            var d=BlockDesign.full(s.resolution);var saved=s.plans.getIfPresent(key(s,pos,d));var after=saved==null?d.withSlope(s.region.plane(pos,d,s.direction,s.upper)):saved.after();
            if(after.empty())return null;
            var shape=DesignGeometry.shape(after).move(pos.getX(),pos.getY(),pos.getZ());if(!shape.isEmpty())for(var collision:p.level().getEntityCollisions(null,shape.bounds()))if(Shapes.joinIsNotEmpty(shape,collision,BooleanOp.AND))throw new IllegalArgumentException("Entity blocks creation");
            return new DesignOperations.Plan(d.remove(all(d.resolution())),after,List.of(),List.of(),0);
        }return null;
    }
    private static BitSet all(int n){var b=new BitSet();b.set(0,n*n*n);return b;}
    private static boolean events(ServerPlayer p,Session s,BlockPos pos,DesignOperations.Plan plan){
        checkingEvents=true;try{return checkEvents(p,s,pos,plan);}finally{checkingEvents=false;}
    }
    private static boolean checkEvents(ServerPlayer p,Session s,BlockPos pos,DesignOperations.Plan plan){
        if(s.create){
            var intended=ModContent.block(s.resolution).defaultBlockState().setValue(PaintableBlock.CUT,!plan.after().complete());
            var snapshot=net.neoforged.neoforge.common.util.BlockSnapshot.create(p.level().dimension(),p.level(),pos);
            var event=new net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent(snapshot,p.level().getBlockState(pos.below()),p){
                @Override public net.minecraft.world.level.block.state.BlockState getPlacedBlock(){return intended;}
                @Override public net.minecraft.world.level.block.state.BlockState getState(){return intended;}
            };
            if(NeoForge.EVENT_BUS.post(event).isCanceled())return false;
        }
        if(NeoForge.EVENT_BUS.post(new PaintableBlockEditEvent(p.level(),pos,p.level().getBlockState(pos),p,s.hand,plan)).isCanceled())return false;
        if((!s.create&&s.former)||plan.after().count()<plan.before().count())if(NeoForge.EVENT_BUS.post(new BreakBlockEvent(p.level(),pos,p.level().getBlockState(pos),p)).isCanceled())return false;return true;
    }
    private static void step(ServerPlayer p,Session s){
        if(!held(p,s)||s.invalid){stop(p,"volume_invalid");return;}
        BlockPos pos=s.pendingPos;
        if(pos==null){long previous=s.traversal.reviewed();try{pos=s.traversal.poll(p.level());}catch(IllegalStateException unloaded){stop(p,"volume_unloaded");return;}
            s.cursor=s.traversal.reviewed();if(s.phase==SCANNING)s.ignored+=s.cursor-previous-(pos==null?0:1);if(pos==null){if(s.traversal.done())finish(p,s);return;}s.pendingPos=pos;
        }
        if(!p.level().hasChunkAt(pos)){stop(p,"volume_unloaded");return;}
        var entity=p.level().getBlockEntity(pos);BlockDesign source=s.create?BlockDesign.full(s.resolution):entity instanceof PaintableBlockEntity be?be.design():BlockDesign.full(s.resolution);
        if((entity instanceof PaintableBlockEntity be&&be.valid())||s.create){
            var key=key(s,pos,source);if(s.plans.getIfPresent(key)==null){
                try{if(s.pendingWork==null)s.pendingWork=new PlanWork(s,pos,source);var built=s.pendingWork.advance();if(built==null)return;s.plans.get(key,()->built,v->v.cells().size()+v.before().paints().size()+v.after().paints().size()+64);s.pendingWork=null;}
                catch(IllegalArgumentException conflict){s.pendingPos=null;s.pendingWork=null;reject(p,s,"volume_conflict");return;}
            }
        }
        s.pendingPos=null;
        DesignOperations.Plan plan;
        try{plan=plan(p,s,pos);}catch(IllegalArgumentException bad){reject(p,s,"volume_conflict");return;}
        if(plan!=null&&plan.changes()){
            if(!allowed(p,pos,s.hand)||!events(p,s,pos,plan)||s.invalid||!held(p,s)){stop(p,"volume_denied");return;}
            // Listeners may mutate state. Recompute after events before committing.
            DesignOperations.Plan fresh;try{fresh=plan(p,s,pos);}catch(IllegalArgumentException bad){stop(p,"volume_invalid");return;}
            if(fresh==null||!fresh.equals(plan)){stop(p,"volume_invalid");return;}
            if(s.phase==SCANNING)s.expected++;
            else{
                s.writing=true;s.writingPos=pos;
                try{
                    if(p.level().getBlockEntity(pos) instanceof PaintableBlockEntity be)be.setDesign(plan.after());
                    else{var state=ModContent.block(s.resolution).defaultBlockState().setValue(PaintableBlock.CUT,!plan.after().complete());try(var placing=PlacementDesign.enter(pos,plan.after())){if(!p.level().setBlock(pos,state,Block.UPDATE_ALL)){stop(p,"volume_conflict");return;}}}
                    s.modified++;
                    if(s.former&&!p.isCreative()){var held=p.getItemInHand(s.hand);int damage=held.getDamageValue()+1;if(damage>=held.getMaxDamage()){held.shrink(1);s.tool.shrink(1);}else{held.setDamageValue(damage);s.tool.setDamageValue(damage);}}
                }finally{s.writing=false;s.writingPos=null;}
            }
        }else if(s.phase==SCANNING)s.ignored++;
    }
    private static void finish(ServerPlayer p,Session s){
        if(s.phase==SCANNING){s.phase=READY;reply(p,s);message(p,"volume_ready",s.expected,s.ignored);}else stop(p,"volume_done");
    }
    private static void tick(ServerTickEvent.Post event){
        for(var p:List.copyOf(SESSIONS.keySet())){var s=SESSIONS.get(p);if(event.getServer().getPlayerList().getPlayer(p.getUUID())!=p||!held(p,s)||s.invalid)stop(p,"volume_invalid");}
        long end=System.nanoTime()+NANOS;int budget=BUDGET;
        var active=SESSIONS.entrySet().stream().filter(e->e.getValue().phase==SCANNING||e.getValue().phase==RUNNING).toList();
        int idle=0;while(!active.isEmpty()&&budget-->0&&System.nanoTime()<end){var entry=active.get(Math.floorMod(roundRobin++,active.size()));var s=entry.getValue();if(SESSIONS.get(entry.getKey())!=s||s.phase!=SCANNING&&s.phase!=RUNNING){if(++idle>=active.size())break;continue;}idle=0;step(entry.getKey(),s);}
        if(event.getServer().getTickCount()%10==0)for(var e:SESSIONS.entrySet())if(e.getValue().phase==SCANNING||e.getValue().phase==RUNNING)reply(e.getKey(),e.getValue());
    }
}
