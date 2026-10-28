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
    public static void register(RegisterPayloadHandlersEvent event){
        var registrar=event.registrar("3");
        registrar.playToServer(Use.TYPE,Use.CODEC,(v,ctx)->ServerInteractions.use(ctx.player(),v));
        registrar.playToClient(Selection.TYPE,Selection.CODEC);
        registrar.playToServer(BucketAction.TYPE,BucketAction.CODEC,(v,ctx)->{
            var player=ctx.player();if(player.containerMenu instanceof net.nicomar.paintableblocks.menu.PaintBucketMenu menu&&menu.containerId==v.container)menu.clickMenuButton(player,v.button);
        });
    }
    public static void selection(ServerPlayer player,Selection response){PacketDistributor.sendToPlayer(player,response);}
}
