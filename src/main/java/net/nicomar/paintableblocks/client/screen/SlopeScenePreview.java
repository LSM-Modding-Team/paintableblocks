package net.nicomar.paintableblocks.client.screen;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.client.selection.VolumePreview;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.network.ModNetworking;
import net.nicomar.paintableblocks.operation.VolumeRegion;
import net.nicomar.paintableblocks.registry.ModContent;

/** Prepares the same pure design operations as the server, without consuming items or editing the world. */
final class SlopeScenePreview {
    private record Key(long session,VolumeRegion region,int direction,boolean upper,boolean vertical,int start,int end,boolean create,int resolution,boolean combined,int length,boolean first,boolean both,int other){}
    private Key key;private long cursor;private int weight,phase,mode,yaw=45,pitch=30;private boolean limited;private String error="";
    private final List<PreviewScene.Piece> pieces=new ArrayList<>();
    private ItemStack stack=ItemStack.EMPTY;
    String error(){return error;}
    void view(int mode,int yaw,int pitch){if(this.mode!=mode){this.mode=mode;key=null;}this.yaw=yaw;this.pitch=pitch;if(!stack.isEmpty())stack.set(ModContent.PREVIEW_SCENE.get(),new PreviewScene(pieces,yaw,pitch));}
    private ModNetworking.VolumeNotice shownNotice;
    ItemStack stack(){var n=VolumePreview.notice();if(n!=shownNotice&&!stack.isEmpty()){shownNotice=n;var visible=pieces.stream().map(p->n!=null&&n.pos().equals(p.pos())?new PreviewScene.Piece(p.pos(),p.design(),0xffff5555):p).toList();stack.set(ModContent.PREVIEW_SCENE.get(),new PreviewScene(visible,yaw,pitch));}return stack;}
    boolean pending(){return key!=null&&cursor<key.region().size()&&!limited;}
    boolean limited(){return limited;}
    private static boolean solid(BlockDesign d,net.minecraft.world.phys.Vec3 world){
        var p=DesignOrientation.rotate(DesignOrientation.inverse(d.orientation()),world);var v=d.slope()==null?p:d.slope().grid(p);int n=d.resolution();
        return v.x>=0&&v.y>=0&&v.z>=0&&v.x<1&&v.y<1&&v.z<1&&d.occupied((int)(v.x*n),(int)(v.y*n),(int)(v.z*n))&&(d.slope()==null||!d.slope().cuts(p)||d.slope().value(p)<=1e-10);
    }
    private static BlockDesign removed(BlockDesign before,BlockDesign after){
        var cells=new java.util.BitSet();for(int c=0;c<4096;c++){var p=new net.minecraft.world.phys.Vec3((c%16+.5)/16,(c/16%16+.5)/16,(c/256+.5)/16);if(!solid(before,p)||solid(after,p))cells.set(c);}return BlockDesign.full(16).remove(cells);
    }
    void tick(){
        var s=VolumePreview.state();var client=Minecraft.getInstance();if(s==null||client.level==null)return;
        var r=VolumePreview.region(s);var next=new Key(s.id(),r,s.direction(),s.upper(),s.vertical(),s.start(),s.end(),s.create(),s.resolution(),s.combined(),s.fullLength(),s.fullAtStart(),s.bothEnds(),s.otherLength());
        if(!next.equals(key)||phase==net.nicomar.paintableblocks.interaction.VolumeEditing.READY&&s.phase()==net.nicomar.paintableblocks.interaction.VolumeEditing.SCANNING){key=next;cursor=0;weight=0;limited=false;error="";pieces.clear();stack=ItemStack.EMPTY;}
        phase=s.phase();if(limited||cursor>=r.size())return;
        long deadline=System.nanoTime()+1_000_000;int budget=2;boolean changed=false;
        while(cursor<r.size()&&budget-->0&&System.nanoTime()<deadline){
            var pos=r.position(cursor++);if(!client.level.hasChunkAt(pos)){error=net.minecraft.network.chat.Component.translatable("message.paintableblocks.unloaded_chunk").getString()+" "+pos.toShortString();continue;}
            BlockDesign d;
            if(s.create()){
                if(!client.level.getBlockState(pos).isAir()||!client.level.getFluidState(pos).isEmpty()){error=net.minecraft.network.chat.Component.translatable("message.paintableblocks.occupied_destination").getString()+" "+pos.toShortString();if(weight+6<=32768){pieces.add(new PreviewScene.Piece(pos,BlockDesign.full(1),0xffff5555));weight+=6;changed=true;}continue;}
                d=BlockDesign.full(s.resolution());
            }else{
                if(!(client.level.getBlockEntity(pos) instanceof PaintableBlockEntity be)||!be.valid())continue;d=be.design();
            }
            try{
                var source=s.bothEnds()?r.doubleSeed(pos,d):d;
                var result=source.withSlope(r.doublePlane(pos,d,s.direction(),s.upper(),s.vertical(),s.start(),s.end(),s.create(),s.combined(),s.fullLength(),s.fullAtStart(),s.bothEnds(),s.otherLength()));
                if(mode==1){if(s.create())continue;result=d;}
                if(mode==2){if(s.create())continue;result=removed(d,result);}
                if(result.empty())continue;
                int cost=DesignGeometry.surfaces(result).size();if(weight+cost>32768){limited=true;break;}weight+=cost;
                pieces.add(new PreviewScene.Piece(pos,result,mode==2?0xffff5555:-1));changed=true;
            }catch(IllegalArgumentException|IllegalStateException incompatible){error=net.minecraft.network.chat.Component.translatable(incompatible.getMessage()!=null&&incompatible.getMessage().contains("16")?"message.paintableblocks.grid_mismatch":"message.paintableblocks.plane_conflict").getString()+" "+pos.toShortString();if(!s.create()&&weight+DesignGeometry.surfaces(d).size()<=32768){pieces.add(new PreviewScene.Piece(pos,d,0xffff5555));weight+=DesignGeometry.surfaces(d).size();changed=true;}continue;}
        }
        if(changed){stack=ModContent.stack(BlockDesign.full(1));stack.set(ModContent.PREVIEW_SCENE.get(),new PreviewScene(pieces,yaw,pitch));}
    }
}
