package net.nicomar.paintableblocks.operation;

import java.util.BitSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.nicomar.paintableblocks.design.*;

/** Integer sixteenth-block boundaries, inclusive cell centers; block extents count both endpoints. */
public record VolumeRegion(int x0,int y0,int z0,int x1,int y1,int z1) {
    public VolumeRegion {if(x1<=x0||y1<=y0||z1<=z0)throw new IllegalArgumentException("Empty volume");}
    public static VolumeRegion block(BlockPos p){return new VolumeRegion(p.getX()*16,p.getY()*16,p.getZ()*16,(p.getX()+1)*16,(p.getY()+1)*16,(p.getZ()+1)*16);}
    public static VolumeRegion cell(BlockPos p,BlockDesign d,int cell){int[] c=DesignOrientation.cell(d.orientation(),d.x(cell),d.y(cell),d.z(cell),d.resolution());int size=16/d.resolution();return new VolumeRegion(p.getX()*16+c[0]*size,p.getY()*16+c[1]*size,p.getZ()*16+c[2]*size,p.getX()*16+(c[0]+1)*size,p.getY()*16+(c[1]+1)*size,p.getZ()*16+(c[2]+1)*size);}
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
            var center=DesignOrientation.rotate(d.orientation(),new Vec3((d.x(c)+.5)/d.resolution(),(d.y(c)+.5)/d.resolution(),(d.z(c)+.5)/d.resolution())).add(pos.getX(),pos.getY(),pos.getZ());if(contains(center))selected.set(c);
        }return new DesignOperations.Plan(d,d.remove(selected),selected.stream().boxed().toList(),java.util.List.of(),0);
    }
    public SlopePlane plane(BlockPos pos,BlockDesign d,int direction,boolean upper){
        Direction facing=new Direction[]{Direction.EAST,Direction.SOUTH,Direction.WEST,Direction.NORTH}[direction];var lo=min();int length=facing.getAxis()==Direction.Axis.X?width():depth(),height=height();
        int a=-height*facing.getStepX(),b=length,c=-height*facing.getStepZ();int origin=facing.getAxis()==Direction.Axis.X?(facing.getStepX()>0?lo.getX():max().getX()+1):(facing.getStepZ()>0?lo.getZ():max().getZ()+1);
        int offset=length*lo.getY()+(a!=0?a:c)*origin;if(upper){a=-a;b=-b;c=-c;offset=-offset;}
        return SlopePlane.local(a,b,c,offset,pos,d.orientation());
    }
}
