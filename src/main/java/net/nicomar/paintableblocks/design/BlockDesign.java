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
    public record GeometryKey(int resolution,List<Long> occupied,SlopePlane slope){public GeometryKey{occupied=List.copyOf(occupied);}}
    private final GeometryKey geometryKey;
    public GeometryKey geometryKey(){return geometryKey;}
    private final int resolution, orientation;
    private final SlopePlane slope;
    public SlopePlane slope(){return slope;}
    public static final int SLOPE_KEY=24576;
    private final BitSet occupied;
    private final Map<Integer,Integer> paints;
    private final int hash;
    private BlockDesign(int resolution, BitSet occupied, Map<Integer,Integer> paints, int orientation) {
        this(resolution,occupied,paints,orientation,null);
    }
    private BlockDesign(int resolution, BitSet occupied, Map<Integer,Integer> paints, int orientation,SlopePlane slope) {
        this.slope=slope;
        this.resolution=PaintingBalance.requireResolution(resolution);
        this.orientation=DesignOrientation.require(orientation);
        this.occupied=(BitSet)occupied.clone();
        this.geometryKey=new GeometryKey(resolution,java.util.Arrays.stream(this.occupied.toLongArray()).boxed().toList(),slope);
        if(occupied.length()>resolution*resolution*resolution) throw new IllegalArgumentException("Occupation outside block");
        if(slope!=null)for(int cell=this.occupied.nextSetBit(0);cell>=0;cell=this.occupied.nextSetBit(cell+1)){
            double min=Double.POSITIVE_INFINITY;for(var corner:corners(cell))min=Math.min(min,slope.a()*corner.x+slope.b()*corner.y+slope.c()*corner.z-slope.offset());
            if(min>=-1e-10)throw new IllegalArgumentException("Occupied cell has no volume inside slope");
        }
        TreeMap<Integer,Integer> sorted = new TreeMap<>();
        for(var e:paints.entrySet()) {
            int key=e.getKey(), paint=e.getValue();
            if(key<0 || key>=SLOPE_KEY+resolution*resolution*resolution || !PaintCatalog.validId(paint) || !validSurfaceKey(key)) {
                throw new IllegalArgumentException("Paint must identify a valid exposed face");
            }
            sorted.put(key,paint);
        }
        this.paints=Collections.unmodifiableMap(sorted);
        hash=java.util.Objects.hash(resolution,orientation,this.occupied,this.paints,slope);
    }
    public static BlockDesign full(int n) {
        PaintingBalance.requireResolution(n);BitSet bits=new BitSet(n*n*n);bits.set(0,n*n*n);
        return new BlockDesign(n,bits,Map.of(),0);
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
    private Data data() {
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
    public BlockDesign withOrientation(int orientation){return new BlockDesign(resolution,occupied,paints,orientation,slope);}
    public BlockDesign withoutPaint(){return painted()?new BlockDesign(resolution,occupied,Map.of(),orientation,slope):this;}
    public BlockDesign paint(int cell,Direction face,int paint){
        if(!exposed(cell,face))throw new IllegalArgumentException("Surface is hidden or absent");
        TreeMap<Integer,Integer> next=new TreeMap<>(paints);
        if(paint==0)next.remove(cell*6+face.ordinal());else next.put(cell*6+face.ordinal(),paint);
        return new BlockDesign(resolution,occupied,next,orientation,slope);
    }
    public BlockDesign paintFaces(Map<Integer,Integer> changes){
        TreeMap<Integer,Integer> next=new TreeMap<>(paints);
        changes.forEach((key,id)->{if(id==0)next.remove(key);else next.put(key,id);});
        return new BlockDesign(resolution,occupied,next,orientation,slope);
    }
    public BlockDesign remove(BitSet cells){
        BitSet next=(BitSet)occupied.clone();next.andNot(cells);
        TreeMap<Integer,Integer> paint=new TreeMap<>(paints);paint.keySet().removeIf(k->!next.get(k>=SLOPE_KEY?k-SLOPE_KEY:k/6));
        return new BlockDesign(resolution,next,paint,orientation,slope);
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
    public BlockDesign withSlope(SlopePlane plane){
        if(slope!=null&&!slope.equals(plane))throw new IllegalArgumentException("A piece accepts only its existing slope plane");
        if(slope!=null)return this;
        BitSet bits=(BitSet)occupied.clone();boolean changed=false;
        for(int cell=bits.nextSetBit(0);cell>=0;cell=bits.nextSetBit(cell+1)){
            double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
            for(Vec3Corner corner:corners(cell)){double v=plane.a()*corner.x+plane.b()*corner.y+plane.c()*corner.z-plane.offset();min=Math.min(min,v);max=Math.max(max,v);}
            if(min>=-1e-10){bits.clear(cell);changed=true;}else if(max>1e-10)changed=true;
        }
        if(!changed)return this;
        BlockDesign bare=new BlockDesign(resolution,bits,Map.of(),orientation,plane);
        TreeMap<Integer,Integer> kept=new TreeMap<>();for(var e:paints.entrySet())if(bare.validSurfaceKey(e.getKey()))kept.put(e.getKey(),e.getValue());
        return new BlockDesign(resolution,bits,kept,orientation,plane);
    }
    private record Vec3Corner(double x,double y,double z){}
    private List<Vec3Corner> corners(int cell){java.util.ArrayList<Vec3Corner> out=new java.util.ArrayList<>();for(int i=0;i<8;i++)out.add(new Vec3Corner((x(cell)+(i&1))/(double)resolution,(y(cell)+((i>>1)&1))/(double)resolution,(z(cell)+((i>>2)&1))/(double)resolution));return out;}
    @Override public boolean equals(Object o){return o instanceof BlockDesign d && resolution==d.resolution && orientation==d.orientation && occupied.equals(d.occupied)&&paints.equals(d.paints)&&java.util.Objects.equals(slope,d.slope);}
    @Override public int hashCode(){return hash;}
}
