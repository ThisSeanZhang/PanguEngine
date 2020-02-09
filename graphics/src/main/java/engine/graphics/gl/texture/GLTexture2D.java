package engine.graphics.gl.texture;

import engine.graphics.image.ImageHelper;
import engine.graphics.image.ReadOnlyImage;
import engine.graphics.texture.FilterMode;
import engine.graphics.texture.Texture2D;
import engine.graphics.texture.WrapMode;
import engine.util.Color;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public final class GLTexture2D extends GLTexture implements Texture2D {

    public static final GLTexture2D EMPTY = new GLTexture2D(0);

    private int internalFormat;
    private int format;
    private int type;

    private int width;
    private int height;

    private boolean mipmap;

    public static GLTexture2D of(ByteBuffer fileBuffer) throws IOException {
        ReadOnlyImage image = ImageHelper.instance().loadImage(fileBuffer);
        return of(image.getPixelBuffer(), image.getWidth(), image.getHeight());
    }

    public static GLTexture2D of(ReadOnlyImage image) {
        return of(image.getPixelBuffer(), image.getWidth(), image.getHeight());
    }

    public static GLTexture2D of(ByteBuffer pixelBuffer, int width, int height) {
        if (!pixelBuffer.isDirect()) {
            ByteBuffer direct = ByteBuffer.allocateDirect(pixelBuffer.capacity());
            direct.put(pixelBuffer);
            pixelBuffer = direct;
        }
        return builder().build(pixelBuffer, width, height);
    }

    public static Builder builder() {
        return new Builder();
    }

    private GLTexture2D(int id) {
        super(id);
    }

    @Override
    public int getTarget() {
        return GL_TEXTURE_2D;
    }

    public void upload(ReadOnlyImage image) {
        upload(image.getPixelBuffer(), image.getWidth(), image.getHeight(), 0);
    }

    public void upload(ByteBuffer pixelBuffer, int width, int height) {
        upload(pixelBuffer, width, height, 0);
    }

    public void upload(ByteBuffer pixelBuffer, int width, int height, int level) {
        bind();
        glTexImage2D(pixelBuffer, width, height, level);
    }

    public void glTexImage2D(ByteBuffer pixelBuffer, int width, int height, int level) {
        this.width = width;
        this.height = height;
//        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL_TEXTURE_2D, level, internalFormat, width, height, 0, format, type, pixelBuffer);
        if (mipmap) glGenerateMipmap(GL_TEXTURE_2D);
//        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
    }

    public void upload(int offsetX, int offsetY, ReadOnlyImage image) {
        upload(offsetX, offsetY, image, 0);
    }

    public void upload(int offsetX, int offsetY, ReadOnlyImage image, int level) {
        bind();
        glTexSubImage2D(offsetX, offsetY, image, level);
    }

    public void glTexSubImage2D(int offsetX, int offsetY, ReadOnlyImage image, int level) {
//        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexSubImage2D(GL_TEXTURE_2D, level, offsetX, offsetY, image.getWidth(), image.getHeight(), format, type, image.getPixelBuffer());
        if (mipmap) glGenerateMipmap(GL_TEXTURE_2D);
//        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public int getInternalFormat() {
        return internalFormat;
    }

    public int getFormat() {
        return format;
    }

    public int getType() {
        return type;
    }

    public static final class Builder implements Texture2D.Builder {

        private final Map<Integer, Integer> parameterMap = new HashMap<>();

        private boolean mipmap = false;
        private int internalFormat = GL_SRGB_ALPHA;
        private int format = GL_RGBA;
        private int type = GL_UNSIGNED_BYTE;

        private Builder() {
            magFilter(FilterMode.NEAREST);
            minFilter(FilterMode.NEAREST);
            wrapS(WrapMode.REPEAT);
            wrapT(WrapMode.REPEAT);
        }

        @Override
        public Builder magFilter(FilterMode mode) {
            parameterMap.put(GL_TEXTURE_MAG_FILTER, toGLFilterMode(mode));
            return this;
        }

        @Override
        public Builder minFilter(FilterMode mode) {
            parameterMap.put(GL_TEXTURE_MIN_FILTER, toGLFilterMode(mode));
            return this;
        }

        @Override
        public Builder wrapS(WrapMode mode) {
            parameterMap.put(GL_TEXTURE_WRAP_S, toGLWrapMode(mode));
            return this;
        }

        @Override
        public Builder wrapT(WrapMode mode) {
            parameterMap.put(GL_TEXTURE_WRAP_T, toGLWrapMode(mode));
            return this;
        }

        @Override
        public Builder generateMipmap() {
            mipmap = true;
            return this;
        }

        @Override
        public Builder borderColor(Color color) {
            parameterMap.put(GL_TEXTURE_BORDER_COLOR, color.toRGBA());
            return this;
        }

        public Builder internalFormat(int internalFormat) {
            this.internalFormat = internalFormat;
            return this;
        }

        public Builder format(int format) {
            this.format = format;
            return this;
        }

        public Builder type(int type) {
            this.type = type;
            return this;
        }

        public GLTexture2D build() {
            return build(null, 0, 0);
        }

        public GLTexture2D build(ReadOnlyImage image) {
            return build(image.getPixelBuffer(), image.getWidth(), image.getHeight());
        }

        public GLTexture2D build(int width, int height) {
            return build(null, width, height);
        }

        public GLTexture2D build(ByteBuffer pixelBuffer, int width, int height) {
            GLTexture2D glTexture2D = new GLTexture2D(glGenTextures());
            glTexture2D.internalFormat = internalFormat;
            glTexture2D.format = format;
            glTexture2D.type = type;
            glTexture2D.mipmap = mipmap;
            glTexture2D.bind();

            parameterMap.forEach((key, value) -> glTexParameteri(GL_TEXTURE_2D, key, value));

            glTexture2D.glTexImage2D(pixelBuffer, width, height, 0);
            return glTexture2D;
        }
    }
}