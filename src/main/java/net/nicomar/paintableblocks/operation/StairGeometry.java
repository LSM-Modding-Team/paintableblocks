package net.nicomar.paintableblocks.operation;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.phys.Vec3;
import net.nicomar.paintableblocks.design.*;

/** Exact axial stair cells; no oblique plane is persisted or approximated. */
public final class StairGeometry {
    private StairGeometry(){}
    public static BlockDesign form(VolumeRegion r,BlockPos pos,BlockDesign source,int direction,boolean upper,boolean vertical,int start,int end,boolean create,boolean combined,int fullLength,boolean fullAtStart,boolean bothEnds,int otherLength,int lateralLow,int lateralHigh,int requested){
        if(source.slope()!=null)throw new IllegalArgumentException("Stairs require a design without an oblique cut");
        var d=source.physical();boolean x=direction%2==0,positive=direction<2;
        int run=x?r.x1()-r.x0():r.z1()-r.z0(),span=vertical?(x?r.z1()-r.z0():r.x1()-r.x0()):r.y1()-r.y0();
        if(end<0)end=span;if(requested<1||requested>256||start<0||end<0||start>span||end>span)throw new IllegalArgumentException("Invalid stair profile");
        int first=combined?(fullAtStart?fullLength:bothEnds?otherLength:0):0,last=combined?(fullAtStart?(bothEnds?otherLength:0):fullLength):0;
        int length=run-first-last;if(length<0)throw new IllegalArgumentException("Overlapping stair ends");
        int count=Math.max(1,Math.min(requested,Math.min(Math.max(1,length),Math.max(1,Math.abs(end-start)))));
        BitSet bits=new BitSet();boolean removed=false;int n=d.resolution(),scale=16/n;
        Map<Integer,Integer> paints=new TreeMap<>();
        for(int z=0;z<16;z++)for(int y=0;y<16;y++)for(int a=0;a<16;a++){
            int c=d.index(a/scale,y/scale,z/scale);if(!d.occupied(c))continue;
            var world=new Vec3(pos.getX()+(a+.5)/16,pos.getY()+(y+.5)/16,pos.getZ()+(z+.5)/16);boolean inside=r.contains(world),keep=!create||inside;
            double travel=positive?(x?world.x*16-r.x0():world.z*16-r.z0()):(x?r.x1()-world.x*16:r.z1()-world.z*16);
            double lateral=vertical?world.y*16-r.y0():x?world.z*16-r.z0():world.x*16-r.x0();
            if(inside&&length>0&&travel>=first&&travel<run-last&&(lateralHigh<0||lateral>=lateralLow&&lateral<lateralHigh)){
                int step=Math.min(count,(int)Math.floor((travel-first)*count/length)+(positive?1:0));
                int height=start+(int)Math.round((end-start)*step/(double)count);
                double dependent=vertical?(x?world.z*16-r.z0():world.x*16-r.x0()):world.y*16-r.y0();
                keep=upper?dependent>=height:dependent<height;
            }
            if(!keep){removed=true;continue;}int target=a+16*(y+16*z);bits.set(target);
            for(Direction face:Direction.values()){
                int coordinate=face.getAxis()==Direction.Axis.X?a:face.getAxis()==Direction.Axis.Y?y:z;
                boolean boundary=face.getAxisDirection()==Direction.AxisDirection.POSITIVE?(coordinate+1)%scale==0:coordinate%scale==0;
                int paint=d.paint(c,face);if(boundary&&paint!=0)paints.put(target*6+face.ordinal(),paint);
            }
        }
        if(!removed&&!create)return source;
        return BlockDesign.restore(16,bits.toLongArray(),paints,0,null);
    }
}
