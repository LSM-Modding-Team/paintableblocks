package net.nicomar.paintableblocks;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.FriendlyByteBuf;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.network.DesignWireCodec;
import net.nicomar.paintableblocks.performance.WorkCache;

/** Semantic regressions and deterministic work-count checks; prepared, NOT executed without Java/build authorization. */
public final class PerformanceContractChecks {
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
    public static void run(){
        for(int n:List.of(1,2,4,8,16)){
            var d=BlockDesign.full(n);check(SurfaceGroups.merge(d,Map.of()).size()==6,"Complete uniform block merges to six visual surfaces");
            var painted=d.paint(0,net.minecraft.core.Direction.DOWN,1);check(painted.geometryKey()==d.geometryKey(),"Painting shares validated geometry identity");
            check(DesignGeometry.shape(d)==DesignGeometry.shape(painted),"Collision is shared independently of paint");
            check(d.withOrientation(5).geometryKey()==d.geometryKey(),"Orientation shares local geometry");
            var slope=d.withSlope(new SlopePlane(-1,1,0,0));var task=d.clipTask(new SlopePlane(-1,1,0,0));int calls=0;while(!task.advance(1)){check(++calls<100000,"Resumable task terminates");}check(task.result().equals(slope),"Resumable and synchronous clipping match");
            var cap=DesignGeometry.surfaces(slope).stream().filter(DesignGeometry.Surface::slope).findFirst().orElseThrow();check(SlopeGeometry.polygon(slope,cap)==SlopeGeometry.polygon(slope.paintFaces(Map.of(cap.key(),1)),cap),"Polygons are reused");
            double originalArea=DesignGeometry.surfaces(slope).stream().mapToDouble(s->SlopeGeometry.area(SlopeGeometry.polygon(slope,s))).sum(),mergedArea=SurfaceGroups.merge(slope,Map.of()).stream().mapToDouble(p->SlopeGeometry.area(p.polygon())).sum();check(Math.abs(originalArea-mergedArea)<1e-8,"Visual merging preserves area");
            for(int rotation=0;rotation<24;rotation++){
                var rotated=slope.withOrientation(rotation);var bytes=new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());try{DesignWireCodec.write(bytes,rotated);check(DesignWireCodec.read(bytes).equals(rotated),"Compact wire format round trip");}finally{bytes.release();}
                var shape=DesignGeometry.shape(rotated);var boxes=shape.toAabbs();int inverse=DesignOrientation.inverse(rotation);
                for(int cell=0;cell<4096;cell++){int x=cell%16,y=cell/16%16,z=cell/256;var center=new Vec3((x+.5)/16,(y+.5)/16,(z+.5)/16);var local=DesignOrientation.rotate(inverse,center);boolean expected=rotated.occupied(Math.min(n-1,(int)(local.x*n)),Math.min(n-1,(int)(local.y*n)),Math.min(n-1,(int)(local.z*n)))&&rotated.slope().value(local)<=1e-10;
                    boolean actual=boxes.stream().anyMatch(box->box.contains(center));check(expected==actual,"Optimized collision preserves every 1/16 sample");}
            }
            var random=new Random(262088+n);for(int sample=0;sample<80;sample++){var a=new Vec3(random.nextDouble()*3-1,2,random.nextDouble()*3-1);var b=new Vec3(random.nextDouble(),-1,random.nextDouble());var actual=SlopeGeometry.ray(slope,a,b,BlockPos.ZERO);double expected=Double.POSITIVE_INFINITY;var delta=b.subtract(a);
                for(var surface:DesignGeometry.surfaces(slope)){var p=SlopeGeometry.polygon(slope,surface);var normal=SlopeGeometry.normal(slope,surface);double denominator=normal.dot(delta);if(denominator>=-1e-12)continue;double t=normal.dot(p.getFirst().subtract(a))/denominator;if(t>=0&&t<=1&&SlopeGeometry.contains(p,a.add(delta.scale(t)),normal))expected=Math.min(expected,t);}
                check((actual==null)==!Double.isFinite(expected),"Cell traversal agrees with exhaustive ray result");if(actual!=null)check(actual.getLocation().distanceToSqr(a.lerp(b,expected))<1e-14,"Closest exact hit is preserved");}
        }
        var cache=new WorkCache<Integer,Object>("contract",2,2);AtomicInteger calls=new AtomicInteger();var workers=Executors.newFixedThreadPool(8);
        try{List<Future<Object>> futures=new ArrayList<>();for(int i=0;i<8;i++)futures.add(workers.submit(()->cache.get(1,()->{calls.incrementAndGet();return new Object();},v->1)));Object first=futures.getFirst().get(10,TimeUnit.SECONDS);for(var future:futures)check(future.get(10,TimeUnit.SECONDS)==first,"Concurrent consumers share one result");check(calls.get()==1,"One shared calculation");cache.get(2,Object::new,v->1);cache.get(3,Object::new,v->1);var stats=cache.stats();check(stats.entries()<=stats.entryLimit()&&stats.weight()<=stats.weightLimit(),"Cache admission obeys both limits");}
        catch(InterruptedException e){Thread.currentThread().interrupt();throw new AssertionError(e);}catch(ExecutionException|TimeoutException e){throw new AssertionError(e);}finally{workers.shutdownNow();}
    }
}
