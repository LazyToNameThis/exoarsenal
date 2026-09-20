package com.exoarsenal.client.render;

import com.exoarsenal.client.ScoutWingGeometry;
import net.minecraft.client.renderer.BufferBuilder;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import javax.vecmath.Vector4f;
import javax.vecmath.Vector3f;

final class ScoutWingMesh {
    private ScoutWingMesh() {}

    static void emit(BufferBuilder buffer, boolean right, boolean outer) {
        Vector4f p = new Vector4f();
        Vector3f n = new Vector3f();
        float mirror = right ? -1 : 1;
        for (ScoutWingGeometry.Vertex v :
                outer ? ScoutWingGeometry.OUTER : ScoutWingGeometry.INNER) {
            p.set((float) -v.x * mirror / 16, (float) v.y / 16, (float) v.z / 16, 1);
            n.set((float) -v.nx * mirror, (float) v.ny, (float) v.nz);
            IGeoRenderer.MATRIX_STACK.getModelMatrix().transform(p);
            IGeoRenderer.MATRIX_STACK.getNormalMatrix().transform(n);
            float l = v.light;
            buffer.pos(p.x, p.y, p.z)
                    .tex(0, 0)
                    .color(.12F + l * .65F, .36F + l * .61F, .78F + l * .22F, 1)
                    .normal(n.x, n.y, n.z)
                    .endVertex();
        }
    }
}
