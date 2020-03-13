package engine.graphics.internal.graph;

import engine.graphics.Scene3D;
import engine.graphics.graph.DrawDispatcher;
import engine.graphics.graph.Frame;
import engine.graphics.graph.Renderer;
import engine.graphics.queue.RenderType;
import engine.graphics.shader.ShaderResource;
import engine.graphics.shader.UniformBlock;
import engine.graphics.shader.UniformTexture;
import engine.graphics.viewport.Viewport;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public class ViewportSkyDrawDispatcher implements DrawDispatcher {
    private final Viewport viewport;

    private UniformBlock uniformMatrices;
    private UniformTexture uniformTexture;

    private static class Matrices implements UniformBlock.Value {
        private Matrix4fc projMatrix;
        private Matrix4fc viewMatrix;

        public Matrices(Matrix4fc projMatrix, Matrix4fc viewMatrix) {
            this.projMatrix = projMatrix;
            this.viewMatrix = viewMatrix;
        }

        @Override
        public ByteBuffer get(MemoryStack stack) {
            return get(stack.malloc(128));
        }

        @Override
        public ByteBuffer get(int index, ByteBuffer buffer) {
            projMatrix.get(index, buffer);
            viewMatrix.get(index + 64, buffer);
            return buffer;
        }
    }

    public ViewportSkyDrawDispatcher(Viewport viewport) {
        this.viewport = viewport;
    }

    @Override
    public void init(ShaderResource resource) {
        this.uniformMatrices = resource.getUniformBlock("Transformation");
        this.uniformTexture = resource.getUniformTexture("u_Texture");
    }

    @Override
    public void draw(Frame frame, ShaderResource resource, Renderer renderer) {
        if (frame.isResized()) viewport.setSize(frame.getWidth(), frame.getHeight());
        uniformMatrices.set(new Matrices(viewport.getProjectionMatrix(), viewport.getViewMatrix()));
        Scene3D scene = viewport.getScene();
        scene.doUpdate(frame.getTickLastFrame());
        scene.getRenderQueue().getGeometryList(RenderType.SKY).forEach(geometry -> {
            uniformTexture.set(geometry.getTexture());
            resource.refresh();
            renderer.drawMesh(geometry.getMesh());
        });
    }
}