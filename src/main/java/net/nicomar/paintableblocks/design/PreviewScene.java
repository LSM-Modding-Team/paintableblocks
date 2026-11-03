package net.nicomar.paintableblocks.design;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

/** Immutable GUI-only scene carried by a temporary stack; never inserted into player inventories. */
public record PreviewScene(List<Piece> pieces) {
    public record Piece(BlockPos pos,BlockDesign design){
        public static final Codec<Piece> CODEC=RecordCodecBuilder.create(i->i.group(BlockPos.CODEC.fieldOf("pos").forGetter(Piece::pos),BlockDesign.CODEC.fieldOf("design").forGetter(Piece::design)).apply(i,Piece::new));
    }
    public static final Codec<PreviewScene> CODEC=Piece.CODEC.listOf(0,8192).xmap(PreviewScene::new,PreviewScene::pieces);
    public PreviewScene{pieces=List.copyOf(pieces);if(pieces.size()>8192)throw new IllegalArgumentException("Scene too large");}
}
