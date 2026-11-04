package net.nicomar.paintableblocks.client.model;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.nicomar.paintableblocks.design.*;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** One native item render for the entire scene, sharing depth and the normal design textures. */
final class PreviewSceneMesh {
    static QuadCollection build(DesignMesh meshes,PreviewScene scene){
        var out=new QuadCollection.Builder();if(scene.pieces().isEmpty())return out.build();
        Map<BlockPos,BlockDesign> world=new HashMap<>();for(var p:scene.pieces())world.put(p.pos(),p.design());
        int x0=Integer.MAX_VALUE,y0=x0,z0=x0,x1=Integer.MIN_VALUE,y1=x1,z1=x1;
        for(var p:scene.pieces()){x0=Math.min(x0,p.pos().getX());y0=Math.min(y0,p.pos().getY());z0=Math.min(z0,p.pos().getZ());x1=Math.max(x1,p.pos().getX()+1);y1=Math.max(y1,p.pos().getY()+1);z1=Math.max(z1,p.pos().getZ()+1);}
        float scale=1F/Math.max(x1-x0,Math.max(y1-y0,z1-z0));double cx=(x0+(double)x1)/2,cy=(y0+(double)y1)/2,cz=(z0+(double)z1)/2;
        var camera=new org.joml.Quaternionf().rotationX((float)Math.toRadians(-30)).rotateY((float)Math.toRadians(-225)).rotateX((float)Math.toRadians(scene.pitch())).rotateY((float)Math.toRadians(scene.yaw()));
        for(var piece:scene.pieces()){
            var d=piece.design();var neighbors=NeighborOcclusion.neighbors(world::get,piece.pos());
            var mesh=meshes.mesh(d,NeighborOcclusion.visible(d,neighbors),GlassConnections.hasGlass(d)?GlassConnections.surroundings(world::get,piece.pos()):Map.of());
            for(var q:mesh.quads().getAll()){
                var a=rotate(move(q.position0(),piece.pos(),scale,cx,cy,cz),camera);var b=rotate(move(q.position1(),piece.pos(),scale,cx,cy,cz),camera);var c=rotate(move(q.position2(),piece.pos(),scale,cx,cy,cz),camera);var d0=rotate(move(q.position3(),piece.pos(),scale,cx,cy,cz),camera);
                var normal=new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a));if(normal.lengthSquared()>1e-12F)normal.normalize();
                var direction=net.minecraft.core.Direction.getNearest((int)(normal.x*512),(int)(normal.y*512),(int)(normal.z*512),q.direction());
                var colors=q.bakedColors();int tint=piece.tint();
                out.addUnculledFace(new BakedQuad(a,b,c,d0,q.packedUV0(),q.packedUV1(),q.packedUV2(),q.packedUV3(),direction,q.materialInfo(),net.neoforged.neoforge.client.model.quad.BakedNormals.of(net.neoforged.neoforge.client.model.quad.BakedNormals.pack(normal.x,normal.y,normal.z)),net.neoforged.neoforge.client.model.quad.BakedColors.of(tint(colors.color(0),tint),tint(colors.color(1),tint),tint(colors.color(2),tint),tint(colors.color(3),tint))));
            }
        }
        return out.build();
    }
    private static Vector3f rotate(Vector3f p,org.joml.Quaternionf q){return p.sub(.5F,.5F,.5F).rotate(q).mul(.75F).add(.5F,.5F,.5F);}
    private static int tint(int color,int tint){int out=0;for(int shift=0;shift<32;shift+=8)out|=(((color>>>shift)&255)*((tint>>>shift)&255)/255)<<shift;return out;}
    private static Vector3f move(Vector3fc p,BlockPos pos,float scale,double x,double y,double z){return new Vector3f((float)(.5+(p.x()+(double)pos.getX()-x)*scale),(float)(.5+(p.y()+(double)pos.getY()-y)*scale),(float)(.5+(p.z()+(double)pos.getZ()-z)*scale));}
}
