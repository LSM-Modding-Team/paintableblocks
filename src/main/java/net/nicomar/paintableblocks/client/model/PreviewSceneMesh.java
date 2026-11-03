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
        for(var piece:scene.pieces()){
            var d=piece.design();var neighbors=NeighborOcclusion.neighbors(world::get,piece.pos());
            var mesh=meshes.mesh(d,NeighborOcclusion.visible(d,neighbors),GlassConnections.hasGlass(d)?GlassConnections.surroundings(world::get,piece.pos()):Map.of());
            for(var q:mesh.quads().getAll()){
                out.addUnculledFace(new BakedQuad(move(q.position0(),piece.pos(),scale,cx,cy,cz),move(q.position1(),piece.pos(),scale,cx,cy,cz),move(q.position2(),piece.pos(),scale,cx,cy,cz),move(q.position3(),piece.pos(),scale,cx,cy,cz),q.packedUV0(),q.packedUV1(),q.packedUV2(),q.packedUV3(),q.direction(),q.materialInfo(),q.bakedNormals(),q.bakedColors()));
            }
        }
        return out.build();
    }
    private static Vector3f move(Vector3fc p,BlockPos pos,float scale,double x,double y,double z){return new Vector3f((float)(.5+(p.x()+(double)pos.getX()-x)*scale),(float)(.5+(p.y()+(double)pos.getY()-y)*scale),(float)(.5+(p.z()+(double)pos.getZ()-z)*scale));}
}
