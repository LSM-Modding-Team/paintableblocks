package net.nicomar.paintableblocks.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.nicomar.paintableblocks.block.PaintableBlock;
import net.nicomar.paintableblocks.blockentity.PaintableBlockEntity;
import net.nicomar.paintableblocks.design.BlockDesign;
import net.nicomar.paintableblocks.design.DesignGeometry;
import net.nicomar.paintableblocks.design.DesignOrientation;
import net.nicomar.paintableblocks.registry.ModContent;

public final class PaintableBlockItem extends BlockItem {
    public PaintableBlockItem(PaintableBlock block,Properties properties){super(block,properties);}
    public static BlockDesign preparedFor(BlockPlaceContext context){
        BlockDesign design=context.getItemInHand().get(ModContent.DESIGN.get());
        if(design==null || design.empty() || design.resolution()!=((PaintableBlock)((PaintableBlockItem)context.getItemInHand().getItem()).getBlock()).resolution())return null;
        Direction face=context.getClickedFace();
        boolean inverted=face==Direction.DOWN || (face.getAxis()!=Direction.Axis.Y && context.getClickLocation().y-context.getClickedPos().getY()>.5);
        int placement=DesignOrientation.placement(context.getHorizontalDirection(),inverted);
        return design.withOrientation(DesignOrientation.compose(placement,design.orientation()));
    }
    @Override public InteractionResult place(BlockPlaceContext context){
        BlockDesign design=preparedFor(context);
        if(design==null)return InteractionResult.FAIL;
        InteractionResult result=super.place(context);
        if(result.consumesAction() && !context.getLevel().isClientSide() && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof PaintableBlockEntity block) {
            block.setDesign(design);
        }
        return result;
    }
    @Override protected boolean canPlace(BlockPlaceContext context,BlockState state){
        BlockDesign design=preparedFor(context);
        return design!=null && state.canSurvive(context.getLevel(),context.getClickedPos()) && context.getLevel().isUnobstructed(null,
                DesignGeometry.shape(design).move(context.getClickedPos().getX(),context.getClickedPos().getY(),context.getClickedPos().getZ()));
    }
    @Override protected BlockState getPlacementState(BlockPlaceContext context){
        BlockDesign design=preparedFor(context);
        BlockState state=super.getPlacementState(context);
        return design==null||state==null?null:state.setValue(PaintableBlock.CUT,!design.complete());
    }

}
