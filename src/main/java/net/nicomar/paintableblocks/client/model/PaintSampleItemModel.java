package net.nicomar.paintableblocks.client.model;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.nicomar.paintableblocks.design.BlockDesign;
import net.nicomar.paintableblocks.registry.ModContent;
import net.nicomar.paintableblocks.paint.PaintCatalog;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** A small painted 3D bottle body with a fixed metal cap and handle. */
public final class PaintSampleItemModel implements ItemModel {
    private final DesignMesh meshes;
    private final QuadCollection cap;
    private final ModelRenderProperties properties;
    private final Matrix4fc transform;
    private PaintSampleItemModel(DesignMesh meshes,QuadCollection cap,ModelRenderProperties properties,Matrix4fc transform){this.meshes=meshes;this.cap=cap;this.properties=properties;this.transform=transform;}
    @Override public void update(ItemStackRenderState state,ItemStack stack,ItemModelResolver resolver,ItemDisplayContext display,ClientLevel level,ItemOwner owner,int seed){
        Integer id=stack.get(ModContent.PAINT_ID.get());if(id==null||!PaintCatalog.validId(id))return;
        BlockDesign design=BlockDesign.full(1);for(Direction face:Direction.values())design=design.paint(0,face,id);
        var out=new QuadCollection.Builder().addAll(cap);
        for(var q:meshes.mesh(design).quads().getAll())out.addUnculledFace(new BakedQuad(scale(q.position0()),scale(q.position1()),scale(q.position2()),scale(q.position3()),
                q.packedUV0(),q.packedUV1(),q.packedUV2(),q.packedUV3(),q.direction(),q.materialInfo(),q.bakedNormals(),q.bakedColors()));
        new CuboidItemModelWrapper(List.of(),out.build(),properties,transform).update(state,stack,resolver,display,level,owner,seed);
    }
    private static Vector3f scale(Vector3fc p){return new Vector3f(.25F+p.x()*.5F,.125F+p.y()*.5F,.25F+p.z()*.5F);}
    public record Unbaked(Identifier baseModel,Identifier capModel) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC=com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(i->i.group(
                Identifier.CODEC.fieldOf("base_model").forGetter(Unbaked::baseModel),Identifier.CODEC.fieldOf("cap_model").forGetter(Unbaked::capModel)).apply(i,Unbaked::new));
        @Override public MapCodec<? extends ItemModel.Unbaked> type(){return CODEC;}
        @Override public void resolveDependencies(Resolver resolver){resolver.markDependency(baseModel);resolver.markDependency(capModel);}
        @Override public ItemModel bake(BakingContext context,Matrix4fc transform){
            var baker=context.blockModelBaker();var model=baker.getModel(baseModel);var cap=new Variant(capModel).bake(baker);
            var out=new QuadCollection.Builder();for(Direction face:Direction.values())for(var q:cap.getQuads(face))out.addUnculledFace(q);
            for(var q:cap.getQuads(null))out.addUnculledFace(q);
            return new PaintSampleItemModel(DesignMesh.bake(baker,baseModel),out.build(),ModelRenderProperties.fromResolvedModel(baker,model,model.getTopTextureSlots()),transform);
        }
    }
}
