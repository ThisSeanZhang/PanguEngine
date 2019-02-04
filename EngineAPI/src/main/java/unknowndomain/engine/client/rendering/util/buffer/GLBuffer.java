package unknowndomain.engine.client.rendering.util.buffer;

import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;
import unknowndomain.engine.math.Math2;
import unknowndomain.engine.util.BufferPool;
import unknowndomain.engine.util.Color;

import java.nio.ByteBuffer;

public abstract class GLBuffer {

    private ByteBuffer backingBuffer;

    private float posOffsetX;
    private float posOffsetY;
    private float posOffsetZ;

    private boolean drawing;
    private int drawMode;
    private GLBufferFormat format;

    private int elementIndex = -1;
    private int nextByteOffset = -1;

    private int vertexCount;

    public GLBuffer() {
        this(256);
    }

    public GLBuffer(int size) {
        backingBuffer = createBuffer(size);
    }

    protected abstract ByteBuffer createBuffer(int capacity);

    protected abstract void freeBuffer(ByteBuffer buffer);

    public ByteBuffer getBackingBuffer() {
        return backingBuffer;
    }

    public boolean isDrawing() {
        return drawing;
    }

    public int getDrawMode() {
        return drawMode;
    }

    public GLBufferFormat getFormat() {
        return format;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void begin(int mode, GLBufferFormat format) {
        if (drawing) {
            throw new IllegalStateException("Already drawing!");
        } else {
            drawing = true;
            reset();
            drawMode = mode;
            this.format = format;
            backingBuffer.limit(backingBuffer.capacity());
        }
    }

    public void finish() {
        if (!drawing) {
            throw new IllegalStateException("Not yet drawn!");
        } else {
            if (drawMode == GL11.GL_QUADS || drawMode == GL11.GL_QUAD_STRIP) {
                if (vertexCount % 4 != 0)
                    throw new IllegalArgumentException(String.format("Not enough vertexes! Expected: %d, Found: %d", (vertexCount / 4 + 1) * 4, vertexCount));
                byte[] bytes = new byte[format.getStride() * 4];
                ByteBuffer tempBuffer = BufferPool.getDefaultHeapBufferPool().get(backingBuffer.capacity());
                backingBuffer.rewind();
                tempBuffer.put(backingBuffer);
                backingBuffer.clear();
                tempBuffer.flip();
                for (int i = 0; i < vertexCount / 4; i++) {
                    tempBuffer.get(bytes, 0, format.getStride() * 4);
                    backingBuffer.put(bytes, 0, format.getStride() * 3);
                    backingBuffer.put(bytes, format.getStride() * 2, format.getStride() * 2);
                    backingBuffer.put(bytes, 0, format.getStride());
                }
                vertexCount = vertexCount / 4 * 6;
                drawMode = drawMode == GL11.GL_QUAD_STRIP ? GL11.GL_TRIANGLE_STRIP : GL11.GL_TRIANGLES;
            }
            drawing = false;
            backingBuffer.position(0);
            backingBuffer.limit(vertexCount * format.getStride());
        }
    }

    public void reset() {
        drawMode = 0;
        format = null;
        vertexCount = 0;
        posOffset(0, 0, 0);
        backingBuffer.clear();
    }

    public void grow(int needLength) {
        if (needLength > backingBuffer.remaining()) {
            int oldSize = this.backingBuffer.capacity();
            int newSize = oldSize + Math2.ceil(needLength, 0x200000);
            int oldPosition = backingBuffer.position();
            ByteBuffer newBuffer = createBuffer(newSize);
            this.backingBuffer.position(0);
            newBuffer.put(this.backingBuffer);
            newBuffer.rewind();
            freeBuffer(backingBuffer);
            this.backingBuffer = newBuffer;
            newBuffer.position(oldPosition);
        }
    }

    public void endVertex() {
        vertexCount++;
        grow(format.getStride());
    }

    public GLBuffer put(byte value) {
        backingBuffer.put(value);
        return this;
    }

    public GLBuffer put(int value) {
        backingBuffer.putInt(value);
        return this;
    }

    public GLBuffer put(float value) {
        backingBuffer.putFloat(value);
        return this;
    }

    public GLBuffer put(double value) {
        backingBuffer.putDouble(value);
        return this;
    }

    public GLBuffer put(byte... bytes) {
        int bits = bytes.length * Byte.BYTES;
        if (bits % format.getStride() != 0) {
            throw new IllegalArgumentException();
        }
        backingBuffer.put(bytes);
        vertexCount += bits / format.getStride();
        return this;
    }

    public GLBuffer put(int... ints) {
        int bits = ints.length * Integer.BYTES;
        if (bits % format.getStride() != 0) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < ints.length; i++) {
            backingBuffer.putInt(ints[i]);
        }
        vertexCount += bits / format.getStride();
        return this;
    }

    public GLBuffer put(float... floats) {
        int bits = floats.length * Float.BYTES;
        if (bits % format.getStride() != 0) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < floats.length; i++) {
            backingBuffer.putFloat(floats[i]);
        }
        vertexCount += bits / format.getStride();
        return this;
    }

    public GLBuffer posOffset(float x, float y, float z) {
        posOffsetX = x;
        posOffsetY = y;
        posOffsetZ = z;
        return this;
    }

    public GLBuffer pos(float x, float y, float z) {
        if (format.isUsingPosition()) {
            backingBuffer.putFloat(x + posOffsetX);
            backingBuffer.putFloat(y + posOffsetY);
            backingBuffer.putFloat(z + posOffsetZ);
        }
        return this;
    }

    public GLBuffer color(Color color) {
        if (format.isUsingColor()) {
            color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }
        return this;
    }

    public GLBuffer color(int color) {
        if (format.isUsingColor()) {
            color(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f, ((color >> 24) & 255) / 255f);
        }
        return this;
    }

    public GLBuffer color(float r, float g, float b) {
        return color(r, g, b, 1);
    }

    public GLBuffer color(float r, float g, float b, float a) {
        if (format.isUsingColor()) {
            backingBuffer.putFloat(r);
            backingBuffer.putFloat(g);
            backingBuffer.putFloat(b);
            backingBuffer.putFloat(a);
        }
        return this;
    }

    public GLBuffer uv(float u, float v) {
        if (format.isUsingTextureUV()) {
            backingBuffer.putFloat(u);
            backingBuffer.putFloat(v);
        }
        return this;
    }

    public GLBuffer normal(Vector3fc vec) {
        return normal(vec.x(), vec.y(), vec.z());
    }

    public GLBuffer normal(float nx, float ny, float nz) {
        if (format.isUsingNormal()) {
            backingBuffer.putFloat(nx);
            backingBuffer.putFloat(ny);
            backingBuffer.putFloat(nz);
        }
        return this;
    }
}
