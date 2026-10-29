package net.nicomar.paintableblocks;

import com.mojang.serialization.JsonOps;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.design.*;

/** Dependency-free contract harness on the game's development classpath. Not executed without build authorization. */
public final class DesignContractChecks {
    private static int checks;
    private static void check(boolean result,String message){checks++;if(!result)throw new AssertionError(message);}
    private static void rejects(Runnable action){try{action.run();throw new AssertionError("Invalid design accepted");}catch(IllegalArgumentException|IllegalStateException expected){checks++;}}
    public static void main(String[] args){
        rotations();geometry();persistence();SlopeVolumeContractChecks.run();PerformanceContractChecks.run();
        System.out.println("Design contract checks: "+checks);
    }
    private static void rotations(){
        HashSet<Vec3> axes=new HashSet<>();
        for(int rotation=0;rotation<24;rotation++){
            check(DesignOrientation.compose(rotation,DesignOrientation.inverse(rotation))==0,"Rotation inverse");
            axes.add(DesignOrientation.rotate(rotation,new Vec3(.125,.375,.875)));
            for(int other=0;other<24;other++)check(DesignOrientation.compose(rotation,other)<24,"Closure");
            for(int n:PaintingBalance.RESOLUTIONS){
                BitSet visited=new BitSet(n*n*n);
                for(int z=0;z<n;z++)for(int y=0;y<n;y++)for(int x=0;x<n;x++){
                    int[] p=DesignOrientation.cell(rotation,x,y,z,n);
                    check(p[0]>=0&&p[1]>=0&&p[2]>=0&&p[0]<n&&p[1]<n&&p[2]<n,"Rotation inside grid");
                    int c=p[0]+n*(p[1]+n*p[2]);check(!visited.get(c),"Rotation bijection");visited.set(c);
                }
                check(visited.cardinality()==n*n*n,"Rotation preserves occupancy");
            }
            BlockDesign painted=BlockDesign.full(2).paint(0,Direction.DOWN,337).withOrientation(rotation).physical();
            check(painted.paints().size()==1,"Paint rotates with geometry");
        }
        check(axes.size()==24,"Exactly 24 orientations, not reflections or duplicates");
    }
    private static void geometry(){
        for(int n:PaintingBalance.RESOLUTIONS){
            BlockDesign d=BlockDesign.full(n);
            check(DesignGeometry.surfaces(d).size()==6*n*n,"Full-block exposed area");
            check(DesignGeometry.boxes(d).size()==1,"Full block merges into one collision box");
            check(PaintingBalance.surfaceUnits(n)*n*n==256,"Constant area consumption");
        }
        BlockDesign carved=BlockDesign.full(16);BitSet remove=new BitSet(4096);
        for(int c=0;c<4096;c++)if((carved.x(c)+carved.y(c)+carved.z(c))%2==1)remove.set(c);
        carved=carved.remove(remove);
        check(carved.count()==2048,"Disconnected checkerboard retained");
        check(DesignGeometry.surfaces(carved).size()==12288,"All faces of disconnected cells exposed");
        check(DesignGeometry.boxes(carved).size()==2048,"No collision bridges across checkerboard holes");
        Random random=new Random(2620088L);
        for(int sample=0;sample<40;sample++){
            int n=sample%2==0?4:16;BlockDesign d=BlockDesign.full(n);BitSet cuts=new BitSet(n*n*n);
            for(int c=0;c<n*n*n;c++)if(random.nextBoolean())cuts.set(c);
            d=d.remove(cuts).withOrientation(sample%24);BlockDesign physical=d.physical();BitSet covered=new BitSet(n*n*n);
            for(var box:DesignGeometry.boxes(d))for(int z=box.z0();z<box.z1();z++)for(int y=box.y0();y<box.y1();y++)for(int x=box.x0();x<box.x1();x++){
                int c=physical.index(x,y,z);check(physical.occupied(c)&&!covered.get(c),"Boxes match occupancy without overlap");covered.set(c);
            }
            check(covered.cardinality()==physical.count(),"Boxes preserve total solid volume");
        }
        BlockDesign tunnel=BlockDesign.full(16);BitSet cuts=new BitSet();
        for(int z=0;z<8;z++)cuts.set(tunnel.index(8,8,z));tunnel=tunnel.remove(cuts);
        var hit=DesignGeometry.shape(tunnel).clip(new Vec3(8.5/16,8.5/16,-1),new Vec3(8.5/16,8.5/16,2),BlockPos.ZERO);
        check(hit!=null&&Math.abs(hit.getLocation().z-.5)<1e-8,"Ray passes through hole to interior face");
        check(DesignGeometry.target(tunnel,hit.getLocation(),hit.getDirection()).orElseThrow().cell()==tunnel.index(8,8,8),"Interior selection targets actual remaining subcube");
    }
    private static void persistence(){
        BlockDesign original=BlockDesign.full(4).paint(0,Direction.DOWN,337).paint(0,Direction.NORTH,1).withOrientation(17);
        var encoded=BlockDesign.CODEC.encodeStart(JsonOps.INSTANCE,original).getOrThrow();
        check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow().equals(original),"Versioned codec round trip");
        var invalid=encoded.deepCopy().getAsJsonObject();invalid.addProperty("version",BlockDesign.VERSION+1);
        check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,invalid).error().isPresent(),"Unknown format rejected");
        invalid=encoded.deepCopy().getAsJsonObject();var faces=invalid.getAsJsonArray("paints");faces.add(faces.get(0).deepCopy());
        check(BlockDesign.CODEC.parse(JsonOps.INSTANCE,invalid).error().isPresent(),"Duplicate face rejected");
        rejects(()->BlockDesign.full(4).paint(BlockDesign.full(4).index(1,1,1),Direction.NORTH,1));
        rejects(()->BlockDesign.full(4).paint(0,Direction.NORTH,338));
        rejects(()->BlockDesign.full(3));
        rejects(()->original.converted(8));
        BitSet removed=new BitSet();removed.set(0);BlockDesign cut=original.remove(removed);
        check(cut.paints().isEmpty(),"Paint on removed geometry is discarded");
        check(cut.withoutPaint().count()==cut.count(),"Cleaning never restores geometry");
        rejects(()->cut.converted(2));
        check(BlockDesign.full(4).converted(16).count()==4096,"Conversion creates only complete unpainted resolution");
        BlockDesign a=BlockDesign.full(2).paint(0,Direction.NORTH,1).paint(0,Direction.DOWN,2);
        BlockDesign b=BlockDesign.full(2).paint(0,Direction.DOWN,2).paint(0,Direction.NORTH,1);
        check(a.equals(b)&&a.hashCode()==b.hashCode(),"Equivalent paint insertion orders stack canonically");
    }
}
