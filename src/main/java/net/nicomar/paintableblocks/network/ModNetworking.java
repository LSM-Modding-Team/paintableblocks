package net.nicomar.paintableblocks.network;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nicomar.paintableblocks.interaction.ServerInteractions;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.item.BrushItem;

public final class ModNetworking {
    public static final UUID NO_BLOCK=new UUID(0,0);
    private ModNetworking(){}
    /** Expected state is a stale-click guard; the server still ray-traces and computes the result. */
    public record Use(long request,long selection,InteractionHand hand,int slot,Identifier dimension,BlockPos pos,
            Vec3 point,Direction face,Identifier item,int paint,UUID block,long revision,boolean control,boolean reverse,boolean cancel) implements CustomPacketPayload {
        public static final Type<Use> TYPE=new Type<>(Identifier.fromNamespaceAndPath("paintableblocks","use_tool"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Use> CODEC=StreamCodec.of((b,v)->{
            b.writeLong(v.request);b.writeLong(v.selection);b.writeEnum(v.hand);b.writeVarInt(v.slot);b.writeIdentifier(v.dimension);b.writeBlockPos(v.pos);
            b.writeDouble(v.point.x);b.writeDouble(v.point.y);b.writeDouble(v.point.z);b.writeEnum(v.face);b.writeIdentifier(v.item);b.writeVarInt(v.paint);
            b.writeUUID(v.block);b.writeLong(v.revision);b.writeBoolean(v.control);b.writeBoolean(v.reverse);b.writeBoolean(v.cancel);
        },b->new Use(b.readLong(),b.readLong(),b.readEnum(InteractionHand.class),b.readVarInt(),b.readIdentifier(),b.readBlockPos(),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readEnum(Direction.class),b.readIdentifier(),b.readVarInt(),b.readUUID(),b.readLong(),b.readBoolean(),b.readBoolean(),b.readBoolean()));
        @Override public Type<Use> type(){return TYPE;}
        public static Use capture(Player p,InteractionHand hand,BlockHitResult hit,long request,long selection,boolean control,boolean reverse,boolean cancel){
            var held=p.getItemInHand(hand);int paint=held.getItem() instanceof BrushItem b&&b.valid(held)?b.content(held).paint():0;
            var entity=p.level().getBlockEntity(hit.getBlockPos());var block=entity instanceof PaintableBlockEntity be?be.editIdentity():NO_BLOCK;
            long revision=entity instanceof PaintableBlockEntity be?be.revision():0;
            return new Use(request,selection,hand,hand==InteractionHand.MAIN_HAND?p.getInventory().getSelectedSlot():-1,p.level().dimension().identifier(),hit.getBlockPos().immutable(),hit.getLocation(),hit.getDirection(),BuiltInRegistries.ITEM.getKey(held.getItem()),paint,block,revision,control,reverse,cancel);
        }
    }
    public record Selection(long request,long selection,boolean active,Identifier dimension,InteractionHand hand,int slot,Identifier item,int paint,
            BlockPos pos,UUID block,long revision,int cell,int face) implements CustomPacketPayload {
        public static final Type<Selection> TYPE=new Type<>(Identifier.fromNamespaceAndPath("paintableblocks","selection"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Selection> CODEC=StreamCodec.of((b,v)->{
            b.writeLong(v.request);b.writeLong(v.selection);b.writeBoolean(v.active);b.writeIdentifier(v.dimension);b.writeEnum(v.hand);b.writeVarInt(v.slot);b.writeIdentifier(v.item);b.writeVarInt(v.paint);
            b.writeBlockPos(v.pos);b.writeUUID(v.block);b.writeLong(v.revision);b.writeVarInt(v.cell);b.writeVarInt(v.face);
        },b->new Selection(b.readLong(),b.readLong(),b.readBoolean(),b.readIdentifier(),b.readEnum(InteractionHand.class),b.readVarInt(),b.readIdentifier(),b.readVarInt(),b.readBlockPos(),b.readUUID(),b.readLong(),b.readVarInt(),b.readVarInt()));
        @Override public Type<Selection> type(){return TYPE;}
    }
    /** Explicit int codec avoids depending on native menu-button ID width. */
    public record BucketAction(int container,int button) implements CustomPacketPayload {
        public static final Type<BucketAction> TYPE=new Type<>(Identifier.fromNamespaceAndPath("paintableblocks","bucket_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,BucketAction> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.container);b.writeVarInt(v.button);},b->new BucketAction(b.readVarInt(),b.readVarInt()));
        @Override public Type<BucketAction> type(){return TYPE;}
    }
    public record ProductionAction(int container,int action,int variant,int expectedResolution,int expectedMaterial) implements CustomPacketPayload {
        public static final Type<ProductionAction> TYPE=new Type<>(Identifier.fromNamespaceAndPath("paintableblocks","production_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,ProductionAction> CODEC=StreamCodec.of((b,v)->{b.writeVarInt(v.container);b.writeVarInt(v.action);b.writeVarInt(v.variant);b.writeVarInt(v.expectedResolution);b.writeVarInt(v.expectedMaterial);},b->new ProductionAction(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt()));
        @Override public Type<ProductionAction> type(){return TYPE;}
    }
    /** No client-supplied geometry: only configuration of a server-owned selection. */
    public record VolumeAction(long id,int action,int direction,boolean upper,boolean create,int width,int height,int depth,int resolution) implements CustomPacketPayload {
        public static final Type<VolumeAction> TYPE=new Type<>(Identifier.fromNamespaceAndPath("paintableblocks","volume_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,VolumeAction> CODEC=StreamCodec.of((b,v)->{b.writeLong(v.id);b.writeVarInt(v.action);b.writeVarInt(v.direction);b.writeBoolean(v.upper);b.writeBoolean(v.create);b.writeVarInt(v.width);b.writeVarInt(v.height);b.writeVarInt(v.depth);b.writeVarInt(v.resolution);},b->new VolumeAction(b.readLong(),b.readVarInt(),b.readVarInt(),b.readBoolean(),b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt()));
        @Override public Type<VolumeAction> type(){return TYPE;}
    }
    public record VolumeState(long id,int phase,boolean former,InteractionHand hand,Identifier dimension,BlockPos first,BlockPos last,
            int x0,int y0,int z0,int x1,int y1,int z1,int direction,boolean upper,boolean create,int resolution,long reviewed,long modified,long ignored,long affected) implements CustomPacketPayload {
        public static final Type<VolumeState> TYPE=new Type<>(Identifier.fromNamespaceAndPath("paintableblocks","volume_state"));
        public static final StreamCodec<RegistryFriendlyByteBuf,VolumeState> CODEC=StreamCodec.of((b,v)->{b.writeLong(v.id);b.writeVarInt(v.phase);b.writeBoolean(v.former);b.writeEnum(v.hand);b.writeIdentifier(v.dimension);b.writeBlockPos(v.first);b.writeBlockPos(v.last);b.writeInt(v.x0);b.writeInt(v.y0);b.writeInt(v.z0);b.writeInt(v.x1);b.writeInt(v.y1);b.writeInt(v.z1);b.writeVarInt(v.direction);b.writeBoolean(v.upper);b.writeBoolean(v.create);b.writeVarInt(v.resolution);b.writeVarLong(v.reviewed);b.writeVarLong(v.modified);b.writeVarLong(v.ignored);b.writeVarLong(v.affected);},b->new VolumeState(b.readLong(),b.readVarInt(),b.readBoolean(),b.readEnum(InteractionHand.class),b.readIdentifier(),b.readBlockPos(),b.readBlockPos(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readVarInt(),b.readBoolean(),b.readBoolean(),b.readVarInt(),b.readVarLong(),b.readVarLong(),b.readVarLong(),b.readVarLong()));
        @Override public Type<VolumeState> type(){return TYPE;}
    }
    public record DesignRequest(Identifier dimension,BlockPos pos) implements CustomPacketPayload {
        public static final Type<DesignRequest> TYPE=new Type<>(Identifier.fromNamespaceAndPath("paintableblocks","design_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf,DesignRequest> CODEC=StreamCodec.of((b,v)->{b.writeIdentifier(v.dimension);b.writeBlockPos(v.pos);},b->new DesignRequest(b.readIdentifier(),b.readBlockPos()));
        @Override public Type<DesignRequest> type(){return TYPE;}
    }
    public record DesignSync(Identifier dimension,BlockPos pos,UUID block,long before,long after,net.nicomar.paintableblocks.design.BlockDesign full,
            java.util.List<Long> removed,java.util.Map<Integer,Integer> paints,int orientation,net.nicomar.paintableblocks.design.SlopePlane plane) implements CustomPacketPayload {
        public static final Type<DesignSync> TYPE=new Type<>(Identifier.fromNamespaceAndPath("paintableblocks","design_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf,DesignSync> CODEC=StreamCodec.of((b,v)->{
            b.writeIdentifier(v.dimension);b.writeBlockPos(v.pos);b.writeUUID(v.block);b.writeLong(v.before);b.writeLong(v.after);b.writeBoolean(v.full!=null);
            if(v.full!=null){DesignWireCodec.write(b,v.full);return;}
            b.writeVarInt(v.removed.size());for(long word:v.removed)b.writeLong(word);b.writeVarInt(v.paints.size());for(var e:v.paints.entrySet()){b.writeVarInt(e.getKey());b.writeVarInt(e.getValue());}b.writeVarInt(v.orientation);b.writeBoolean(v.plane!=null);if(v.plane!=null)DesignWireCodec.plane(b,v.plane);
        },b->{
            var dimension=b.readIdentifier();var pos=b.readBlockPos();var block=b.readUUID();long before=b.readLong(),after=b.readLong();
            if(b.readBoolean())return new DesignSync(dimension,pos,block,before,after,DesignWireCodec.read(b),java.util.List.of(),java.util.Map.of(),0,null);
            int count=b.readVarInt();if(count<0||count>64)throw new IllegalArgumentException("Invalid occupation delta");var removed=new java.util.ArrayList<Long>();for(int i=0;i<count;i++)removed.add(b.readLong());
            count=b.readVarInt();if(count<0||count>1024)throw new IllegalArgumentException("Invalid paint delta");var paints=new java.util.TreeMap<Integer,Integer>();for(int i=0;i<count;i++){int key=b.readVarInt(),id=b.readVarInt();if(paints.put(key,id)!=null)throw new IllegalArgumentException("Duplicate paint delta");}
            int orientation=b.readVarInt();var plane=b.readBoolean()?DesignWireCodec.plane(b):null;
            return new DesignSync(dimension,pos,block,before,after,null,java.util.List.copyOf(removed),java.util.Map.copyOf(paints),orientation,plane);
        });
        @Override public Type<DesignSync> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent event){
        var registrar=event.registrar("6");
        registrar.playToServer(Use.TYPE,Use.CODEC,(v,ctx)->ServerInteractions.use(ctx.player(),v));
        registrar.playToClient(Selection.TYPE,Selection.CODEC);
        registrar.playToClient(VolumeState.TYPE,VolumeState.CODEC);
        registrar.playToClient(DesignSync.TYPE,DesignSync.CODEC);
        registrar.playToServer(DesignRequest.TYPE,DesignRequest.CODEC,(v,ctx)->DesignUpdates.resync(ctx.player(),v));
        registrar.playToServer(VolumeAction.TYPE,VolumeAction.CODEC,(v,ctx)->net.nicomar.paintableblocks.interaction.VolumeEditing.action(ctx.player(),v));
        registrar.playToServer(ProductionAction.TYPE,ProductionAction.CODEC,(v,ctx)->{var p=ctx.player();if(p.containerMenu instanceof net.nicomar.paintableblocks.menu.WorkstationMenu menu&&menu.containerId==v.container)menu.action(p,v.action,v.variant,v.expectedResolution,v.expectedMaterial);});
        registrar.playToServer(BucketAction.TYPE,BucketAction.CODEC,(v,ctx)->{
            var player=ctx.player();if(player.containerMenu instanceof net.nicomar.paintableblocks.menu.PaintBucketMenu menu&&menu.containerId==v.container)menu.clickMenuButton(player,v.button);
        });
    }
    public static void selection(ServerPlayer player,Selection response){PacketDistributor.sendToPlayer(player,response);}
}
