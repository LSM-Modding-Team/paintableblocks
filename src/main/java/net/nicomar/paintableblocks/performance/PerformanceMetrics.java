package net.nicomar.paintableblocks.performance;

import java.lang.ref.WeakReference;
import java.util.*;

/** Read on demand by a development harness/profiler. No periodic logging, world scanning or strong cache retention. */
public final class PerformanceMetrics {
    private static final List<WeakReference<WorkCache<?,?>>> CACHES=new ArrayList<>();
    static synchronized void register(WorkCache<?,?> cache){CACHES.removeIf(ref->ref.get()==null);CACHES.add(new WeakReference<>(cache));}
    public static List<WorkCache.Stats> snapshot(){
        List<WorkCache<?,?>> live=new ArrayList<>();synchronized(PerformanceMetrics.class){var iterator=CACHES.iterator();while(iterator.hasNext()){var c=iterator.next().get();if(c==null)iterator.remove();else live.add(c);}}
        return live.stream().map(WorkCache::stats).toList();
    }
    private PerformanceMetrics(){}
}
