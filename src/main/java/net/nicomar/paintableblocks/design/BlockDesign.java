package net.nicomar.paintableblocks.design;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.Direction;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.paint.PaintCatalog;

/** Immutable and canonical. Paint is stored only on exposed local faces; 0 means original texture. */
public final class BlockDesign {
    public static final int VERSION = 2;
    private record Face(int key, int paint) {
        private static final Codec<Face> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("key").forGetter(Face::key), Codec.INT.fieldOf("paint").forGetter(Face::paint)).apply(i, Face::new));
    }
    private record Data(int version, int resolution, List<Long> occupied, List<Face> paints, int orientation, java.util.Optional<SlopePlane> slope) {
        private static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("version").forGetter(Data::version), Codec.INT.fieldOf("resolution").forGetter(Data::resolution),
                Codec.LONG.listOf(1,64).fieldOf("occupied").forGetter(Data::occupied), Face.CODEC.listOf(0,28672).fieldOf("paints").forGetter(Data::paints),
                Codec.INT.fieldOf("orientation").forGetter(Data::orientation), SlopePlane.CODEC.optionalFieldOf("slope").forGetter(Data::slope)).apply(i, Data::new));
    }
    public static final Codec<BlockDesign> CODEC = Data.CODEC.flatXmap(BlockDesign::decode, d -> DataResult.success(d.data()));
    public static final class GeometryKey {
        private final int resolution,hash;private final List<Long> occupied;private final SlopePlane slope;
        public GeometryKey(int resolution,List<Long> occupied,SlopePlane slope){this.resolution=resolution;this.occupied=List.copyOf(occupied);this.slope=slope;hash=java.util.Objects.hash(resolution,this.occupied,slope);}
        public int resolution(){return resolution;}public List<Long> occupied(){return occupied;}public SlopePlane slope(){return slope;}
        @Override public int hashCode(){return hash;}
        @Override public boolean equals(Object o){return o instanceof GeometryKey k&&resolution==k.resolution&&java.util.Objects.equals(slope,k.slope)&&occupied.equals(k.occupied);}
    }
    private final GeometryKey geometryKey;
    public GeometryKey geometryKey(){return geometryKey;}
    private final int resolution, orientation;
    private final SlopePlane slope;
    public SlopePlane slope(){return slope;}
    public static final int SLOPE_KEY=24576;
    private final BitSet occupied;
    private final java.util.NavigableMap<Integer,Integer> paints;
    private final int hash;
    private final int paintHash;
    private static final net.nicomar.paintableblocks.performance.WorkCache<BlockDesign,Data> SNAPSHOTS=new net.nicomar.paintableblocks.performance.WorkCache<>("persistent-snapshots",256,262144);
    private static final java.util.concurrent.ConcurrentMap<Integer,BlockDesign> FULL=new java.util.concurrent.ConcurrentHashMap<>();
    private BlockDesign(int resolution, BitSet occupied, Map<Integer,Integer> paints, int orientation) {
        this(resolution,occupied,paints,orientation,null);
    }
    private BlockDesign(int resolution, BitSet occupied, Map<Integer,Integer> paints, int orientation,SlopePlane slope) {this(resolution,occupied,paints,orientation,slope,false);}
    private BlockDesign(int resolution, BitSet occupied, Map<Integer,Integer> paints, int orientation,SlopePlane slope,boolean trusted) {
        this.slope=slope;
        this.resolution=PaintingBalance.requireResolution(resolution);
        this.orientation=DesignOrientation.require(orientation);
        this.occupied=(BitSet)occupied.clone();
        this.geometryKey=new GeometryKey(resolution,java.util.Arrays.stream(this.occupied.toLongArray()).boxed().toList(),slope);
        if(occupied.length()>resolution*resolution*resolution) throw new IllegalArgumentException("Occupation outside block");
        if(!trusted&&slope!=null)for(int cell=this.occupied.nextSetBit(0);cell>=0;cell=this.occupied.nextSetBit(cell+1)){
            double min=planeMinimum(cell,slope);
            if(min>=-1e-10)throw new IllegalArgumentException("Occupied cell has no volume inside slope");
        }
        TreeMap<Integer,Integer> sorted = new TreeMap<>();
        for(var e:paints.entrySet()) {
            int key=e.getKey(), paint=e.getValue();
            if(!trusted&&(key<0 || key>=SLOPE_KEY+resolution*resolution*resolution || !PaintCatalog.validId(paint) || !validSurfaceKey(key))) {
                throw new IllegalArgumentException("Paint must identify a valid exposed face");
            }
            sorted.put(key,paint);
        }
        this.paints=Collections.unmodifiableNavigableMap(sorted);paintHash=sorted.hashCode();
        hash=designHash();
    }
    public static BlockDesign restore(int n,long[] words,Map<Integer,Integer> paints,int orientation,SlopePlane slope){return new BlockDesign(n,BitSet.valueOf(words),paints,orientation,slope);}
    public static BlockDesign full(int n) {
        PaintingBalance.requireResolution(n);return FULL.computeIfAbsent(n,k->{BitSet bits=new BitSet(k*k*k);bits.set(0,k*k*k);return new BlockDesign(k,bits,Map.of(),0);});
    }
    private static DataResult<BlockDesign> decode(Data data) {
        try {
            if(data.version()!=1&&data.version()!=VERSION) throw new IllegalArgumentException("Unsupported design version");
            int n=PaintingBalance.requireResolution(data.resolution());
            if(data.occupied().size()!=(n*n*n+63)/64 || data.paints().size()>7*n*n*n) throw new IllegalArgumentException("Invalid design size");
            long[] words=data.occupied().stream().mapToLong(Long::longValue).toArray();
            TreeMap<Integer,Integer> paints=new TreeMap<>();
            for(Face face:data.paints()) if(paints.put(face.key(),face.paint())!=null) throw new IllegalArgumentException("Duplicate face");
            return DataResult.success(new BlockDesign(n,BitSet.valueOf(words),paints,data.orientation(),data.version()==1?null:data.slope().orElse(null)));
        } catch(IllegalArgumentException e) { return DataResult.error(e::getMessage); }
    }
    private Data data(){return SNAPSHOTS.get(this,this::buildData,d->d.paints().size()+d.occupied().size()+1);}
    private Data buildData() {
        long[] words=java.util.Arrays.copyOf(occupied.toLongArray(),(resolution*resolution*resolution+63)/64);
        return new Data(VERSION,resolution,java.util.Arrays.stream(words).boxed().toList(),
                paints.entrySet().stream().map(e->new Face(e.getKey(),e.getValue())).toList(),orientation,java.util.Optional.ofNullable(slope));
    }
    public int resolution(){return resolution;}
    public int orientation(){return orientation;}
    public int count(){return occupied.cardinality();}
    public boolean sameGeometry(BlockDesign other){return resolution==other.resolution && orientation==other.orientation && occupied.equals(other.occupied)&&java.util.Objects.equals(slope,other.slope);}
    public boolean complete(){return slope==null&&count()==resolution*resolution*resolution;}
    public boolean empty(){return occupied.isEmpty();}
    public boolean painted(){return !paints.isEmpty();}
    public Map<Integer,Integer> paints(){return paints;}
    public int index(int x,int y,int z){return x+resolution*(y+resolution*z);}
    public int x(int cell){return cell%resolution;}
    public int y(int cell){return cell/resolution%resolution;}
    public int z(int cell){return cell/(resolution*resolution);}
    public boolean occupied(int cell){return cell>=0 && cell<resolution*resolution*resolution && occupied.get(cell);}
    public boolean occupied(int x,int y,int z){return x>=0 && y>=0 && z>=0 && x<resolution && y<resolution && z<resolution && occupied(index(x,y,z));}
    public boolean exposed(int cell,Direction face){return occupied(cell)&&!occupied(x(cell)+face.getStepX(),y(cell)+face.getStepY(),z(cell)+face.getStepZ())
            &&(slope==null||SlopeGeometry.area(SlopeGeometry.polygon(this,new DesignGeometry.Surface(cell,face)))>1e-12);}
    public boolean validSurfaceKey(int key){return key>=SLOPE_KEY?occupied(key-SLOPE_KEY)&&slope!=null&&SlopeGeometry.area(SlopeGeometry.polygon(this,new DesignGeometry.Surface(key-SLOPE_KEY,slope.dominant(),true)))>1e-12:
            key>=0&&key<resolution*resolution*resolution*6&&exposed(key/6,Direction.values()[key%6]);}
    public int paint(DesignGeometry.Surface s){return paints.getOrDefault(s.key(),0);}
    public int paint(int cell,Direction face){return paints.getOrDefault(cell*6+face.ordinal(),0);}
    public BlockDesign withOrientation(int orientation){return orientation==this.orientation?this:new BlockDesign(this,paints,orientation);}
    public BlockDesign withoutPaint(){return painted()?new BlockDesign(this,Collections.emptyNavigableMap(),orientation):this;}
    /** Trusted internal path: geometry and existing paints were validated already. */
    private BlockDesign(BlockDesign geometry,java.util.NavigableMap<Integer,Integer> paints,int orientation){this(geometry,paints,orientation,paints==geometry.paints?geometry.paintHash:paints.hashCode());}
    private BlockDesign(BlockDesign geometry,java.util.NavigableMap<Integer,Integer> paints,int orientation,int paintHash){
        resolution=geometry.resolution;this.orientation=DesignOrientation.require(orientation);slope=geometry.slope;occupied=geometry.occupied;geometryKey=geometry.geometryKey;
        this.paints=Collections.unmodifiableNavigableMap(paints);this.paintHash=paintHash;hash=designHash();
    }
    public BlockDesign paint(int cell,Direction face,int paint){return paintFaces(Map.of(cell*6+face.ordinal(),paint));}
    public BlockDesign paintFaces(Map<Integer,Integer> changes){
        boolean changed=false;
        for(var e:changes.entrySet()){
            if(!validSurfaceKey(e.getKey())||e.getValue()!=0&&!PaintCatalog.validId(e.getValue()))throw new IllegalArgumentException("Invalid changed paint surface");
            if(paints.getOrDefault(e.getKey(),0).intValue()!=e.getValue())changed=true;
        }
        if(!changed)return this;TreeMap<Integer,Integer> next=new TreeMap<>(paints);
        for(var e:changes.entrySet())if(e.getValue()==0)next.remove(e.getKey());else next.put(e.getKey(),e.getValue());
        return new BlockDesign(this,next,orientation);
    }
    public BlockDesign remove(BitSet cells){
        BitSet next=(BitSet)occupied.clone();next.andNot(cells);if(next.equals(occupied))return this;
        TreeMap<Integer,Integer> paint=new TreeMap<>(paints);paint.keySet().removeIf(k->!next.get(k>=SLOPE_KEY?k-SLOPE_KEY:k/6));
        return new BlockDesign(resolution,next,paint,orientation,slope,true);
    }
    public BlockDesign converted(int n){
        if(!complete()||painted())throw new IllegalStateException("Conversion requires a complete unpainted block");
        return full(n).withOrientation(orientation);
    }
    public BlockDesign physical(){
        if(orientation==0)return this;
        BitSet bits=new BitSet();TreeMap<Integer,Integer> paint=new TreeMap<>();
        for(int cell=occupied.nextSetBit(0);cell>=0;cell=occupied.nextSetBit(cell+1)){
            int[] p=DesignOrientation.cell(orientation,x(cell),y(cell),z(cell),resolution);int target=index(p[0],p[1],p[2]);bits.set(target);
            for(Direction face:Direction.values()){
                int id=paint(cell,face);if(id!=0)paint.put(target*6+DesignOrientation.direction(orientation,face).ordinal(),id);
            }
        }
        if(slope!=null)for(var e:paints.entrySet())if(e.getKey()>=SLOPE_KEY){int cell=e.getKey()-SLOPE_KEY;int[] p=DesignOrientation.cell(orientation,x(cell),y(cell),z(cell),resolution);paint.put(SLOPE_KEY+index(p[0],p[1],p[2]),e.getValue());}
        return new BlockDesign(resolution,bits,paint,0,slope==null?null:slope.rotated(orientation));
    }
    public ClipTask clipTask(SlopePlane plane){return new ClipTask(plane);}
    public BlockDesign withSlope(SlopePlane plane){var task=clipTask(plane);while(!task.advance(4096)){}return task.result();}
    /** Resumable pure operation: cells and paint validation are separately budgeted. */
    public final class ClipTask {
        private final SlopePlane plane;private final BitSet bits=(BitSet)occupied.clone();private int cell,hash;private boolean changed,geometryDone;private BlockDesign bare,result;private java.util.Iterator<Map.Entry<Integer,Integer>> paintIterator;private final TreeMap<Integer,Integer> kept=new TreeMap<>();
        private ClipTask(SlopePlane plane){this.plane=plane;if(slope!=null){if(!slope.equals(plane))throw new IllegalArgumentException("A piece accepts only its existing slope plane");result=BlockDesign.this;}}
        public boolean advance(int budget){
            if(result!=null)return true;
            while(budget-->0&&!geometryDone){if(cell==resolution*resolution*resolution){geometryDone=true;break;}int c=cell++;if(!bits.get(c))continue;double min=planeMinimum(c,plane),max=min+(Math.abs(plane.a())+Math.abs(plane.b())+Math.abs(plane.c()))/(double)resolution;if(min>=-1e-10){bits.clear(c);changed=true;}else if(max>1e-10)changed=true;}
            if(!geometryDone)return false;
            if(!changed){result=BlockDesign.this;return true;}
            if(bare==null){bare=new BlockDesign(resolution,bits,Map.of(),orientation,plane,true);paintIterator=paints.entrySet().iterator();}
            while(budget-->0&&paintIterator.hasNext()){var e=paintIterator.next();if(bare.validSurfaceKey(e.getKey())){kept.put(e.getKey(),e.getValue());hash+=e.getKey().hashCode()^e.getValue().hashCode();}}
            if(paintIterator.hasNext())return false;result=new BlockDesign(bare,kept,orientation,hash);return true;
        }
        public BlockDesign result(){if(result==null)throw new IllegalStateException("Unfinished clip task");return result;}
    }
    private int designHash(){int out=1;out=31*out+resolution;out=31*out+orientation;out=31*out+occupied.hashCode();out=31*out+paintHash;return 31*out+java.util.Objects.hashCode(slope);}
    private double planeMinimum(int cell,SlopePlane p){return (p.a()*x(cell)+p.b()*y(cell)+p.c()*z(cell)+Math.min(0,p.a())+Math.min(0,p.b())+Math.min(0,p.c()))/(double)resolution-p.offset();}
    @Override public boolean equals(Object o){return o instanceof BlockDesign d && resolution==d.resolution && orientation==d.orientation && occupied.equals(d.occupied)&&paints.equals(d.paints)&&java.util.Objects.equals(slope,d.slope);}
    @Override public int hashCode(){return hash;}
}
