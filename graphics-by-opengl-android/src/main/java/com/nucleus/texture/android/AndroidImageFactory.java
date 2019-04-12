package com.nucleus.texture.android;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.nucleus.profiling.FrameSampler;
import com.nucleus.texturing.BaseImageFactory;
import com.nucleus.texturing.BufferImage;
import com.nucleus.texturing.BufferImage.ImageFormat;
import com.nucleus.texturing.BufferImage.SourceFormat;
import com.nucleus.texturing.ImageFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AndroidImageFactory extends BaseImageFactory implements ImageFactory {

    @Override
    public BufferImage createImage(String name, ImageFormat format) throws IOException {
        long start = System.currentTimeMillis();
        ClassLoader classLoader = getClass().getClassLoader();
        Bitmap b = BitmapFactory.decodeStream(classLoader.getResourceAsStream(name));
        SourceFormat sf = getFormat(b);
        long loaded = System.currentTimeMillis();
        FrameSampler.getInstance().logTag(FrameSampler.Samples.LOAD_IMAGE, start, loaded);
        if (b == null) {
            throw new IOException("Could not load " + name);
        }
        byte[] bytePixels = new byte[b.getWidth() * b.getHeight() * 4];
        ByteBuffer bb = ByteBuffer.wrap(bytePixels);
        b.copyPixelsToBuffer(bb);
        BufferImage image = new BufferImage(b.getWidth(), b.getHeight(), format != null ? format : sf.imageFormat);
        if (b.getConfig() != Bitmap.Config.ARGB_8888) {
            throw new IllegalArgumentException("Not supported Bitmap.Config " + b.getConfig());
        }
        copyPixels(bytePixels, SourceFormat.TYPE_INT_ARGB, image);
        b.recycle();
        FrameSampler.getInstance().logTag(FrameSampler.Samples.COPY_IMAGE, " " + image.getFormat().toString(), loaded,
                System.currentTimeMillis());
        return image;
    }

    protected SourceFormat getFormat(Bitmap b) {
        switch (b.getConfig()) {
            case ARGB_8888:
                return SourceFormat.TYPE_RGBA;
            case RGB_565:
                return SourceFormat.TYPE_RGB565;
            default:
                throw new IllegalArgumentException("No support for config " + b.getConfig());

        }
    }

}
