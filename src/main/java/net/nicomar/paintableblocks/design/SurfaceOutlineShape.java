package net.nicomar.paintableblocks.design;

import java.util.List;
import net.minecraft.world.phys.shapes.Shapes;

/** Exact perimeter of affected polygons, without drawing stepped collision surfaces. */
public final class SurfaceOutlineShape extends DesignOutlineShape {
    private final BlockDesign design;
    private final List<DesignGeometry.Surface> surfaces;
    public SurfaceOutlineShape(BlockDesign d,List<DesignGeometry.Surface> surfaces){super(d);design=d.withoutPaint();this.surfaces=List.copyOf(surfaces);}
    @Override public void forAllEdges(Shapes.DoubleLineConsumer consumer){for(var surface:surfaces)emit(design,surface,consumer);}
}
