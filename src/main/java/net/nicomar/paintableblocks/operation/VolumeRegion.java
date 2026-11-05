package net.nicomar.paintableblocks.operation;

import java.util.BitSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.nicomar.paintableblocks.design.*;

/** Integer sixteenth-block boundaries, inclusive cell centers; block extents count both endpoints. */
public record VolumeRegion(int x0,int y0,int z0,int x1,int y1,int z1) {
    public VolumeRegion {if(x1<=x0||y1<=y0||z1<=z0)throw new IllegalArgumentException("Empty volume");}
    public static VolumeRegion aim(BlockPos pos,Vec3 point,Direction face){
        int x=(int)Math.floor((point.x-face.getStepX()*1e-7)*16),y=(int)Math.floor((point.y-face.getStepY()*1e-7)*16),z=(int)Math.floor((point.z-face.getStepZ()*1e-7)*16);
        return new VolumeRegion(x,y,z,x+1,y+1,z+1);
    }
    public static VolumeRegion block(BlockPos p){return new VolumeRegion(p.getX()*16,p.getY()*16,p.getZ()*16,(p.getX()+1)*16,(p.getY()+1)*16,(p.getZ()+1)*16);}
    public static VolumeRegion cell(BlockPos p,BlockDesign d,int cell){
        Vec3 lo=new Vec3(d.x(cell)/(double)d.resolution(),d.y(cell)/(double)d.resolution(),d.z(cell)/(double)d.resolution());
        Vec3 hi=lo.add(1.0/d.resolution(),1.0/d.resolution(),1.0/d.resolution());
        if(d.slope()!=null){lo=d.slope().point(lo.x,lo.y,lo.z);hi=d.slope().point(hi.x,hi.y,hi.z);}
        lo=DesignOrientation.rotate(d.orientation(),lo);hi=DesignOrientation.rotate(d.orientation(),hi);
        return new VolumeRegion(p.getX()*16+(int)Math.floor(Math.min(lo.x,hi.x)*16),p.getY()*16+(int)Math.floor(Math.min(lo.y,hi.y)*16),p.getZ()*16+(int)Math.floor(Math.min(lo.z,hi.z)*16),p.getX()*16+(int)Math.ceil(Math.max(lo.x,hi.x)*16),p.getY()*16+(int)Math.ceil(Math.max(lo.y,hi.y)*16),p.getZ()*16+(int)Math.ceil(Math.max(lo.z,hi.z)*16));
    }
    public VolumeRegion union(VolumeRegion b){return new VolumeRegion(Math.min(x0,b.x0),Math.min(y0,b.y0),Math.min(z0,b.z0),Math.max(x1,b.x1),Math.max(y1,b.y1),Math.max(z1,b.z1));}
    public BlockPos min(){return new BlockPos(Math.floorDiv(x0,16),Math.floorDiv(y0,16),Math.floorDiv(z0,16));}
    public BlockPos max(){return new BlockPos(Math.floorDiv(x1-1,16),Math.floorDiv(y1-1,16),Math.floorDiv(z1-1,16));}
    public int width(){return max().getX()-min().getX()+1;}
    public int height(){return max().getY()-min().getY()+1;}
    public int depth(){return max().getZ()-min().getZ()+1;}
    public long size(){return (long)width()*height()*depth();}
    public boolean valid(int limit){return width()<=limit&&height()<=limit&&depth()<=limit;}
    public BlockPos position(long index){var p=min();return p.offset((int)(index%width()),(int)(index/width()%height()),(int)(index/((long)width()*height())));}
    public boolean contains(Vec3 p){return p.x*16>=x0-1e-8&&p.x*16<=x1+1e-8&&p.y*16>=y0-1e-8&&p.y*16<=y1+1e-8&&p.z*16>=z0-1e-8&&p.z*16<=z1+1e-8;}
    public DesignOperations.Plan hammer(BlockPos pos,BlockDesign d){
        BitSet selected=new BitSet();if(d.resolution()!=1)for(int c=0;c<d.resolution()*d.resolution()*d.resolution();c++)if(d.occupied(c)){
            var point=new Vec3((d.x(c)+.5)/d.resolution(),(d.y(c)+.5)/d.resolution(),(d.z(c)+.5)/d.resolution());if(d.slope()!=null)point=d.slope().point(point.x,point.y,point.z);var center=DesignOrientation.rotate(d.orientation(),point).add(pos.getX(),pos.getY(),pos.getZ());if(contains(center))selected.set(c);
        }return new DesignOperations.Plan(d,d.remove(selected),selected.stream().boxed().toList(),java.util.List.of(),0);
    }
    public SlopePlane plane(BlockPos pos,BlockDesign d,int direction,boolean upper){
        Direction facing=new Direction[]{Direction.EAST,Direction.SOUTH,Direction.WEST,Direction.NORTH}[direction];
        int length=facing.getAxis()==Direction.Axis.X?x1-x0:z1-z0,height=y1-y0;
        int a=-height*facing.getStepX(),b=length,c=-height*facing.getStepZ();
        int origin=facing.getAxis()==Direction.Axis.X?(facing.getStepX()>0?x0:x1):(facing.getStepZ()>0?z0:z1);
        // Work in local sixteenths before scaling to preserve fractional intercepts without world-coordinate overflow.
        long local=(long)length*(y0-pos.getY()*16);
        if(a!=0)local+=(long)a*(origin-pos.getX()*16);else local+=(long)c*(origin-pos.getZ()*16);
        int sign=upper?-1:1;
        return new SlopePlane(sign*a*16,sign*b*16,sign*c*16,Math.toIntExact(sign*local)).rotated(DesignOrientation.inverse(d.orientation()));
    }
    /** Endpoint distances are local sixteenths along Y, or the transverse horizontal axis in vertical mode. */
    public SlopePlane profilePlane(BlockPos pos,BlockDesign d,int direction,boolean upper,boolean vertical,int start,int end,boolean bounded){
        if(direction<0||direction>3)throw new IllegalArgumentException("Invalid direction");
        boolean runX=direction%2==0,positive=direction<2;
        int runSpan=runX?x1-x0:z1-z0;
        int dependentSpan=vertical?(runX?z1-z0:x1-x0):y1-y0;
        if(end<0)end=dependentSpan;
        if(start<0||end<0||start>dependentSpan||end>dependentSpan)throw new IllegalArgumentException("Endpoint outside prism");
        int delta=end-start,runCoefficient=-delta*(positive?1:-1),dependentCoefficient=runSpan;
        int a=runX?runCoefficient:vertical?dependentCoefficient:0;
        int b=vertical?0:dependentCoefficient;
        int c=runX?(vertical?dependentCoefficient:0):runCoefficient;
        int runOrigin=runX?(positive?x0:x1):(positive?z0:z1);
        int runPosition=(runX?pos.getX():pos.getZ())*16;
        int dependentOrigin=vertical?(runX?z0:x0):y0;
        int dependentPosition=(vertical?(runX?pos.getZ():pos.getX()):pos.getY())*16;
        long constant=(long)dependentCoefficient*(dependentOrigin-dependentPosition+start)+(long)runCoefficient*(runOrigin-runPosition);
        int sign=upper?-1:1;
        var plane=new SlopePlane(sign*a*16,sign*b*16,sign*c*16,Math.toIntExact(sign*constant));
        if(bounded)plane=new SlopePlane(plane.a(),plane.b(),plane.c(),plane.offset(),java.util.List.of(Math.max(0,x0-pos.getX()*16),Math.max(0,y0-pos.getY()*16),Math.max(0,z0-pos.getZ()*16),Math.min(16,x1-pos.getX()*16),Math.min(16,y1-pos.getY()*16),Math.min(16,z1-pos.getZ()*16)));
        return plane.rotated(DesignOrientation.inverse(d.orientation()));
    }
    /** Shorten the incline to leave a complete plateau, preserving the existing stored geometry. */
    public SlopePlane combinedPlane(BlockPos pos,BlockDesign d,int direction,boolean upper,boolean vertical,int start,int end,boolean bounded,boolean combined,int fullLength,boolean fullAtStart){
        if(!combined)return profilePlane(pos,d,direction,upper,vertical,start,end,bounded);
        boolean runX=direction%2==0,positive=direction<2;
        int run=runX?x1-x0:z1-z0,dependent=vertical?(runX?z1-z0:x1-x0):y1-y0;
        if(fullLength<0||fullLength>run)throw new IllegalArgumentException("Invalid full section");
        if(end<0)end=dependent;
        if(start<0||start>dependent||end<0||end>dependent)throw new IllegalArgumentException("Invalid endpoint");
        VolumeRegion ramp=this;int low=fullAtStart?dependent:start,high=fullAtStart?end:dependent;
        if(fullLength==run){low=dependent;high=dependent;}
        else if(fullLength>0){
            boolean trimLow=fullAtStart==positive;
            ramp=runX?new VolumeRegion(x0+(trimLow?fullLength:0),y0,z0,x1-(trimLow?0:fullLength),y1,z1):
                new VolumeRegion(x0,y0,z0+(trimLow?fullLength:0),x1,y1,z1-(trimLow?0:fullLength));
        }
        if(upper){low=dependent-low;high=dependent-high;}
        var plane=ramp.profilePlane(pos,d,direction,upper,vertical,low,high,false);
        if(!bounded)return plane;
        var world=plane.rotated(d.orientation());
        return new SlopePlane(world.a(),world.b(),world.c(),world.offset(),java.util.List.of(Math.max(0,x0-pos.getX()*16),Math.max(0,y0-pos.getY()*16),Math.max(0,z0-pos.getZ()*16),Math.min(16,x1-pos.getX()*16),Math.min(16,y1-pos.getY()*16),Math.min(16,z1-pos.getZ()*16))).rotated(DesignOrientation.inverse(d.orientation()));
    }
    public BlockDesign doubleSeed(BlockPos pos,BlockDesign d){
        return d;
    }
    public SlopePlane doublePlane(BlockPos pos,BlockDesign d,int direction,boolean upper,boolean vertical,int start,int end,boolean bounded,boolean combined,int fullLength,boolean fullAtStart,boolean bothEnds,int otherLength){
        if(!bothEnds)return combinedPlane(pos,d,direction,upper,vertical,start,end,bounded,combined,fullLength,fullAtStart);
        boolean x=direction%2==0,positive=direction<2;int run=x?x1-x0:z1-z0;
        int first=fullAtStart?fullLength:otherLength,last=fullAtStart?otherLength:fullLength;
        if(first<0||last<0||first+last>run)throw new IllegalArgumentException("Overlapping complete ends");
        if(first+last==run)return profilePlane(pos,d,direction,upper,vertical,upper?0:(vertical?(x?z1-z0:x1-x0):y1-y0),upper?0:(vertical?(x?z1-z0:x1-x0):y1-y0),false);
        int lo=(x?x0:z0)+(positive?first:last),hi=(x?x1:z1)-(positive?last:first);
        var middle=x?new VolumeRegion(lo,y0,z0,hi,y1,z1):new VolumeRegion(x0,y0,lo,x1,y1,hi);
        var plane=middle.profilePlane(pos,d.withOrientation(0),direction,upper,vertical,start,end,false);
        int origin=(x?pos.getX():pos.getZ())*16;
        var bounds=bounded?java.util.List.of(Math.max(0,x0-pos.getX()*16),Math.max(0,y0-pos.getY()*16),Math.max(0,z0-pos.getZ()*16),Math.min(16,x1-pos.getX()*16),Math.min(16,y1-pos.getY()*16),Math.min(16,z1-pos.getZ()*16)):d.slope()==null?SlopePlane.FULL:d.slope().rotated(d.orientation()).bounds();
        return new SlopePlane(plane.a(),plane.b(),plane.c(),plane.offset(),bounds,java.util.List.of(x?0:2,Math.clamp(lo-origin,0,16),Math.clamp(hi-origin,0,16))).rotated(DesignOrientation.inverse(d.orientation()));
    }
    /** The second window bounds the extrusion axis; material outside remains complete. */
    public SlopePlane lateralPlane(BlockPos pos,BlockDesign d,SlopePlane local,int direction,boolean vertical,int low,int high){
        if(high<0)return local;
        int axis=vertical?1:direction%2==0?2:0;
        int origin=axis==1?y0:axis==2?z0:x0,span=axis==1?y1-y0:axis==2?z1-z0:x1-x0;
        if(low<0||high<=low||high>span)throw new IllegalArgumentException("Invalid lateral interval");
        var plane=local.rotated(d.orientation());var window=new java.util.ArrayList<>(plane.window().subList(0,3));
        if(window.getFirst()<0){window.set(0,direction%2==0?0:2);window.set(1,0);window.set(2,16);}
        int block=(axis==1?pos.getY():axis==2?pos.getZ():pos.getX())*16;
        window.add(axis);window.add(Math.clamp(origin+low-block,0,16));window.add(Math.clamp(origin+high-block,0,16));
        return new SlopePlane(plane.a(),plane.b(),plane.c(),plane.offset(),plane.bounds(),window).rotated(DesignOrientation.inverse(d.orientation()));
    }
    public SlopePlane creationPlane(BlockPos pos,BlockDesign d,int direction,boolean upper){
        var p=plane(pos,d,direction,upper);
        return new SlopePlane(p.a(),p.b(),p.c(),p.offset(),java.util.List.of(Math.max(0,x0-pos.getX()*16),Math.max(0,y0-pos.getY()*16),Math.max(0,z0-pos.getZ()*16),Math.min(16,x1-pos.getX()*16),Math.min(16,y1-pos.getY()*16),Math.min(16,z1-pos.getZ()*16)));
    }
}
