package net.nicomar.paintableblocks.operation;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;

/** One bounded unit per poll: inspect one block-entity entry, or yield one position. Never loads chunks. */
public final class VolumeCursor {
    private final VolumeRegion region;private final boolean dense;private final int x0,z0,xCount,zCount;
    private int chunk;private long reviewed;private Iterator<BlockPos> inspecting;private final List<BlockPos> candidates=new ArrayList<>();private int next;private boolean collected,done;
    public VolumeCursor(VolumeRegion region,boolean dense){this.region=region;this.dense=dense;x0=region.min().getX()>>4;z0=region.min().getZ()>>4;xCount=(region.max().getX()>>4)-x0+1;zCount=(region.max().getZ()>>4)-z0+1;}
    public boolean done(){return done;}public long reviewed(){return reviewed;}
    public BlockPos poll(ServerLevel level){
        if(dense){if(reviewed==region.size()){done=true;return null;}return region.position(reviewed++);}
        if(chunk>=xCount*zCount){done=true;return null;}int cx=x0+chunk%xCount,cz=z0+chunk/xCount;var loaded=level.getChunkSource().getChunkNow(cx,cz);if(loaded==null)throw new IllegalStateException("Unloaded selection chunk");
        if(!collected){
            if(inspecting==null)inspecting=loaded.getBlockEntitiesPos().iterator();
            try{if(inspecting.hasNext()){var p=inspecting.next();if(inside(p)&&loaded.getBlockState(p).getBlock() instanceof net.nicomar.paintableblocks.block.PaintableBlock)candidates.add(p.immutable());return null;}}
            catch(ConcurrentModificationException changed){inspecting=null;candidates.clear();return null;}
            collected=true;reviewed+=chunkSize(cx,cz)-candidates.size();
        }
        if(next<candidates.size()){reviewed++;return candidates.get(next++);}
        chunk++;inspecting=null;collected=false;next=0;candidates.clear();return null;
    }
    private boolean inside(BlockPos p){return p.getX()>=region.min().getX()&&p.getX()<=region.max().getX()&&p.getY()>=region.min().getY()&&p.getY()<=region.max().getY()&&p.getZ()>=region.min().getZ()&&p.getZ()<=region.max().getZ();}
    private long chunkSize(int x,int z){int width=Math.min(region.max().getX(),x*16+15)-Math.max(region.min().getX(),x*16)+1,depth=Math.min(region.max().getZ(),z*16+15)-Math.max(region.min().getZ(),z*16)+1;return (long)width*depth*region.height();}
}
