package net.nicomar.paintableblocks.design;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Canonical integer half-space ax+by+cz <= offset, in local block coordinates. */
public record SlopePlane(int a,int b,int c,int offset) {
    private record Raw(int a,int b,int c,int offset){
        private static final Codec<Raw> CODEC=RecordCodecBuilder.create(i->i.group(
                Codec.intRange(-512,512).fieldOf("a").forGetter(Raw::a),Codec.intRange(-512,512).fieldOf("b").forGetter(Raw::b),
                Codec.intRange(-512,512).fieldOf("c").forGetter(Raw::c),Codec.intRange(-2048,2048).fieldOf("offset").forGetter(Raw::offset)).apply(i,Raw::new));
    }
    public static final Codec<SlopePlane> CODEC=Raw.CODEC.flatXmap(raw->{
        try{return com.mojang.serialization.DataResult.success(new SlopePlane(raw.a,raw.b,raw.c,raw.offset));}
        catch(IllegalArgumentException invalid){return com.mojang.serialization.DataResult.error(invalid::getMessage);}
    },plane->com.mojang.serialization.DataResult.success(new Raw(plane.a,plane.b,plane.c,plane.offset)));
    public SlopePlane {
        if(Math.abs((long)a)>512||Math.abs((long)b)>512||Math.abs((long)c)>512||Math.abs((long)offset)>2048||((a!=0?1:0)+(b!=0?1:0)+(c!=0?1:0)!=2))throw new IllegalArgumentException("Invalid slope plane");
        int g=gcd(gcd(Math.abs(a),Math.abs(b)),gcd(Math.abs(c),Math.abs(offset)));a/=g;b/=g;c/=g;offset/=g;
    }
    private static int gcd(int a,int b){while(b!=0){int t=a%b;a=b;b=t;}return a;}
    public Vec3 normal(){return new Vec3(a,b,c);}
    public double value(Vec3 p){return a*p.x+b*p.y+c*p.z-offset;}
    public Direction dominant(){return Direction.getNearest(a,b,c,Direction.UP);}
    public SlopePlane rotated(int orientation){
        Vec3 center=new Vec3(.5,.5,.5),v=DesignOrientation.rotate(orientation,center.add(normal())).subtract(center);
        double w=offset-(a+b+c)*.5+(v.x+v.y+v.z)*.5;
        return new SlopePlane((int)Math.round(v.x),(int)Math.round(v.y),(int)Math.round(v.z),(int)Math.round(w));
    }
    public static SlopePlane local(int a,int b,int c,int globalOffset,net.minecraft.core.BlockPos pos,int orientation){
        return new SlopePlane(a,b,c,globalOffset-a*pos.getX()-b*pos.getY()-c*pos.getZ()).rotated(DesignOrientation.inverse(orientation));
    }
}
