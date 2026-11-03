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
    private Key key;private long cursor;private int weight,phase;private boolean limited;
    private final List<PreviewScene.Piece> pieces=new ArrayList<>();
    private ItemStack stack=ItemStack.EMPTY;
    ItemStack stack(){return stack;}
    boolean pending(){return key!=null&&cursor<key.region().size()&&!limited;}
    boolean limited(){return limited;}
    void tick(){
        var s=VolumePreview.state();var client=Minecraft.getInstance();if(s==null||client.level==null)return;
        var r=VolumePreview.region(s);var next=new Key(s.id(),r,s.direction(),s.upper(),s.vertical(),s.start(),s.end(),s.create(),s.resolution(),s.combined(),s.fullLength(),s.fullAtStart(),s.bothEnds(),s.otherLength());
        if(!next.equals(key)||phase==net.nicomar.paintableblocks.interaction.VolumeEditing.READY&&s.phase()==net.nicomar.paintableblocks.interaction.VolumeEditing.SCANNING){key=next;cursor=0;weight=0;limited=false;pieces.clear();stack=ItemStack.EMPTY;}
        phase=s.phase();if(limited||cursor>=r.size())return;
        long deadline=System.nanoTime()+1_000_000;int budget=2;boolean changed=false;
        while(cursor<r.size()&&budget-->0&&System.nanoTime()<deadline){
            var pos=r.position(cursor++);if(!client.level.hasChunkAt(pos)){limited=true;break;}
            BlockDesign d;
            if(s.create()){
                if(!client.level.getBlockState(pos).isAir()||!client.level.getFluidState(pos).isEmpty())continue;
                d=BlockDesign.full(s.resolution());
            }else{
                if(!(client.level.getBlockEntity(pos) instanceof PaintableBlockEntity be)||!be.valid())continue;d=be.design();
            }
            try{
                var source=s.bothEnds()?r.doubleSeed(pos,d):d;
                var result=source.withSlope(r.doublePlane(pos,d,s.direction(),s.upper(),s.vertical(),s.start(),s.end(),s.create(),s.combined(),s.fullLength(),s.fullAtStart(),s.bothEnds(),s.otherLength()));
                if(result.empty())continue;
                int cost=DesignGeometry.surfaces(result).size();if(weight+cost>32768){limited=true;break;}weight+=cost;
                pieces.add(new PreviewScene.Piece(pos,result));changed=true;
            }catch(IllegalArgumentException|IllegalStateException incompatible){limited=true;break;}
        }
        if(changed){stack=ModContent.stack(BlockDesign.full(1));stack.set(ModContent.PREVIEW_SCENE.get(),new PreviewScene(pieces));}
    }
}
