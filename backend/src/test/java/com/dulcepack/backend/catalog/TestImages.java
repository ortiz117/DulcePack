package com.dulcepack.backend.catalog;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

public final class TestImages {

    private TestImages() {
    }

    public static byte[] png() {
        return write("png");
    }

    public static byte[] jpeg() {
        return write("jpg");
    }

    public static byte[] gif() {
        return write("gif");
    }

    private static byte[] write(String format) {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            if (!ImageIO.write(image, format, output)) {
                throw new IllegalStateException("No writer available for " + format);
            }
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not encode test image", exception);
        }
        return output.toByteArray();
    }
}
