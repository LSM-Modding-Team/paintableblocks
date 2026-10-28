package net.nicomar.paintableblocks.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.nicomar.paintableblocks.operation.DesignOperations;

/** Cancellable authoritative edit. A break event is additionally posted only for removed cells. */
public final class PaintableBlockEditEvent extends BlockEvent implements ICancellableEvent {
    private final Player player;
    private final InteractionHand hand;
    private final DesignOperations.Plan plan;
    public PaintableBlockEditEvent(Level level,BlockPos pos,BlockState state,Player player,InteractionHand hand,DesignOperations.Plan plan){super(level,pos,state);this.player=player;this.hand=hand;this.plan=plan;}
    public Player getPlayer(){return player;}
    public InteractionHand getHand(){return hand;}
    public DesignOperations.Plan getPlan(){return plan;}
}
