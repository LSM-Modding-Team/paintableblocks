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
    public static final int VERSION = 1;
    private record Face(int key, int paint) {
        private static final Codec<Face> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("key").forGetter(Face::key), Codec.INT.fieldOf("paint").forGetter(Face::paint)).apply(i, Face::new));
    }
    private record Data(int version, int resolution, List<Long> occupied, List<Face> paints, int orientation) {
        private static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("version").forGetter(Data::version), Codec.INT.fieldOf("resolution").forGetter(Data::resolution),
                Codec.LONG.listOf(1,64).fieldOf("occupied").forGetter(Data::occupied), Face.CODEC.listOf(0,24576).fieldOf("paints").forGetter(Data::paints),
                Codec.INT.fieldOf("orientation").forGetter(Data::orientation)).apply(i, Data::new));
    }
    public static final Codec<BlockDesign> CODEC = Data.CODEC.flatXmap(BlockDesign::decode, d -> DataResult.success(d.data()));
    private final int resolution, orientation;
    private final BitSet occupied;
    private final Map<Integer,Integer> paints;
    private final int hash;
    private BlockDesign(int resolution, BitSet occupied, Map<Integer,Integer> paints, int orientation) {
        this.resolution=PaintingBalance.requireResolution(resolution);
        this.orientation=DesignOrientation.require(orientation);
        this.occupied=(BitSet)occupied.clone();
        if(occupied.length()>resolution*resolution*resolution) throw new IllegalArgumentException("Occupation outside block");
        TreeMap<Integer,Integer> sorted = new TreeMap<>();
        for(var e:paints.entrySet()) {
            int key=e.getKey(), paint=e.getValue();
            if(key<0 || key>=resolution*resolution*resolution*6 || !PaintCatalog.validId(paint) || !exposed(key/6,Direction.values()[key%6])) {
                throw new IllegalArgumentException("Paint must identify a valid exposed face");
            }
            sorted.put(key,paint);
        }
        this.paints=Collections.unmodifiableMap(sorted);
        hash=java.util.Objects.hash(resolution,orientation,this.occupied,this.paints);
    }
    public static BlockDesign full(int n) {
        PaintingBalance.requireResolution(n);BitSet bits=new BitSet(n*n*n);bits.set(0,n*n*n);
        return new BlockDesign(n,bits,Map.of(),0);
    }
    private static DataResult<BlockDesign> decode(Data data) {
        try {
            if(data.version()!=VERSION) throw new IllegalArgumentException("Unsupported design version");
            int n=PaintingBalance.requireResolution(data.resolution());
            if(data.occupied().size()!=(n*n*n+63)/64 || data.paints().size()>6*n*n*n) throw new IllegalArgumentException("Invalid design size");
            long[] words=data.occupied().stream().mapToLong(Long::longValue).toArray();
            TreeMap<Integer,Integer> paints=new TreeMap<>();
            for(Face face:data.paints()) if(paints.put(face.key(),face.paint())!=null) throw new IllegalArgumentException("Duplicate face");
            return DataResult.success(new BlockDesign(n,BitSet.valueOf(words),paints,data.orientation()));
        } catch(IllegalArgumentException e) { return DataResult.error(e::getMessage); }
    }
    private Data data() {
        long[] words=java.util.Arrays.copyOf(occupied.toLongArray(),(resolution*resolution*resolution+63)/64);
        return new Data(VERSION,resolution,java.util.Arrays.stream(words).boxed().toList(),
                paints.entrySet().stream().map(e->new Face(e.getKey(),e.getValue())).toList(),orientation);
    }
    public int resolution(){return resolution;}
    public int orientation(){return orientation;}
    public int count(){return occupied.cardinality();}
    public boolean sameGeometry(BlockDesign other){return resolution==other.resolution && orientation==other.orientation && occupied.equals(other.occupied);}
    public boolean complete(){return count()==resolution*resolution*resolution;}
    public boolean empty(){return occupied.isEmpty();}
    public boolean painted(){return !paints.isEmpty();}
    public Map<Integer,Integer> paints(){return paints;}
    public int index(int x,int y,int z){return x+resolution*(y+resolution*z);}
    public int x(int cell){return cell%resolution;}
    public int y(int cell){return cell/resolution%resolution;}
    public int z(int cell){return cell/(resolution*resolution);}
    public boolean occupied(int cell){return cell>=0 && cell<resolution*resolution*resolution && occupied.get(cell);}
    public boolean occupied(int x,int y,int z){return x>=0 && y>=0 && z>=0 && x<resolution && y<resolution && z<resolution && occupied(index(x,y,z));}
    public boolean exposed(int cell,Direction face){return occupied(cell)&&!occupied(x(cell)+face.getStepX(),y(cell)+face.getStepY(),z(cell)+face.getStepZ());}
    public int paint(int cell,Direction face){return paints.getOrDefault(cell*6+face.ordinal(),0);}
    public BlockDesign withOrientation(int orientation){return new BlockDesign(resolution,occupied,paints,orientation);}
    public BlockDesign withoutPaint(){return painted()?new BlockDesign(resolution,occupied,Map.of(),orientation):this;}
    public BlockDesign paint(int cell,Direction face,int paint){
        if(!exposed(cell,face))throw new IllegalArgumentException("Surface is hidden or absent");
        TreeMap<Integer,Integer> next=new TreeMap<>(paints);
        if(paint==0)next.remove(cell*6+face.ordinal());else next.put(cell*6+face.ordinal(),paint);
        return new BlockDesign(resolution,occupied,next,orientation);
    }
    public BlockDesign remove(BitSet cells){
        BitSet next=(BitSet)occupied.clone();next.andNot(cells);
        TreeMap<Integer,Integer> paint=new TreeMap<>(paints);paint.keySet().removeIf(k->!next.get(k/6));
        return new BlockDesign(resolution,next,paint,orientation);
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
        return new BlockDesign(resolution,bits,paint,0);
    }
    @Override public boolean equals(Object o){return o instanceof BlockDesign d && resolution==d.resolution && orientation==d.orientation && occupied.equals(d.occupied)&&paints.equals(d.paints);}
    @Override public int hashCode(){return hash;}
}
