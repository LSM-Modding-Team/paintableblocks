package net.nicomar.paintableblocks.design;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Canonical integer half-space ax+by+cz <= offset, in local block coordinates. */
public record SlopePlane(int a,int b,int c,int offset,java.util.List<Integer> bounds,java.util.List<Integer> window) {
    public static final java.util.List<Integer> FULL=java.util.List.of(0,0,0,16,16,16);
    public static final java.util.List<Integer> ALL=java.util.List.of(-1,0,16);
    public SlopePlane(int a,int b,int c,int offset,java.util.List<Integer> bounds){this(a,b,c,offset,bounds,ALL);}
    public SlopePlane(int a,int b,int c,int offset){this(a,b,c,offset,FULL);}
    private record Raw(int a,int b,int c,int offset,java.util.List<Integer> bounds,java.util.List<Integer> window){
        private static final Codec<Raw> CODEC=RecordCodecBuilder.create(i->i.group(
                Codec.intRange(-8192,8192).fieldOf("a").forGetter(Raw::a),Codec.intRange(-8192,8192).fieldOf("b").forGetter(Raw::b),
                Codec.intRange(-8192,8192).fieldOf("c").forGetter(Raw::c),Codec.intRange(-262144,262144).fieldOf("offset").forGetter(Raw::offset), Codec.intRange(0,16).listOf(6,6).optionalFieldOf("bounds",FULL).forGetter(Raw::bounds),Codec.intRange(-1,16).listOf(3,6).optionalFieldOf("window",ALL).forGetter(Raw::window)).apply(i,Raw::new));
    }
    public static final Codec<SlopePlane> CODEC=Raw.CODEC.flatXmap(raw->{
        try{return com.mojang.serialization.DataResult.success(new SlopePlane(raw.a,raw.b,raw.c,raw.offset,raw.bounds,raw.window));}
        catch(IllegalArgumentException invalid){return com.mojang.serialization.DataResult.error(invalid::getMessage);}
    },plane->com.mojang.serialization.DataResult.success(new Raw(plane.a,plane.b,plane.c,plane.offset,plane.bounds,plane.window)));
    public SlopePlane {
        window=java.util.List.copyOf(window);if((window.size()!=3&&window.size()!=6)||window.get(0)<-1||window.get(0)>2||window.get(1)<0||window.get(2)>16||window.get(1)>window.get(2))throw new IllegalArgumentException("Invalid cut window");
        if(window.size()==6&&(window.get(0)<0||window.get(3)<0||window.get(3)>2||window.get(3).equals(window.get(0))||window.get(4)<0||window.get(5)>16||window.get(4)>window.get(5)))throw new IllegalArgumentException("Invalid lateral window");
        bounds=java.util.List.copyOf(bounds);
        if(bounds.size()!=6)throw new IllegalArgumentException("Invalid bounds");
        for(int i=0;i<3;i++)if(bounds.get(i)<0||bounds.get(i+3)>16||bounds.get(i)>=bounds.get(i+3))throw new IllegalArgumentException("Empty bounds");
        if(a==Integer.MIN_VALUE||b==Integer.MIN_VALUE||c==Integer.MIN_VALUE||offset==Integer.MIN_VALUE)throw new IllegalArgumentException("Plane integer overflow");
        int g=gcd(gcd(Math.abs(a),Math.abs(b)),gcd(Math.abs(c),Math.abs(offset)));if(g==0)throw new IllegalArgumentException("Zero plane");a/=g;b/=g;c/=g;offset/=g;
        if(Math.abs((long)a)>8192||Math.abs((long)b)>8192||Math.abs((long)c)>8192||Math.abs((long)offset)>262144||((a!=0?1:0)+(b!=0?1:0)+(c!=0?1:0)<1||(a!=0?1:0)+(b!=0?1:0)+(c!=0?1:0)>2))throw new IllegalArgumentException("Invalid slope plane");
    }
    private static int gcd(int a,int b){while(b!=0){int t=a%b;a=b;b=t;}return a;}
    public double coordinate(int axis,double fraction){return (bounds.get(axis)+(bounds.get(axis+3)-bounds.get(axis))*fraction)/16.0;}
    public Vec3 point(double x,double y,double z){return new Vec3(coordinate(0,x),coordinate(1,y),coordinate(2,z));}
    public Vec3 grid(Vec3 p){return new Vec3((p.x*16-bounds.get(0))/(bounds.get(3)-bounds.get(0)),(p.y*16-bounds.get(1))/(bounds.get(4)-bounds.get(1)),(p.z*16-bounds.get(2))/(bounds.get(5)-bounds.get(2)));}
    public double span(){return (Math.abs(a)*(bounds.get(3)-bounds.get(0))+Math.abs(b)*(bounds.get(4)-bounds.get(1))+Math.abs(c)*(bounds.get(5)-bounds.get(2)))/16.0;}
    public boolean cuts(Vec3 p){
        for(int i=0;i<window.size();i+=3){int axis=window.get(i);if(axis<0)continue;double v=(axis==0?p.x:axis==1?p.y:p.z)*16;if(v<window.get(i+1)-1e-9||v>=window.get(i+2)-1e-9)return false;}return true;
    }
    private java.util.List<Integer> rotatedWindow(int orientation){
        var out=new java.util.ArrayList<Integer>();
        for(int i=0;i<window.size();i+=3){
            if(window.get(i)<0){out.addAll(ALL);continue;}
            var f=DesignOrientation.direction(orientation,new Direction[]{Direction.EAST,Direction.UP,Direction.SOUTH}[window.get(i)]);
            int axis=f.getAxis()==Direction.Axis.X?0:f.getAxis()==Direction.Axis.Y?1:2;
            out.add(axis);out.add(f.getAxisDirection()==Direction.AxisDirection.POSITIVE?window.get(i+1):16-window.get(i+2));out.add(f.getAxisDirection()==Direction.AxisDirection.POSITIVE?window.get(i+2):16-window.get(i+1));
        }return java.util.List.copyOf(out);
    }
    public Vec3 normal(){return new Vec3(a,b,c);}
    public double value(Vec3 p){return a*p.x+b*p.y+c*p.z-offset;}
    public Direction dominant(){return Direction.getNearest(a,b,c,Direction.UP);}
    public SlopePlane rotated(int orientation){
        Vec3 center=new Vec3(.5,.5,.5),v=DesignOrientation.rotate(orientation,center.add(normal())).subtract(center);
        double w=offset-(a+b+c)*.5+(v.x+v.y+v.z)*.5;
        var lo=DesignOrientation.rotate(orientation,point(0,0,0));var hi=DesignOrientation.rotate(orientation,point(1,1,1));
        return new SlopePlane((int)Math.round(v.x),(int)Math.round(v.y),(int)Math.round(v.z),(int)Math.round(w),java.util.List.of((int)Math.round(Math.min(lo.x,hi.x)*16),(int)Math.round(Math.min(lo.y,hi.y)*16),(int)Math.round(Math.min(lo.z,hi.z)*16),(int)Math.round(Math.max(lo.x,hi.x)*16),(int)Math.round(Math.max(lo.y,hi.y)*16),(int)Math.round(Math.max(lo.z,hi.z)*16)),rotatedWindow(orientation));
    }
    public static SlopePlane local(int a,int b,int c,int globalOffset,net.minecraft.core.BlockPos pos,int orientation){
        return new SlopePlane(a,b,c,globalOffset-a*pos.getX()-b*pos.getY()-c*pos.getZ()).rotated(DesignOrientation.inverse(orientation));
    }
}
