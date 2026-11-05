package net.nicomar.paintableblocks.network;

import java.util.*;
import net.minecraft.network.FriendlyByteBuf;
import net.nicomar.paintableblocks.design.*;

/** Versioned compact wire format only. Persistent formats 1–4 remain readable by the design codec. */
public final class DesignWireCodec {
    public static void plane(FriendlyByteBuf b,SlopePlane plane){b.writeVarInt(plane.a());b.writeVarInt(plane.b());b.writeVarInt(plane.c());b.writeVarInt(plane.offset());for(int bound:plane.bounds())b.writeVarInt(bound);b.writeVarInt(plane.window().size());for(int v:plane.window())b.writeVarInt(v);}
    public static SlopePlane plane(FriendlyByteBuf b){
        int a=b.readVarInt(),y=b.readVarInt(),c=b.readVarInt(),offset=b.readVarInt();var bounds=java.util.List.of(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt());
        int count=b.readVarInt();if(count!=3&&count!=6)throw new IllegalArgumentException("Invalid cut window length");var window=new ArrayList<Integer>();for(int i=0;i<count;i++)window.add(b.readVarInt());return new SlopePlane(a,y,c,offset,bounds,window);
    }
    public static void write(FriendlyByteBuf b,BlockDesign d){
        b.writeVarInt(2);b.writeVarInt(d.resolution());b.writeVarInt(d.orientation());var words=d.geometryKey().occupied();b.writeVarInt(words.size());for(long word:words)b.writeLong(word);
        b.writeBoolean(d.slope()!=null);if(d.slope()!=null)plane(b,d.slope());b.writeVarInt(d.paints().size());for(var e:d.paints().entrySet()){b.writeVarInt(e.getKey());b.writeVarInt(e.getValue());}
    }
    public static BlockDesign read(FriendlyByteBuf b){
        if(b.readVarInt()!=2)throw new IllegalArgumentException("Unsupported wire design");int n=net.nicomar.paintableblocks.balance.PaintingBalance.requireResolution(b.readVarInt()),orientation=b.readVarInt(),count=b.readVarInt();
        if(count<0||count>(n*n*n+63)/64)throw new IllegalArgumentException("Invalid wire occupation");long[] words=new long[count];for(int i=0;i<count;i++)words[i]=b.readLong();var slope=b.readBoolean()?plane(b):null;
        count=b.readVarInt();if(count<0||count>7*n*n*n)throw new IllegalArgumentException("Invalid wire paint count");Map<Integer,Integer> paints=new TreeMap<>();for(int i=0;i<count;i++){int key=b.readVarInt(),paint=b.readVarInt();if(paints.put(key,paint)!=null)throw new IllegalArgumentException("Duplicate wire face");}
        return BlockDesign.restore(n,words,paints,orientation,slope);
    }
    private DesignWireCodec(){}
}
