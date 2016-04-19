package com.nucleus.texture.android;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.nucleus.texturing.BaseImageFactory;
import com.nucleus.texturing.Image;
import com.nucleus.texturing.Image.ImageFormat;
import com.nucleus.texturing.ImageFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AndroidImageFactory extends BaseImageFactory implements ImageFactory {

    @Override
    public Image createImage(String name, ImageFormat format, float scaleX, float scaleY) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        Bitmap b = BitmapFactory.decodeStream(classLoader.getResourceAsStream(name));
        if (b == null) {
            throw new IOException("Could not load " + name);
        }
        byte[] bytePixels = new byte[b.getWidth() * b.getHeight() * 4];
        ByteBuffer bb = ByteBuffer.wrap(bytePixels);
        b.copyPixelsToBuffer(bb);
        Image image = new Image(b.getWidth(), b.getHeight(), format);
        copyPixels(bytePixels, ImageFormat.RGBA, image);
        if (scaleX != 1 || scaleY != 1) {
            int width = (int) (b.getWidth() * scaleX + 0.5f);
            int height = (int) (b.getHeight() * scaleY + 0.5f);
            if (height == 0) {
                height = 1;
            }
            if (width == 0) {
                width = 1;
            }
            return createScaledImage(image, width, height, ImageFormat.RGBA);

        }
        return image;
    }
}
