package net.nicomar.paintableblocks.design;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** The 24 proper cube rotations: rows are images of local +X,+Y,+Z. No reflections. */
public final class DesignOrientation {
    private static final List<int[]> ROTATIONS = rotations();
    private static final Direction[][] DIRECTIONS=directionTable();
    private static final int[][] COMPOSITIONS=compositionTable();
    private static final int[] INVERSES=inverseTable();
    private DesignOrientation() {}
    private static Direction[][] directionTable(){var out=new Direction[24][6];for(int i=0;i<24;i++)for(var d:Direction.values()){int[] m=ROTATIONS.get(i);int x=d.getStepX(),y=d.getStepY(),z=d.getStepZ();out[i][d.ordinal()]=Direction.getNearest(m[0]*x+m[1]*y+m[2]*z,m[3]*x+m[4]*y+m[5]*z,m[6]*x+m[7]*y+m[8]*z,Direction.UP);}return out;}
    private static int[][] compositionTable(){int[][] out=new int[24][24];for(int outer=0;outer<24;outer++)for(int inner=0;inner<24;inner++){int[] a=ROTATIONS.get(outer),b=ROTATIONS.get(inner),c=new int[9];for(int row=0;row<3;row++)for(int col=0;col<3;col++)for(int k=0;k<3;k++)c[row*3+col]+=a[row*3+k]*b[k*3+col];for(int i=0;i<24;i++)if(java.util.Arrays.equals(c,ROTATIONS.get(i))){out[outer][inner]=i;break;}}return out;}
    private static int[] inverseTable(){int[] out=new int[24];for(int a=0;a<24;a++)for(int b=0;b<24;b++)if(COMPOSITIONS[a][b]==0){out[a]=b;break;}return out;}

    public static int require(int orientation) {
        if (orientation < 0 || orientation >= 24) throw new IllegalArgumentException("Invalid cube orientation");
        return orientation;
    }
    private static List<int[]> rotations() {
        List<int[]> result = new ArrayList<>();
        result.add(new int[]{1, 0, 0, 0, 1, 0, 0, 0, 1});
        int[][] axes = {{1,0,0},{0,1,0},{0,0,1},{-1,0,0},{0,-1,0},{0,0,-1}};
        for (int[] x : axes) for (int[] y : axes) {
            if (x[0]*y[0]+x[1]*y[1]+x[2]*y[2] != 0) continue;
            int[] z = {x[1]*y[2]-x[2]*y[1], x[2]*y[0]-x[0]*y[2], x[0]*y[1]-x[1]*y[0]};
            int[] matrix = {x[0],y[0],z[0], x[1],y[1],z[1], x[2],y[2],z[2]};
            if (result.stream().noneMatch(m -> java.util.Arrays.equals(m, matrix))) result.add(matrix);
        }
        if (result.size() != 24) throw new IllegalStateException("Rotation group must contain 24 elements");
        return List.copyOf(result);
    }
    public static Vec3 rotate(int orientation, Vec3 point) {
        int[] m = ROTATIONS.get(require(orientation));
        double x = point.x - .5, y = point.y - .5, z = point.z - .5;
        return new Vec3(m[0]*x+m[1]*y+m[2]*z+.5, m[3]*x+m[4]*y+m[5]*z+.5, m[6]*x+m[7]*y+m[8]*z+.5);
    }
    public static int[] cell(int orientation, int x, int y, int z, int n) {
        int[] m=ROTATIONS.get(require(orientation));int[] out=new int[3];
        for(int row=0;row<3;row++){int i=row*3;out[row]=m[i]*x+m[i+1]*y+m[i+2]*z+(m[i]+m[i+1]+m[i+2]<0?n-1:0);}return out;
    }
    public static Direction direction(int orientation,Direction direction){return DIRECTIONS[require(orientation)][direction.ordinal()];}
    public static int compose(int outer,int inner){return COMPOSITIONS[require(outer)][require(inner)];}
    public static int inverse(int orientation){return INVERSES[require(orientation)];}
    public static int quarterTurn(Direction axis,boolean reverse){
        int ax=axis.getStepX()*(reverse?-1:1), ay=axis.getStepY()*(reverse?-1:1), az=axis.getStepZ()*(reverse?-1:1);
        Direction[] positive={Direction.EAST,Direction.UP,Direction.SOUTH};
        Direction[] rotated=new Direction[3];
        for(int i=0;i<3;i++){
            Direction d=positive[i];int x=d.getStepX(),y=d.getStepY(),z=d.getStepZ(),dot=ax*x+ay*y+az*z;
            rotated[i]=Direction.getNearest(ay*z-az*y+dot*ax,az*x-ax*z+dot*ay,ax*y-ay*x+dot*az,Direction.UP);
        }
        for(int i=0;i<24;i++)if(direction(i,positive[0])==rotated[0]&&direction(i,positive[1])==rotated[1]&&direction(i,positive[2])==rotated[2])return i;
        throw new IllegalStateException("Quarter turn must be a proper cube rotation");
    }
    public static int placement(Direction facing, boolean upsideDown) {
        // Local north faces the player-facing direction; inverted is a half-turn around local Z.
        for(int i=0;i<24;i++) if(direction(i,Direction.NORTH)==facing && direction(i,Direction.UP)==(upsideDown?Direction.DOWN:Direction.UP)) return i;
        throw new IllegalArgumentException("Placement facing must be horizontal");
    }
}
