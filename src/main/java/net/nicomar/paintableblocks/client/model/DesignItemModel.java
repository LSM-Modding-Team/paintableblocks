package net.nicomar.paintableblocks.client.model;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.nicomar.paintableblocks.design.BlockDesign;
import net.nicomar.paintableblocks.registry.ModContent;
import org.joml.Matrix4fc;

/** Same projection factory as world blocks; the native wrapper supplies GUI/hand/ground transforms. */
public final class DesignItemModel implements ItemModel {
    private final DesignMesh meshes;
    private final ModelRenderProperties properties;
    private final Matrix4fc transform;
    public DesignItemModel(DesignMesh meshes,ModelRenderProperties properties,Matrix4fc transform){this.meshes=meshes;this.properties=properties;this.transform=transform;}
    @Override public void update(ItemStackRenderState state,ItemStack stack,ItemModelResolver resolver,ItemDisplayContext display,ClientLevel level,ItemOwner owner,int seed){
        BlockDesign design=stack.get(ModContent.DESIGN.get());
        if(design==null||design.empty())return;
        new CuboidItemModelWrapper(List.of(),meshes.mesh(design).quads(),properties,transform).update(state,stack,resolver,display,level,owner,seed);
    }
    public record Unbaked(Identifier baseModel) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC=Identifier.CODEC.fieldOf("base_model").xmap(Unbaked::new,Unbaked::baseModel);
        @Override public MapCodec<? extends ItemModel.Unbaked> type(){return CODEC;}
        @Override public void resolveDependencies(Resolver resolver){resolver.markDependency(baseModel);}
        @Override public ItemModel bake(BakingContext context,Matrix4fc transform){
            var baker=context.blockModelBaker();var model=baker.getModel(baseModel);
            return new DesignItemModel(DesignMesh.bake(baker,baseModel),
                    ModelRenderProperties.fromResolvedModel(baker,model,model.getTopTextureSlots()),transform);
        }
    }
}
