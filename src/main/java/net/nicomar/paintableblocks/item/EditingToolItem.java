package net.nicomar.paintableblocks.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.nicomar.paintableblocks.operation.DesignOperations;
import net.nicomar.paintableblocks.interaction.ServerInteractions;

/** Right-click editing without durability, model or texture. Native breaking keeps the drop policy. */
public class EditingToolItem extends Item {
    private final DesignOperations.Action action;
    public EditingToolItem(Properties properties,DesignOperations.Action action){super(properties);this.action=action;}
    public DesignOperations.Action action(){return action;}
    @Override public InteractionResult useOn(UseOnContext context){
        if(context.getPlayer()==null)return InteractionResult.PASS;
        if(!context.getLevel().isClientSide())ServerInteractions.use(context.getPlayer(),false,context.isSecondaryUseActive(),0);
        return InteractionResult.SUCCESS;
    }
}
