package net.nicomar.paintableblocks;

import java.util.BitSet;
import java.util.List;
import net.minecraft.core.Direction;
import net.nicomar.paintableblocks.balance.PaintingBalance;
import net.nicomar.paintableblocks.design.*;
import net.nicomar.paintableblocks.operation.DesignOperations;
import net.nicomar.paintableblocks.paint.*;
import net.nicomar.paintableblocks.inventory.PaintTransfers;

/** Prepared contracts on the development classpath. Requires explicit authorization to compile/run. */
public final class ToolPaintContractChecks {
    private static int checks;
    private static void check(boolean condition,String reason){checks++;if(!condition)throw new AssertionError(reason);}
    private static DesignOperations.Plan tool(BlockDesign d,int cell,Direction face,DesignOperations.Action action){return DesignOperations.tool(d,new DesignGeometry.Surface(cell,face),action,face,false);}
    public static void main(String[] args){
        edits();paints();storage();
        System.out.println("Tool/paint contracts: "+checks);
    }
    private static void edits(){
        var d=BlockDesign.full(4);BitSet holes=new BitSet();holes.set(d.index(0,3,0));holes.set(d.index(1,3,0));holes.set(d.index(1,2,0));d=d.remove(holes);
        int hit=d.index(2,3,0);
        var mallet=tool(d,hit,Direction.UP,DesignOperations.Action.MALLET);
        check(mallet.cells().size()==16,"Mallet removes exactly one cell per occupied column");
        check(mallet.cells().contains(d.index(0,2,0))&&mallet.cells().contains(d.index(1,1,0)),"Mallet reaches different depths");
        var chisel=tool(d,hit,Direction.UP,DesignOperations.Action.CHISEL);
        check(chisel.cells().size()==14&&chisel.after().occupied(d.index(0,2,0)),"Chisel never drills through holes");
        check(tool(d,hit,Direction.UP,DesignOperations.Action.HAMMER).cells().size()==1,"Hammer removes only target");
        var one=BlockDesign.full(1);
        check(!tool(one,0,Direction.UP,DesignOperations.Action.HAMMER).changes(),"1x1 cannot be carved");
        BitSet allButOne=new BitSet();allButOne.set(0,8);allButOne.clear(0);var last=BlockDesign.full(2).remove(allButOne);
        check(tool(last,0,Direction.DOWN,DesignOperations.Action.HAMMER).after().empty(),"Last cell removes block");
        for(var face:Direction.values()){
            int quarter=DesignOrientation.quarterTurn(face,false),reverse=DesignOrientation.quarterTurn(face,true),rotation=0;
            for(int step=0;step<4;step++)rotation=DesignOrientation.compose(quarter,rotation);
            check(rotation==0&&DesignOrientation.compose(quarter,reverse)==0,"Four turns and reverse restore orientation");
        }
    }
    private static void paints(){
        for(int n:PaintingBalance.RESOLUTIONS){
            var d=BlockDesign.full(n);var first=new DesignGeometry.Surface(d.index(0,n-1,0),Direction.UP);var last=new DesignGeometry.Surface(d.index(n-1,n-1,n-1),Direction.UP);
            var area=DesignOperations.area(d,first,last);var painted=DesignOperations.paint(d,area,1);
            check(area.size()==n*n&&painted.units()==256,"Whole face has constant cost at every resolution");
            check(DesignOperations.paint(painted.after(),area,1).units()==0,"Same paint is free");
            check(DesignOperations.paint(painted.after(),area,2).units()==256,"Replacement costs surface without refund");
            var cleaned=tool(painted.after(),first.cell(),Direction.UP,DesignOperations.Action.SCRAPE);
            check(!cleaned.after().painted()&&cleaned.after().sameGeometry(d),"Scraper removes paint, keeps geometry");
        }
        var d=BlockDesign.full(4);BitSet holes=new BitSet();holes.set(d.index(1,3,1));d=d.remove(holes);
        var area=DesignOperations.area(d,new DesignGeometry.Surface(d.index(0,3,0),Direction.UP),new DesignGeometry.Surface(d.index(3,3,3),Direction.UP));
        check(area.size()==15&&area.stream().noneMatch(f->f.cell()==BlockDesign.full(4).index(1,2,1)),"Area skips holes and deeper exposed faces");
        var inside=DesignOperations.paint(d,List.of(new DesignGeometry.Surface(d.index(1,2,1),Direction.UP)),1).after();
        check(!tool(inside,d.index(0,3,0),Direction.UP,DesignOperations.Action.SCRAPE).changes(),"Planar scraper cannot erase a deeper face");
        check(!tool(inside,d.index(0,3,0),Direction.UP,DesignOperations.Action.CLEAN).after().painted(),"Sponge reaches interior exposed paints");
    }
    private static void storage(){
        var tier=PaintingBalance.Bucket.COPPER;var storage=PaintStorage.EMPTY.add(1,tier.capacityPerPaint-1);
        check(PaintTransfers.itemCountThatFits(storage,tier,1,64)==0,"Fractional capacity cannot consume a whole paint item");
        check(storage.room(tier,2)==0,"Slot capacity independent from unit capacity");
        for(var material:PaintingBalance.Material.values()){
            var bank=PaintStorage.EMPTY.add(1,PaintingBalance.Bucket.NETHERITE.capacityPerPaint);
            var loaded=PaintTransfers.load(PaintContent.EMPTY,material.brushCapacity,bank,1,false);
            check(loaded.moved()==material.brushCapacity,"Brush capacity respected");
            var returned=PaintTransfers.giveBack(loaded.brush(),loaded.storage(),PaintingBalance.Bucket.NETHERITE);
            check(returned.storage().equals(bank)&&returned.brush().units()==0,"Load/return conserves all real units");
            var creative=PaintTransfers.load(PaintContent.EMPTY,material.brushCapacity,bank,1,true);
            check(creative.brush().paint()==1&&creative.brush().units()==0&&creative.storage().equals(bank),"Creative selection creates no transferable units");
        }
        var bank=PaintStorage.EMPTY.add(2,300);var occupied=new PaintContent(1,10);
        check(PaintTransfers.load(occupied,1024,bank,2,false).brush().equals(occupied),"Real paint identity cannot mix");
        var gold=PaintStorage.EMPTY.add(1,PaintingBalance.Bucket.GOLD.capacityPerPaint);
        check(gold.fits(PaintingBalance.Bucket.GOLD)&&!gold.fits(PaintingBalance.Bucket.DIAMOND),"Gold to diamond must reject excessive per-paint contents");
    }
}
