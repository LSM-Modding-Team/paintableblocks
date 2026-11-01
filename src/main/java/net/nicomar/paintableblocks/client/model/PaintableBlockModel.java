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
    private record GeometryKey(BlockDesign design,java.util.List<java.util.List<net.nicomar.paintableblocks.design.NeighborOcclusion.PaintedPatch>> neighbors,java.util.Map<BlockPos,BlockDesign> surroundings,DesignMesh resourceGeneration){}
    private java.util.List<BlockDesign> neighbors(BlockAndTintGetter level,BlockPos pos){return net.nicomar.paintableblocks.design.NeighborOcclusion.neighbors(p->level.getModelData(p).get(PaintableBlockEntity.DESIGN_PROPERTY),pos);}
    private java.util.Map<BlockPos,BlockDesign> surroundings(BlockDesign design,BlockAndTintGetter level,BlockPos pos){
        if(net.nicomar.paintableblocks.design.GlassConnections.needed(design))return net.nicomar.paintableblocks.design.GlassConnections.surroundings(p->level.getModelData(p).get(PaintableBlockEntity.DESIGN_PROPERTY),pos);
        if(!net.nicomar.paintableblocks.design.GlassConnections.hasGlass(design))return java.util.Map.of();
        var out=new java.util.HashMap<BlockPos,BlockDesign>();
        for(var direction:net.minecraft.core.Direction.values()){
            var d=level.getModelData(pos.relative(direction)).get(PaintableBlockEntity.DESIGN_PROPERTY);
            if(d!=null)out.put(new BlockPos(direction.getStepX(),direction.getStepY(),direction.getStepZ()),d);
        }
        return java.util.Map.copyOf(out);
    }
    public PaintableBlockModel(DesignMesh meshes){this.meshes=meshes;}
    private BlockDesign design(BlockAndTintGetter level,BlockPos pos,BlockState state){
        BlockDesign d=level.getModelData(pos).get(PaintableBlockEntity.DESIGN_PROPERTY);
        return d!=null?d:BlockDesign.full(state.getBlock() instanceof PaintableBlock b?b.resolution():1);
    }
    @Override public Object createGeometryKey(BlockAndTintGetter level,BlockPos pos,BlockState state,RandomSource random){var d=design(level,pos,state);return new GeometryKey(d,net.nicomar.paintableblocks.design.NeighborOcclusion.neighborKeys(neighbors(level,pos)),surroundings(d,level,pos),meshes);}
    @Override public void collectParts(BlockAndTintGetter level,BlockPos pos,BlockState state,RandomSource random,List<BlockStateModelPart> parts){var d=design(level,pos,state);parts.add(meshes.mesh(d,net.nicomar.paintableblocks.design.NeighborOcclusion.visible(d,neighbors(level,pos)),surroundings(d,level,pos)).part());}
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
