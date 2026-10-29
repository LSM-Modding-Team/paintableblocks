package net.nicomar.paintableblocks.client.model;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.nicomar.paintableblocks.block.PaintableBlock;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.design.BlockDesign;

public final class PaintableBlockModel implements DynamicBlockStateModel {
    private final DesignMesh meshes;
    private record GeometryKey(BlockDesign design,java.util.List<BlockDesign> neighbors,DesignMesh resourceGeneration){}
    private java.util.List<BlockDesign> neighbors(BlockAndTintGetter level,BlockPos pos){return net.nicomar.paintableblocks.design.NeighborOcclusion.neighbors(p->level.getModelData(p).get(PaintableBlockEntity.DESIGN_PROPERTY),pos);}
    public PaintableBlockModel(DesignMesh meshes){this.meshes=meshes;}
    private BlockDesign design(BlockAndTintGetter level,BlockPos pos,BlockState state){
        BlockDesign d=level.getModelData(pos).get(PaintableBlockEntity.DESIGN_PROPERTY);
        return d!=null?d:BlockDesign.full(state.getBlock() instanceof PaintableBlock b?b.resolution():1);
    }
    @Override public Object createGeometryKey(BlockAndTintGetter level,BlockPos pos,BlockState state,RandomSource random){return new GeometryKey(design(level,pos,state),neighbors(level,pos),meshes);}
    @Override public void collectParts(BlockAndTintGetter level,BlockPos pos,BlockState state,RandomSource random,List<BlockStateModelPart> parts){var d=design(level,pos,state);parts.add(meshes.mesh(d,net.nicomar.paintableblocks.design.NeighborOcclusion.visible(d,neighbors(level,pos))).part());}
    @Override public Material.Baked particleMaterial(){return meshes.particle();}
    // Conservative flags; actual quads retain their material flags, including animations.
    @Override public int materialFlags(){return 3;}
    public record Unbaked(Identifier baseModel) implements CustomUnbakedBlockStateModel {
        public static final MapCodec<Unbaked> CODEC=Identifier.CODEC.fieldOf("base_model").xmap(Unbaked::new,Unbaked::baseModel);
        @Override public BlockStateModel bake(ModelBaker baker){return new PaintableBlockModel(DesignMesh.bake(baker,baseModel));}
        @Override public void resolveDependencies(Resolver resolver){resolver.markDependency(baseModel);}
        @Override public MapCodec<? extends CustomUnbakedBlockStateModel> codec(){return CODEC;}
    }
}
