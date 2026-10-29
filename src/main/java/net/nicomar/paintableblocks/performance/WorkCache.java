package net.nicomar.paintableblocks.performance;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/** Bounded, cost-aware admission; expensive values survive cheap one-off requests. No work under the lock. */
public final class WorkCache<K,V> {
    private record Entry<V>(V value,int weight,long cost){}
    private final LinkedHashMap<K,Entry<V>> entries=new LinkedHashMap<>(16,.75F,true);
    private final ConcurrentHashMap<K,CompletableFuture<V>> flights=new ConcurrentHashMap<>();
    private final int capacity,limit;private int weight;private final String name;
    private long hits,misses,waits,evictions,computeNanos;
    public record Stats(String name,int entries,int weight,int entryLimit,int weightLimit,long hits,long misses,long waits,long evictions,long computeNanos){}
    public Stats stats(){synchronized(entries){return new Stats(name,entries.size(),weight,capacity,limit,hits,misses,waits,evictions,computeNanos);}}
    public WorkCache(int capacity,int limit){this("cache",capacity,limit);}
    public WorkCache(String name,int capacity,int limit){if(capacity<1||limit<1)throw new IllegalArgumentException("Invalid cache budget");this.name=name;this.capacity=capacity;this.limit=limit;PerformanceMetrics.register(this);}
    public void clear(){synchronized(entries){entries.clear();weight=0;}}
    public V getIfPresent(K key){synchronized(entries){var entry=entries.get(key);return entry==null?null:entry.value;}}
    public V get(K key,Supplier<V> factory,ToIntFunction<V> weigh){
        synchronized(entries){var e=entries.get(key);if(e!=null){hits++;return e.value;}misses++;}
        var mine=new CompletableFuture<V>();var flight=flights.putIfAbsent(key,mine);if(flight!=null){synchronized(entries){waits++;}return flight.join();}
        try{
            synchronized(entries){var e=entries.get(key);if(e!=null){mine.complete(e.value);return e.value;}}
            long start=System.nanoTime();V value=factory.get();int size=Math.max(1,weigh.applyAsInt(value));long cost=Math.max(1,System.nanoTime()-start);synchronized(entries){computeNanos+=cost;}
            if(size<=limit)synchronized(entries){
                // Prefer costly results among the oldest quarter, rather than blindly evicting the oldest.
                while(!entries.isEmpty()&&(entries.size()>=capacity||weight+size>limit)){
                    K victim=null;long score=Long.MAX_VALUE;int count=0;
                    for(var e:entries.entrySet()){long s=e.getValue().cost/Math.max(1,e.getValue().weight);if(s<score){score=s;victim=e.getKey();}if(++count>=Math.max(1,entries.size()/4))break;}
                    weight-=entries.remove(victim).weight;evictions++;
                }
                entries.put(key,new Entry<>(value,size,cost));weight+=size;
            }
            mine.complete(value);return value;
        }catch(RuntimeException|Error failure){mine.completeExceptionally(failure);throw failure;}
        finally{flights.remove(key,mine);}
    }
}
