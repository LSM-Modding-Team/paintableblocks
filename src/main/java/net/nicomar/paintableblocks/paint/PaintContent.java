package net.nicomar.paintableblocks.paint;

import net.nicomar.paintableblocks.balance.PaintingBalance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** A selected identity may have zero real units (creative selection never creates stock). */
public record PaintContent(int paint,int units) {
    public static final PaintContent EMPTY=new PaintContent(0,0);
    private static final Codec<PaintContent> RAW=RecordCodecBuilder.create(i->i.group(
            Codec.intRange(0,PaintingBalance.PAINTS).fieldOf("paint").forGetter(PaintContent::paint),Codec.intRange(0,PaintingBalance.Material.NETHERITE.brushCapacity).fieldOf("units").forGetter(PaintContent::units)).apply(i,PaintContent::new));
    public static final Codec<PaintContent> CODEC=RAW.validate(c->c.valid()?DataResult.success(c):DataResult.error(()->"Invalid brush paint content"));
    public boolean valid(){return units>=0&&units<=PaintingBalance.Material.NETHERITE.brushCapacity&&(PaintCatalog.validId(paint)||(paint==0&&units==0));}
    public boolean fits(int capacity){return valid()&&units<=capacity;}
    public PaintContent withUnits(int amount){return new PaintContent(paint,amount);}
}
