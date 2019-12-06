package nullengine.client.rendering.gl.pipeline;

import com.github.mouse0w0.observable.value.ObservableObjectValue;
import nullengine.client.rendering.gl.shader.ShaderManager;
import nullengine.client.rendering.gl.shader.ShaderProgram;
import nullengine.client.rendering.gl.texture.GLFrameBuffer;
import nullengine.client.rendering.management.RenderManager;
import nullengine.client.rendering.material.Material;
import nullengine.client.rendering.queue.GeometryList;
import nullengine.client.rendering.queue.RenderQueue;
import nullengine.client.rendering.queue.RenderTypeHandler;
import nullengine.client.rendering.queue.StandardRenderTypes;
import nullengine.client.rendering.scene.ViewPort;
import nullengine.util.Color;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public class ForwardPipeline implements RenderPipeline {

    private final ObservableObjectValue<ShaderProgram> shader;
    private final GLFrameBuffer frameBuffer;

    private final Material material;

    public ForwardPipeline() {
        shader = ShaderManager.register("forward");
        frameBuffer = GLFrameBuffer.createRGB16FDepth24Stencil8FrameBuffer(1, 1);

        material = new Material()
                .setAmbientColor(new Vector3f(0.5f))
                .setDiffuseColor(new Vector3f(1.0f))
                .setSpecularColor(new Vector3f(1.0f))
                .setShininess(32f);
    }

    @Override
    public void render(RenderManager manager, ViewPort viewPort, float partial) {
        ShaderProgram shader = this.shader.get();
        shader.use();
        setupViewPort(viewPort);

        var scene = viewPort.getScene();
        if (scene == null) return;

        scene.doUpdate(partial);
        scene.getLightManager().bind(viewPort.getCamera(), shader);
        material.bind(shader, "material");

        RenderQueue renderQueue = scene.getRenderQueue();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        renderQueue.render(manager, StandardRenderTypes.OPAQUE, new RenderTypeHandler() {
            @Override
            public void render(RenderManager manager, GeometryList geometries) {
                geometries.forEach(geometry -> geometry.getRenderable().render());
            }
        });
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    private void setupViewPort(ViewPort viewPort) {
        int width = viewPort.getWidth();
        int height = viewPort.getHeight();

//        if (width != frameBuffer.getWidth() || height != frameBuffer.getHeight()) {
//            frameBuffer.resize(width, height);
//        }
//        frameBuffer.bind();
        GLFrameBuffer.bindScreenFrameBuffer();

        GL11.glViewport(0, 0, width, height);
        Color clearColor = viewPort.getClearColor();
        GL11.glClearColor(clearColor.getRed(), clearColor.getGreen(), clearColor.getBlue(), clearColor.getAlpha());
        int clearMask = 0;
        if (viewPort.isClearColor()) {
            clearMask |= GL11.GL_COLOR_BUFFER_BIT;
        }
        if (viewPort.isClearDepth()) {
            clearMask |= GL11.GL_DEPTH_BUFFER_BIT;
        }
        if (viewPort.isClearStencil()) {
            clearMask |= GL11.GL_STENCIL_BUFFER_BIT;
        }
        GL11.glClear(clearMask);
    }

    public GLFrameBuffer getFrameBuffer() {
        return frameBuffer;
    }
}