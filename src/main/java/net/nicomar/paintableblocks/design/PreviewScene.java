package net.nicomar.paintableblocks.design;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

/** Immutable GUI-only scene carried by a temporary stack; never inserted into player inventories. */
public record PreviewScene(List<Piece> pieces,int yaw,int pitch) {
    public record Piece(BlockPos pos,BlockDesign design,int tint){
        public Piece(BlockPos pos,BlockDesign design){this(pos,design,-1);}
        public static final Codec<Piece> CODEC=RecordCodecBuilder.create(i->i.group(BlockPos.CODEC.fieldOf("pos").forGetter(Piece::pos),BlockDesign.CODEC.fieldOf("design").forGetter(Piece::design),Codec.INT.optionalFieldOf("tint",-1).forGetter(Piece::tint)).apply(i,Piece::new));
    }
    public static final Codec<PreviewScene> CODEC=RecordCodecBuilder.create(i->i.group(Piece.CODEC.listOf(0,8192).fieldOf("pieces").forGetter(PreviewScene::pieces),Codec.INT.optionalFieldOf("yaw",45).forGetter(PreviewScene::yaw),Codec.INT.optionalFieldOf("pitch",30).forGetter(PreviewScene::pitch)).apply(i,PreviewScene::new));
    public PreviewScene(List<Piece> pieces){this(pieces,45,30);}
    public PreviewScene{pieces=List.copyOf(pieces);if(pieces.size()>8192)throw new IllegalArgumentException("Scene too large");}
}
