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
import net.nicomar.paintableblocks.design.SlopeGeometry;
import net.nicomar.paintableblocks.design.SlopePlane;
import net.nicomar.paintableblocks.paint.PaintCatalog;
import org.joml.Vector3f;

/** A resource-reload-scoped mesh factory shared by world and inventory projections. */
public final class DesignMesh {
    private final BlockStateModelPart base;
    private final Map<Integer,List<BakedQuad>> samples=new ConcurrentHashMap<>();
    private final net.nicomar.paintableblocks.performance.WorkCache<MeshKey,Mesh> meshes=new net.nicomar.paintableblocks.performance.WorkCache<>("meshes",256,262144);
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
    private record MeshKey(BlockDesign design,Map<Integer,List<List<Vec3>>> exterior){}
    public Mesh mesh(BlockDesign design){return mesh(design,Map.of());}
    public Mesh mesh(BlockDesign design,Map<Integer,List<List<Vec3>>> exterior){
        return meshes.get(new MeshKey(design,exterior),()->build(design,exterior),m->m.quads().getAll().size()+exterior.size()+design.paints().size()+64);
    }
    private Mesh build(BlockDesign design,Map<Integer,List<List<Vec3>>> exterior){
        List<BakedQuad> quads=new ArrayList<>();
        for(var patch:net.nicomar.paintableblocks.design.SurfaceGroups.merge(design,exterior)){
            var surface=patch.surface();int id=design.paint(surface);var source=samples.computeIfAbsent(id*6+surface.face().ordinal(),k->sample(id,surface.face()));int before=quads.size();
            for(var quad:source)project(design,surface,quad,quads,patch.polygon());if(quads.size()==before)for(var quad:baseQuads(surface.face()))project(design,surface,quad,quads,patch.polygon());
        }
        QuadCollection.Builder builder=new QuadCollection.Builder();int flags=0;
        for(BakedQuad quad:quads){builder.addUnculledFace(quad);flags|=quad.materialInfo().flags();}
        return new Mesh(builder.build(),new Part(List.copyOf(quads),particle(),flags));
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
    private static void project(BlockDesign design,DesignGeometry.Surface surface,BakedQuad source,List<BakedQuad> output){project(design,surface,source,output,SlopeGeometry.polygon(design,surface));}
    private static void project(BlockDesign design,DesignGeometry.Surface surface,BakedQuad source,List<BakedQuad> output,List<Vec3> polygon){
        Direction face=surface.face();int axis=face.getAxis()==Direction.Axis.X?0:face.getAxis()==Direction.Axis.Y?1:2;
        int u=axis==0?2:0,v=axis==1?2:1;
        List<Vertex> vertices=new ArrayList<>();
        for(int i=0;i<4;i++){var p=source.position(i);long uv=source.packedUV(i);vertices.add(new Vertex(new Vec3(p.x(),p.y(),p.z()),UVPair.unpackU(uv),UVPair.unpackV(uv),source.bakedColors().color(i)));}
        if(polygon.size()<3)return;
        // Clip texture-layer footprints in the dominant projection before lifting onto the real polygon.
        double signed=0;for(int i=0;i<polygon.size();i++){var a=polygon.get(i);var b=polygon.get((i+1)%polygon.size());signed+=coordinate(a,u)*coordinate(b,v)-coordinate(b,u)*coordinate(a,v);}
        double sign=signed>=0?1:-1;
        for(int edge=0;edge<polygon.size();edge++){
            var a=polygon.get(edge);var b=polygon.get((edge+1)%polygon.size());if(vertices.isEmpty())return;
            List<Vertex> clipped=new ArrayList<>();Vertex prev=vertices.getLast();double dp=side(prev.position,a,b,u,v)*sign;
            for(Vertex next:vertices){double dn=side(next.position,a,b,u,v)*sign;if((dp>=-1e-10)!=(dn>=-1e-10))clipped.add(prev.interpolate(next,dp/(dp-dn)));if(dn>=-1e-10)clipped.add(next);prev=next;dp=dn;}vertices=clipped;
        }
        List<Vertex> unique=new ArrayList<>();for(Vertex vertex:vertices)if(unique.isEmpty()||unique.getLast().position.distanceToSqr(vertex.position)>1e-16)unique.add(vertex);
        if(unique.size()>1&&unique.getFirst().position.distanceToSqr(unique.getLast().position)<1e-16)unique.removeLast();vertices=unique;if(vertices.size()<3)return;
        var normal=SlopeGeometry.normal(design,surface);double constant=normal.dot(polygon.getFirst());
        List<Vertex> transformed=new ArrayList<>();
        for(Vertex vertex:vertices){Vec3 p=vertex.position;double[] xyzp={p.x,p.y,p.z};xyzp[axis]=(constant-coordinate(normal,u)*xyzp[u]-coordinate(normal,v)*xyzp[v])/coordinate(normal,axis);
            transformed.add(new Vertex(DesignOrientation.rotate(design.orientation(),new Vec3(xyzp[0],xyzp[1],xyzp[2])),vertex.u,vertex.v,vertex.color));}
        Direction direction=DesignOrientation.direction(design.orientation(),face);
        if(transformed.size()==4)emit(transformed.get(0),transformed.get(1),transformed.get(2),transformed.get(3),direction,source.materialInfo(),output);
        else for(int i=1;i+1<transformed.size();i++)emit(transformed.get(0),transformed.get(i),transformed.get(i+1),transformed.get(i+1),direction,source.materialInfo(),output);
    }
    private static double side(Vec3 p,Vec3 a,Vec3 b,int u,int v){return (coordinate(b,u)-coordinate(a,u))*(coordinate(p,v)-coordinate(a,v))-(coordinate(b,v)-coordinate(a,v))*(coordinate(p,u)-coordinate(a,u));}
    private static void emit(Vertex a,Vertex b,Vertex c,Vertex d,Direction face,BakedQuad.MaterialInfo material,List<BakedQuad> output){
        if(b.position.subtract(a.position).cross(c.position.subtract(a.position)).lengthSqr()<1e-18)return;
        var normal=b.position.subtract(a.position).cross(c.position.subtract(a.position)).normalize();
        output.add(new BakedQuad(vector(a),vector(b),vector(c),vector(d),UVPair.pack(a.u,a.v),UVPair.pack(b.u,b.v),UVPair.pack(c.u,c.v),UVPair.pack(d.u,d.v),face,material,
                BakedNormals.of(BakedNormals.pack((float)normal.x,(float)normal.y,(float)normal.z)),BakedColors.of(a.color,b.color,c.color,d.color)));
    }
    private static Vector3f vector(Vertex v){return new Vector3f((float)v.position.x,(float)v.position.y,(float)v.position.z);}
}
