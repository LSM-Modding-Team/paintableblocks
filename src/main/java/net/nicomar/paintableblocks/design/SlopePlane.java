package net.nicomar.paintableblocks.design;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Canonical integer half-space ax+by+cz <= offset, in local block coordinates. */
public record SlopePlane(int a,int b,int c,int offset,java.util.List<Integer> bounds) {
    public static final java.util.List<Integer> FULL=java.util.List.of(0,0,0,16,16,16);
    public SlopePlane(int a,int b,int c,int offset){this(a,b,c,offset,FULL);}
    private record Raw(int a,int b,int c,int offset,java.util.List<Integer> bounds){
        private static final Codec<Raw> CODEC=RecordCodecBuilder.create(i->i.group(
                Codec.intRange(-8192,8192).fieldOf("a").forGetter(Raw::a),Codec.intRange(-8192,8192).fieldOf("b").forGetter(Raw::b),
                Codec.intRange(-8192,8192).fieldOf("c").forGetter(Raw::c),Codec.intRange(-262144,262144).fieldOf("offset").forGetter(Raw::offset), Codec.intRange(0,16).listOf(6,6).fieldOf("bounds").forGetter(Raw::bounds)).apply(i,Raw::new));
    }
    public static final Codec<SlopePlane> CODEC=Raw.CODEC.flatXmap(raw->{
        try{return com.mojang.serialization.DataResult.success(new SlopePlane(raw.a,raw.b,raw.c,raw.offset,raw.bounds));}
        catch(IllegalArgumentException invalid){return com.mojang.serialization.DataResult.error(invalid::getMessage);}
    },plane->com.mojang.serialization.DataResult.success(new Raw(plane.a,plane.b,plane.c,plane.offset,plane.bounds)));
    public SlopePlane {
        bounds=java.util.List.copyOf(bounds);
        if(bounds.size()!=6)throw new IllegalArgumentException("Invalid bounds");
        for(int i=0;i<3;i++)if(bounds.get(i)<0||bounds.get(i+3)>16||bounds.get(i)>=bounds.get(i+3))throw new IllegalArgumentException("Empty bounds");
        if(Math.abs((long)a)>8192||Math.abs((long)b)>8192||Math.abs((long)c)>8192||Math.abs((long)offset)>262144||((a!=0?1:0)+(b!=0?1:0)+(c!=0?1:0)!=2))throw new IllegalArgumentException("Invalid slope plane");
        int g=gcd(gcd(Math.abs(a),Math.abs(b)),gcd(Math.abs(c),Math.abs(offset)));a/=g;b/=g;c/=g;offset/=g;
    }
    private static int gcd(int a,int b){while(b!=0){int t=a%b;a=b;b=t;}return a;}
    public double coordinate(int axis,double fraction){return (bounds.get(axis)+(bounds.get(axis+3)-bounds.get(axis))*fraction)/16.0;}
    public Vec3 point(double x,double y,double z){return new Vec3(coordinate(0,x),coordinate(1,y),coordinate(2,z));}
    public Vec3 grid(Vec3 p){return new Vec3((p.x*16-bounds.get(0))/(bounds.get(3)-bounds.get(0)),(p.y*16-bounds.get(1))/(bounds.get(4)-bounds.get(1)),(p.z*16-bounds.get(2))/(bounds.get(5)-bounds.get(2)));}
    public double span(){return (Math.abs(a)*(bounds.get(3)-bounds.get(0))+Math.abs(b)*(bounds.get(4)-bounds.get(1))+Math.abs(c)*(bounds.get(5)-bounds.get(2)))/16.0;}
    public Vec3 normal(){return new Vec3(a,b,c);}
    public double value(Vec3 p){return a*p.x+b*p.y+c*p.z-offset;}
    public Direction dominant(){return Direction.getNearest(a,b,c,Direction.UP);}
    public SlopePlane rotated(int orientation){
        Vec3 center=new Vec3(.5,.5,.5),v=DesignOrientation.rotate(orientation,center.add(normal())).subtract(center);
        double w=offset-(a+b+c)*.5+(v.x+v.y+v.z)*.5;
        var lo=DesignOrientation.rotate(orientation,point(0,0,0));var hi=DesignOrientation.rotate(orientation,point(1,1,1));
        return new SlopePlane((int)Math.round(v.x),(int)Math.round(v.y),(int)Math.round(v.z),(int)Math.round(w),java.util.List.of((int)Math.round(Math.min(lo.x,hi.x)*16),(int)Math.round(Math.min(lo.y,hi.y)*16),(int)Math.round(Math.min(lo.z,hi.z)*16),(int)Math.round(Math.max(lo.x,hi.x)*16),(int)Math.round(Math.max(lo.y,hi.y)*16),(int)Math.round(Math.max(lo.z,hi.z)*16)));
    }
    public static SlopePlane local(int a,int b,int c,int globalOffset,net.minecraft.core.BlockPos pos,int orientation){
        return new SlopePlane(a,b,c,globalOffset-a*pos.getX()-b*pos.getY()-c*pos.getZ()).rotated(DesignOrientation.inverse(orientation));
    }
}
