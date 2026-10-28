package net.nicomar.paintableblocks.client.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import net.nicomar.paintableblocks.design.BlockDesign;
import net.nicomar.paintableblocks.design.DesignGeometry;
import net.nicomar.paintableblocks.design.DesignOrientation;
import net.nicomar.paintableblocks.paint.PaintCatalog;
import org.joml.Vector3f;

/** A resource-reload-scoped mesh factory shared by world and inventory projections. */
public final class DesignMesh {
    private static final int MAX_ENTRIES=64, MAX_CACHED_QUADS=65536;
    private final BlockStateModelPart base;
    private final Map<Integer,List<BakedQuad>> samples=new ConcurrentHashMap<>();
    private final LinkedHashMap<BlockDesign,Mesh> meshes=new LinkedHashMap<>(16,.75F,true);
    private int cachedQuads;
    private final Map<BlockDesign,java.util.concurrent.CompletableFuture<Mesh>> building=new ConcurrentHashMap<>();
    private record SurfaceKey(int resolution,int cell,Direction face,int paint,int orientation){}
    private final LinkedHashMap<SurfaceKey,List<BakedQuad>> projected=new LinkedHashMap<>(16,.75F,true);
    private int projectedQuads;
    public record Mesh(QuadCollection quads,BlockStateModelPart part){}
    private record Part(List<BakedQuad> quads,Material.Baked particleMaterial,int materialFlags) implements BlockStateModelPart {
        @Override public List<BakedQuad> getQuads(Direction side){return side==null?quads:List.of();}
        @Override public boolean useAmbientOcclusion(){return true;}
    }
    private DesignMesh(BlockStateModelPart base){this.base=base;}
    public static DesignMesh bake(ModelBaker baker,Identifier baseModel){return baker.compute(new FactoryKey(baseModel));}
    private record FactoryKey(Identifier baseModel) implements ModelBaker.SharedOperationKey<DesignMesh> {
        @Override public DesignMesh compute(ModelBaker baker){return new DesignMesh(new Variant(baseModel).bake(baker));}
    }
    public Material.Baked particle(){return base.particleMaterial();}
    public Mesh mesh(BlockDesign design){
        synchronized(meshes){var present=meshes.get(design);if(present!=null)return present;}
        var future=new java.util.concurrent.CompletableFuture<Mesh>();var existing=building.putIfAbsent(design,future);
        if(existing!=null)return existing.join();
        try{
            synchronized(meshes){var raced=meshes.get(design);if(raced!=null){future.complete(raced);return raced;}}
            // Rendering different designs never shares a mesh-generation monitor.
            Mesh result=build(design);int count=result.quads().getAll().size();
            if(count<=MAX_CACHED_QUADS)synchronized(meshes){
                while(!meshes.isEmpty()&&(meshes.size()>=MAX_ENTRIES||cachedQuads+count>MAX_CACHED_QUADS)){
                    var first=meshes.entrySet().iterator();cachedQuads-=first.next().getValue().quads().getAll().size();first.remove();
                }
                meshes.put(design,result);cachedQuads+=count;
            }
            future.complete(result);return result;
        }catch(RuntimeException|Error failure){future.completeExceptionally(failure);throw failure;}
        finally{building.remove(design,future);}
    }
    private Mesh build(BlockDesign design){
        List<BakedQuad> quads=new ArrayList<>();
        for(var surface:DesignGeometry.surfaces(design))quads.addAll(surfaceQuads(design,surface));
        QuadCollection.Builder builder=new QuadCollection.Builder();int flags=0;
        for(BakedQuad quad:quads){builder.addUnculledFace(quad);flags|=quad.materialInfo().flags();}
        return new Mesh(builder.build(),new Part(List.copyOf(quads),particle(),flags));
    }
    private List<BakedQuad> surfaceQuads(BlockDesign design,DesignGeometry.Surface surface){
        int id=design.paint(surface.cell(),surface.face());
        var key=new SurfaceKey(design.resolution(),surface.cell(),surface.face(),id,design.orientation());
        synchronized(projected){var present=projected.get(key);if(present!=null)return present;}
        List<BakedQuad> quads=new ArrayList<>();
        var source=samples.computeIfAbsent(id*6+surface.face().ordinal(),k->sample(id,surface.face()));
        for(var quad:source)project(design,surface,quad,quads);
        if(quads.isEmpty())for(var quad:baseQuads(surface.face()))project(design,surface,quad,quads);
        var result=List.copyOf(quads);
        synchronized(projected){
            var raced=projected.get(key);if(raced!=null)return raced;
            while(!projected.isEmpty()&&(projected.size()>=8192||projectedQuads+result.size()>16384)){
                var first=projected.entrySet().iterator();projectedQuads-=first.next().getValue().size();first.remove();
            }
            if(result.size()<=16384){projected.put(key,result);projectedQuads+=result.size();}
        }
        return result;
    }
    private List<BakedQuad> baseQuads(Direction face){
        List<BakedQuad> out=new ArrayList<>(base.getQuads(face));
        for(var q:base.getQuads(null))if(q.direction()==face)out.add(q);return out;
    }
    private List<BakedQuad> sample(int id,Direction face){
        if(id==0)return List.copyOf(baseQuads(face));
        var state=PaintCatalog.byId(id).sampleState();
        var model=Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        List<BlockStateModelPart> parts=new ArrayList<>();model.collectParts(RandomSource.create(0),parts);
        List<BakedQuad> out=new ArrayList<>();
        for(var part:parts){
            List<BakedQuad> source=new ArrayList<>();
            // A cull direction can group inward-facing backs (notably powder snow).
            // Select the outward normal before flattening geometry to a paintable surface.
            for(var quad:part.getQuads(face))if(quad.direction()==face)source.add(quad);
            for(var quad:part.getQuads(null))if(quad.direction()==face)source.add(quad);
            for(var q:source){
                int tint=0xFFFFFFFF;
                if(q.materialInfo().isTinted()){
                    var tintSource=Minecraft.getInstance().getBlockColors().getTintSource(state,q.materialInfo().tintIndex());
                    if(tintSource!=null)tint=0xFF000000|tintSource.color(state);
                }
                var old=q.materialInfo();
                var material=new BakedQuad.MaterialInfo(old.sprite(),old.layer(),old.itemRenderType(),-1,true,0,true);
                BakedColors colors=BakedColors.of(multiply(q.bakedColors().color(0),tint),multiply(q.bakedColors().color(1),tint),
                        multiply(q.bakedColors().color(2),tint),multiply(q.bakedColors().color(3),tint));
                out.add(new BakedQuad(q.position0(),q.position1(),q.position2(),q.position3(),q.packedUV0(),q.packedUV1(),q.packedUV2(),q.packedUV3(),face,material,BakedNormals.UNSPECIFIED,colors));
            }
        }
        return List.copyOf(out);
    }
    private static int multiply(int a,int b){
        int result=0;for(int shift=0;shift<32;shift+=8)result|=(((a>>>shift)&255)*((b>>>shift)&255)/255)<<shift;return result;
    }
    private record Vertex(Vec3 position,float u,float v,int color){
        Vertex interpolate(Vertex b,double t){return new Vertex(position.add(b.position.subtract(position).scale(t)),
                (float)(u+(b.u-u)*t),(float)(v+(b.v-v)*t),lerpColor(color,b.color,t));}
    }
    private static int lerpColor(int a,int b,double t){int out=0;for(int s=0;s<32;s+=8)out|=(int)Math.round(((a>>>s)&255)+(((b>>>s)&255)-((a>>>s)&255))*t)<<s;return out;}
    private static double coordinate(Vec3 p,int axis){return axis==0?p.x:axis==1?p.y:p.z;}
    private static List<Vertex> clip(List<Vertex> polygon,int axis,double edge,boolean greater){
        if(polygon.isEmpty())return polygon;List<Vertex> out=new ArrayList<>();Vertex a=polygon.getLast();
        double da=coordinate(a.position,axis)-edge;boolean ia=greater?da>=-1e-8:da<=1e-8;
        for(Vertex b:polygon){double db=coordinate(b.position,axis)-edge;boolean ib=greater?db>=-1e-8:db<=1e-8;
            if(ia!=ib)out.add(a.interpolate(b,da/(da-db)));if(ib)out.add(b);a=b;da=db;ia=ib;
        }
        return out;
    }
    private static void project(BlockDesign design,DesignGeometry.Surface surface,BakedQuad source,List<BakedQuad> output){
        Direction face=surface.face();int axis=face.getAxis()==Direction.Axis.X?0:face.getAxis()==Direction.Axis.Y?1:2;
        int u=axis==0?2:0,v=axis==1?2:1;int n=design.resolution(),cell=surface.cell();
        int[] xyz={design.x(cell),design.y(cell),design.z(cell)};
        List<Vertex> vertices=new ArrayList<>();
        for(int i=0;i<4;i++){var p=source.position(i);long uv=source.packedUV(i);vertices.add(new Vertex(new Vec3(p.x(),p.y(),p.z()),UVPair.unpackU(uv),UVPair.unpackV(uv),source.bakedColors().color(i)));}
        vertices=clip(vertices,u,xyz[u]/(double)n,true);vertices=clip(vertices,u,(xyz[u]+1)/(double)n,false);
        vertices=clip(vertices,v,xyz[v]/(double)n,true);vertices=clip(vertices,v,(xyz[v]+1)/(double)n,false);
        List<Vertex> unique=new ArrayList<>();
        for(Vertex vertex:vertices)if(unique.isEmpty()||unique.getLast().position.distanceToSqr(vertex.position)>1e-16)unique.add(vertex);
        if(unique.size()>1&&unique.getFirst().position.distanceToSqr(unique.getLast().position)<1e-16)unique.removeLast();
        vertices=unique;
        if(vertices.size()<3)return;
        double plane=(xyz[axis]+(face.getAxisDirection()==Direction.AxisDirection.POSITIVE?1:0))/(double)n;
        List<Vertex> transformed=new ArrayList<>();
        for(Vertex vertex:vertices){Vec3 p=vertex.position;Vec3 flat=new Vec3(axis==0?plane:p.x,axis==1?plane:p.y,axis==2?plane:p.z);
            transformed.add(new Vertex(DesignOrientation.rotate(design.orientation(),flat),vertex.u,vertex.v,vertex.color));}
        Direction direction=DesignOrientation.direction(design.orientation(),face);
        if(transformed.size()==4)emit(transformed.get(0),transformed.get(1),transformed.get(2),transformed.get(3),direction,source.materialInfo(),output);
        else for(int i=1;i+1<transformed.size();i++)emit(transformed.get(0),transformed.get(i),transformed.get(i+1),transformed.get(i+1),direction,source.materialInfo(),output);
    }
    private static void emit(Vertex a,Vertex b,Vertex c,Vertex d,Direction face,BakedQuad.MaterialInfo material,List<BakedQuad> output){
        if(b.position.subtract(a.position).cross(c.position.subtract(a.position)).lengthSqr()<1e-18)return;
        output.add(new BakedQuad(vector(a),vector(b),vector(c),vector(d),UVPair.pack(a.u,a.v),UVPair.pack(b.u,b.v),UVPair.pack(c.u,c.v),UVPair.pack(d.u,d.v),face,material,
                BakedNormals.UNSPECIFIED,BakedColors.of(a.color,b.color,c.color,d.color)));
    }
    private static Vector3f vector(Vertex v){return new Vector3f((float)v.position.x,(float)v.position.y,(float)v.position.z);}
}
