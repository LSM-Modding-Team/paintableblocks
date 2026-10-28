package net.nicomar.paintableblocks.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import net.nicomar.paintableblocks.interaction.ServerInteractions;

public final class ModNetworking {
    private ModNetworking(){}
    public record Use(boolean control,boolean reverse,boolean cancel) implements CustomPacketPayload {
        public static final Type<Use> TYPE=new Type<>(Identifier.fromNamespaceAndPath("paintableblocks","use_tool"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Use> CODEC=StreamCodec.of((b,v)->{b.writeBoolean(v.control);b.writeBoolean(v.reverse);b.writeBoolean(v.cancel);},b->new Use(b.readBoolean(),b.readBoolean(),b.readBoolean()));
        @Override public Type<Use> type(){return TYPE;}
    }
    public record Selection(boolean active,BlockPos pos,int cell,int face) implements CustomPacketPayload {
        public static final Type<Selection> TYPE=new Type<>(Identifier.fromNamespaceAndPath("paintableblocks","selection"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Selection> CODEC=StreamCodec.of((b,v)->{b.writeBoolean(v.active);b.writeBlockPos(v.pos);b.writeVarInt(v.cell);b.writeVarInt(v.face);},b->new Selection(b.readBoolean(),b.readBlockPos(),b.readVarInt(),b.readVarInt()));
        @Override public Type<Selection> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent event){
        var registrar=event.registrar("2");
        registrar.playToServer(Use.TYPE,Use.CODEC,(v,ctx)->ServerInteractions.use(ctx.player(),v.control,v.reverse,v.cancel?1:0));
        registrar.playToClient(Selection.TYPE,Selection.CODEC);
    }
    public static void selection(ServerPlayer player,BlockPos pos,int cell,int face){PacketDistributor.sendToPlayer(player,new Selection(true,pos,cell,face));}
    public static void clear(ServerPlayer player){PacketDistributor.sendToPlayer(player,new Selection(false,BlockPos.ZERO,0,0));}
}
