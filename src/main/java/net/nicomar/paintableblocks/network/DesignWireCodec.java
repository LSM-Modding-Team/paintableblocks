package net.nicomar.paintableblocks.network;

import java.util.*;
import net.minecraft.network.FriendlyByteBuf;
import net.nicomar.paintableblocks.design.*;

/** Versioned compact wire format only. Persistent codec 2 and legacy codec 1 remain unchanged. */
public final class DesignWireCodec {
    public static void plane(FriendlyByteBuf b,SlopePlane plane){b.writeVarInt(plane.a());b.writeVarInt(plane.b());b.writeVarInt(plane.c());b.writeVarInt(plane.offset());}
    public static SlopePlane plane(FriendlyByteBuf b){return new SlopePlane(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt());}
    public static void write(FriendlyByteBuf b,BlockDesign d){
        b.writeVarInt(1);b.writeVarInt(d.resolution());b.writeVarInt(d.orientation());var words=d.geometryKey().occupied();b.writeVarInt(words.size());for(long word:words)b.writeLong(word);
        b.writeBoolean(d.slope()!=null);if(d.slope()!=null)plane(b,d.slope());b.writeVarInt(d.paints().size());for(var e:d.paints().entrySet()){b.writeVarInt(e.getKey());b.writeVarInt(e.getValue());}
    }
    public static BlockDesign read(FriendlyByteBuf b){
        if(b.readVarInt()!=1)throw new IllegalArgumentException("Unsupported wire design");int n=net.nicomar.paintableblocks.balance.PaintingBalance.requireResolution(b.readVarInt()),orientation=b.readVarInt(),count=b.readVarInt();
        if(count<0||count>(n*n*n+63)/64)throw new IllegalArgumentException("Invalid wire occupation");long[] words=new long[count];for(int i=0;i<count;i++)words[i]=b.readLong();var slope=b.readBoolean()?plane(b):null;
        count=b.readVarInt();if(count<0||count>7*n*n*n)throw new IllegalArgumentException("Invalid wire paint count");Map<Integer,Integer> paints=new TreeMap<>();for(int i=0;i<count;i++){int key=b.readVarInt(),paint=b.readVarInt();if(paints.put(key,paint)!=null)throw new IllegalArgumentException("Duplicate wire face");}
        return BlockDesign.restore(n,words,paints,orientation,slope);
    }
    private DesignWireCodec(){}
}
