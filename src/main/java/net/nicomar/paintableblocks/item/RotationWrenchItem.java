package net.nicomar.paintableblocks.item;
import net.nicomar.paintableblocks.operation.DesignOperations;
public final class RotationWrenchItem extends EditingToolItem {
    public RotationWrenchItem(Properties properties){super(properties,DesignOperations.Action.ROTATE);}
}
